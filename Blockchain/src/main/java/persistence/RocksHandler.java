package persistence;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.lang3.SerializationUtils;
import org.rocksdb.*;
import persistence.models.UTXO;

import java.io.Serializable;
import java.util.*;

public class RocksHandler {
    static {
        RocksDB.loadLibrary();
    }

    private final RocksDB db;
    private final DBOptions options;
    private final ColumnFamilyOptions cfOpts;
    private final List<ColumnFamilyHandle> cfHandles;
    private final ColumnFamilyHandle committees;
    private final ColumnFamilyHandle utxos;

    public RocksHandler() throws RocksDBException {
        String path = "/home/baroudy/Projects/Bachelor/payment-system";
        Dotenv dotenv = Dotenv.configure().directory(path).load();
        String dbPath = dotenv.get("ROCKSDB_PATH") + UUID.randomUUID();
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
        try{
            byte[] arr = serialize(set);
            db.put(utxos,pubKey.getBytes(), arr);
        }catch (RocksDBException e){
            return false;
        }
        return true;
    }

    public HashSet<UTXO> getUTXOSet(String pubKey){
        HashSet<UTXO> ret;
        try{
            byte[] arr = db.get(utxos, pubKey.getBytes());
            ret = (HashSet<UTXO>) deserialize(arr);
        }catch (RocksDBException e){
            return null;
        }
        return ret;
    }

    public boolean removeUTXOSet(String pubKey){
        try{
            db.delete(utxos, pubKey.getBytes());
        }catch (RocksDBException e){
            return false;
        }
        return true;
    }

    public void closeHandler() {
        for (ColumnFamilyHandle handle : cfHandles) {
            handle.close();
        }
        options.close();
        db.close();
    }
}
