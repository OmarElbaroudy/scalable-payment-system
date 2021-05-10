package persistence;

import org.apache.commons.lang3.SerializationUtils;
import io.github.cdimascio.dotenv.Dotenv;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.Serializable;
import java.util.UUID;

public class RocksHandler {
    private final String dbPath;
    private final RocksDB db;
    private final Options options;

    static {
        RocksDB.loadLibrary();
    }

    public RocksHandler() throws RocksDBException {
        String path = "/home/baroudy/Projects/Bachelor/payment-system";
        Dotenv dotenv = Dotenv.configure().directory(path).load();
        dbPath = dotenv.get("ROCKSDB_PATH") + UUID.randomUUID();
        options = new Options().setCreateIfMissing(true);
        db = RocksDB.open(options, dbPath);
    }

    public boolean put(String pubKey, String committeeId){
        try {
            db.put(pubKey.getBytes(), committeeId.getBytes());
        }catch (RocksDBException e){
            return false;
        }
        return true;
    }

    public String get(String pubKey){
        String ret;
        try {
            ret = new String(db.get(pubKey.getBytes()));
        }catch (RocksDBException e){
            return null;
        }
        return ret;
    }

    public boolean delete(String pubKey){
        try{
            db.delete(pubKey.getBytes());
        }catch (RocksDBException e){
            return false;
        }
        return true;
    }

    public void closeHandler(){
        options.close();
        db.close();
    }

    private static byte[] serialize(Serializable obj) {
        return SerializationUtils.serialize(obj);
    }

    private static Object deserialize(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }
}
