package persistence;

import io.github.cdimascio.dotenv.Dotenv;
import org.rocksdb.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RocksHandler {
    static {
        RocksDB.loadLibrary();
    }

    private final RocksDB db;
    private final DBOptions options;
    private final ColumnFamilyOptions cfOpts;
    private final List<ColumnFamilyHandle> cfHandles;
    private final ColumnFamilyHandle committees;
    private final ColumnFamilyHandle distribution;

    public RocksHandler() throws RocksDBException {
        String path = "/home/baroudy/Projects/Bachelor/payment-system";
        Dotenv dotenv = Dotenv.configure().directory(path).load();
        String dbPath = dotenv.get("ROCKSDB_PATH") + "signalingServer";
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
        distribution = cfHandles.get(1);
    }


    public boolean assignNodeToCommittee(String nodeId, String committeeId) {
        try {
            db.put(committees, nodeId.getBytes(), committeeId.getBytes());
        } catch (RocksDBException e) {
            return false;
        }
        return true;
    }

    public String getCommitteeId(String nodeId) {
        String ret;
        try {
            ret = new String(db.get(committees, nodeId.getBytes()));
        } catch (RocksDBException e) {
            return null;
        }
        return ret;
    }

    public boolean removeNodeFromCommittee(String nodeId) {
        try {
            db.delete(committees, nodeId.getBytes());
        } catch (RocksDBException e) {
            return false;
        }
        return true;
    }

    public int getCommitteeSize(String committeeId){
        try {
            String s = new String(db.get(distribution, committeeId.getBytes()));
            return Integer.parseInt(s);
        } catch (RocksDBException e) {
            return 0;
        }
    }

    public boolean setCommitteeSize(String committeeId, int committeeSize){
        try {
            String sz = String.valueOf(committeeSize);
            db.put(distribution, committeeId.getBytes(), sz.getBytes());
        } catch (RocksDBException e) {
            return false;
        }
        return true;
    }

    public boolean removeCommittee(String committeeId){
        try {
            db.delete(distribution, committeeId.getBytes());
        } catch (RocksDBException e) {
            return false;
        }
        return true;
    }

    public boolean incCommitteeSize(String committeeId){
        int sz = getCommitteeSize(committeeId);
        return setCommitteeSize(committeeId, ++sz);
    }

    public boolean decCommitteeSize(String committeeId){
        int sz = getCommitteeSize(committeeId);
        return setCommitteeSize(committeeId, Math.max(0, --sz));
    }

    public void closeHandler() {
        for (ColumnFamilyHandle handle : cfHandles) {
            handle.close();
        }
        options.close();
        db.close();
    }
}
