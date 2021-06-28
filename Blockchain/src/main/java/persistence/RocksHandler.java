package persistence;

import org.apache.commons.lang3.SerializationUtils;
import org.rocksdb.*;
import persistence.models.Block;
import persistence.models.Transaction;
import persistence.models.UTXO;

import java.io.Serializable;
import java.util.*;

public class RocksHandler {
    static {
        RocksDB.loadLibrary();
    }

    private final String id;
    private final RocksDB db;
    private final DBOptions options;
    private final List<Block> pendingUpdate;
    private final List<ColumnFamilyHandle> cfHandles;
    private final ColumnFamilyHandle committees;
    private final ColumnFamilyHandle utxos;

    public RocksHandler(String id) throws RocksDBException {
        String dbPath = System.getenv("ROCKSDB_PATH") + (this.id = id);
        ColumnFamilyOptions cfOpts = new ColumnFamilyOptions().optimizeUniversalStyleCompaction();

        List<ColumnFamilyDescriptor> cfDescriptors = Arrays.asList(
                new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, cfOpts),
                new ColumnFamilyDescriptor("snd".getBytes(), cfOpts)
        );

        cfHandles = new ArrayList<>();
        options = new DBOptions().setCreateIfMissing(true).
                setCreateMissingColumnFamilies(true);

        db = RocksDB.open(options, dbPath, cfDescriptors, cfHandles);
        committees = cfHandles.get(0);
        utxos = cfHandles.get(1);
        pendingUpdate = new ArrayList<>();
    }

    private static byte[] serialize(Serializable obj) {
        return SerializationUtils.serialize(obj);
    }

    private static Object deserialize(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }

    public void assignKeyToCommittee(String pubKey, String committeeId) {
        try {
            db.put(committees, pubKey.getBytes(), committeeId.getBytes());
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    public String getCommitteeId(String pubKey) {
        String ret;
        try {
            ret = new String(db.get(committees, pubKey.getBytes()));
        } catch (RocksDBException e) {
            return null;
        }
        return ret;
    }

    public boolean removeKeyFromCommittee(String pubKey) {
        try {
            db.delete(committees, pubKey.getBytes());
        } catch (RocksDBException e) {
            return false;
        }
        return true;
    }

    public void addUTXOSet(String pubKey, HashSet<UTXO> set) {
        try {
            byte[] arr = serialize(set);
            db.put(utxos, pubKey.getBytes(), arr);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    public HashSet<UTXO> getUTXOSet(String pubKey) {
        HashSet<UTXO> ret;
        try {
            byte[] arr = db.get(utxos, pubKey.getBytes());
            ret = (arr == null) ? new HashSet<>() : (HashSet<UTXO>) deserialize(arr);
        } catch (RocksDBException e) {
            return new HashSet<>();
        }
        return ret;
    }

    public boolean removeUTXOSet(String pubKey) {
        try {
            db.delete(utxos, pubKey.getBytes());
        } catch (RocksDBException e) {
            return false;
        }
        return true;
    }


    public void update(Block b) {
        pendingUpdate.add(b);

        String fst = System.getenv("TOTAL_NUMBER_OF_NODES");
        String snd = System.getenv("COMMITTEE_SIZE");

        int numberOfNodes = Integer.parseInt(fst);
        int committeeSize = Integer.parseInt(snd);
        int numberOfCommittees = numberOfNodes / committeeSize;
        numberOfCommittees += numberOfNodes % committeeSize == 0 ? 0 : 1;


        if (pendingUpdate.size() >= numberOfCommittees) {
            List<Transaction> ts = merge(pendingUpdate, b.getIdx() == 1);
            HashMap<String, HashSet<UTXO>> utxos = getUTXOS(ts);
            HashMap<String, HashSet<UTXO>> stxos = getSTXOS(ts);

            for (var out : utxos.entrySet()) {
                HashSet<UTXO> st = getUTXOSet(out.getKey());
                st.addAll(out.getValue());
                addUTXOSet(out.getKey(), st);
            }

            for (var in : stxos.entrySet()) {
                HashSet<UTXO> st = getUTXOSet(in.getKey());
                st.removeAll(in.getValue());
                addUTXOSet(in.getKey(), st);
            }

            pendingUpdate.clear();
        }
    }

    private List<Transaction> merge(List<Block> pendingUpdate, boolean genesis) {
        List<Transaction> ts = new ArrayList<>();
        for (Block b : pendingUpdate) {
            ts.addAll(b.getTransactions().getTransactions());
        }

        HashMap<String, ArrayList<Transaction>> mp = new HashMap<>();
        for (Transaction t : ts) {
            String pubKey = t.getInputPubKey();
            ArrayList<Transaction> arr = mp.getOrDefault(pubKey, new ArrayList<>());

            arr.add(t);
            mp.put(pubKey, arr);
        }

        List<Transaction> ret = new ArrayList<>();
        for (var e : mp.entrySet()) {
            HashSet<UTXO> utxos = new HashSet<>();
            HashSet<UTXO> stxos = new HashSet<>();

            List<UTXO> in = new ArrayList<>();
            List<UTXO> out = new ArrayList<>();

            for (Transaction t : e.getValue()) {
                stxos.addAll(t.getInput());
                utxos.addAll(t.getOutput());
            }

            int sumIn = 0, sumOut = 0;
            for (UTXO utxo : stxos) {
                sumIn += utxo.getAmount();
                in.add(utxo);
            }

            for (UTXO utxo : utxos) {
                sumOut += utxo.getAmount();
                if (sumOut > sumIn && !genesis) {
                    sumOut -= utxo.getAmount();
                    break;
                }
                out.add(utxo);
            }

            int val = sumIn - sumOut;
            String id = val + e.getKey();
            System.out.println("sumIn sumOut" + sumIn + " " + sumOut);
            UTXO returned = val == 0 ? null : new UTXO(val, e.getKey(), id);
            ret.add(new Transaction(in, out, returned));
        }

        return ret;
    }


    /**
     * @return all utxos from all transactions in this block in
     * a Hashmap where key is the public key and value is all utxos going
     * to that key
     */
    public HashMap<String, HashSet<UTXO>> getUTXOS(List<Transaction> transactions) {
        HashMap<String, HashSet<UTXO>> ret = new HashMap<>();

        for (Transaction t : transactions) {
            for (UTXO utxo : t.getOutput()) {
                addUTXOToMap(utxo, ret);
            }

            if (t.getReturned() != null) {
                addUTXOToMap(t.getReturned(), ret);
            }
        }
        return ret;
    }

    /**
     * @return spent transaction outputs from all transactions in this block
     * in a hashmap where key is the public key and value is all stxos that
     * went to that public key
     */
    public HashMap<String, HashSet<UTXO>> getSTXOS(List<Transaction> transactions) {
        HashMap<String, HashSet<UTXO>> ret = new HashMap<>();
        for (Transaction t : transactions) {
            for (UTXO utxo : t.getInput()) {
                addUTXOToMap(utxo, ret);
            }
        }
        return ret;
    }

    private void addUTXOToMap(UTXO utxo, HashMap<String, HashSet<UTXO>> mp) {
        String pubKey = utxo.getScriptPublicKey();
        HashSet<UTXO> utxos = mp.getOrDefault(pubKey, new HashSet<>());
        utxos.add(utxo);
        mp.put(pubKey, utxos);
    }


    public void closeHandler() {
        for (ColumnFamilyHandle handle : cfHandles) {
            handle.close();
        }
        options.close();
        db.close();
    }

}
