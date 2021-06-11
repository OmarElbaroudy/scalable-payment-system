package persistence;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.lang3.SerializationUtils;
import org.rocksdb.*;
import persistence.models.Block;
import persistence.models.Transaction;
import persistence.models.UTXO;
import services.TransactionServices;

import java.io.Serializable;
import java.util.*;

public class RocksHandler {
    static {
        RocksDB.loadLibrary();
    }

    private final String id;
    private final RocksDB db;
    private final DBOptions options;
    private final ColumnFamilyOptions cfOpts;
    private final List<ColumnFamilyHandle> cfHandles;
    private final ColumnFamilyHandle committees;
    private final ColumnFamilyHandle utxos;

    public RocksHandler(String id) throws RocksDBException {
        String path = "/home/baroudy/Projects/Bachelor/payment-system";
        Dotenv dotenv = Dotenv.configure().directory(path).load();
        String dbPath = dotenv.get("ROCKSDB_PATH") + (this.id = id);
        cfOpts = new ColumnFamilyOptions().optimizeUniversalStyleCompaction();

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
    }

    private static byte[] serialize(Serializable obj) {
        return SerializationUtils.serialize(obj);
    }

    private static Object deserialize(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }

    public boolean assignKeyToCommittee(String pubKey, String committeeId) {
        try {
            db.put(committees, pubKey.getBytes(), committeeId.getBytes());
        } catch (RocksDBException e) {
            return false;
        }
        return true;
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

    public boolean addUTXOSet(String pubKey, HashSet<UTXO> set) {
        try {
            byte[] arr = serialize(set);
            db.put(utxos, pubKey.getBytes(), arr);
        } catch (RocksDBException e) {
            return false;
        }
        return true;
    }

    public HashSet<UTXO> getUTXOSet(String pubKey) {
        HashSet<UTXO> ret;
        try {
            byte[] arr = db.get(utxos, pubKey.getBytes());
            ret = arr == null ? new HashSet<>() : (HashSet<UTXO>) deserialize(arr);
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

    public void refresh(MongoHandler handler) {
        int idx = 1;
        Block b = handler.getBlock(idx);

        while (b != null) {
            HashMap<String, HashSet<UTXO>> mp = b.getUTXOS();
            for (Map.Entry<String, HashSet<UTXO>> e : mp.entrySet()) {
                HashSet<UTXO> st = this.getUTXOSet(e.getKey());
                st.addAll(e.getValue());
                this.addUTXOSet(e.getKey(), st);
            }

            mp = b.getSTXOS();
            for (Map.Entry<String, HashSet<UTXO>> e : mp.entrySet()) {
                HashSet<UTXO> st = this.getUTXOSet(e.getKey());
                st.removeAll(e.getValue());
                this.addUTXOSet(e.getKey(), st);
            }

            b = handler.getBlock(++idx);
        }
    }

    public void update(List<Transaction> ts){
        double rem = 0, val = 0;
        for(Transaction t : ts){
            rem += t.getOutput().getAmount();
            HashSet<UTXO> st = getUTXOSet(t.getOutput().getScriptPublicKey());
            st.add(t.getOutput());
            addUTXOSet(t.getOutput().getScriptPublicKey(), st);
        }

        if(!ts.get(0).getInput().isEmpty()){
            String pubKey = ts.get(0).getInput().get(0).getScriptPublicKey();
            HashSet<UTXO> st = getUTXOSet(pubKey);

            for(UTXO utxo : st){
                val += utxo.getAmount();
            }

            System.out.println("val and rem");
            System.out.println(val + " " + rem);

            st.clear();
            st.add(new UTXO(val - rem, pubKey));
            addUTXOSet(pubKey, st);
        }

    }

    public void update(Block b){
        List<Transaction> ts = b.getTransactions().getTransactions();
        HashMap<String, List<Transaction>> mp = new HashMap<>();

        TransactionServices.orderByPubKey(ts, mp);

        for(var e : mp.entrySet()){
            this.update(e.getValue());
        }

    }

    public void closeHandler() {
        for (ColumnFamilyHandle handle : cfHandles) {
            handle.close();
        }
        options.close();
        db.close();
    }
}
