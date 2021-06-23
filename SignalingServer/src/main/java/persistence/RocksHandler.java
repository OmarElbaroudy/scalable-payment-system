package persistence;

import org.rocksdb.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class RocksHandler {
    static {
        RocksDB.loadLibrary();
    }

    private final RocksDB db;
    private final DBOptions options;
    private final List<ColumnFamilyHandle> cfHandles;
    private final ColumnFamilyHandle committees;
    private final ColumnFamilyHandle distribution;

    public RocksHandler() throws RocksDBException {
        String dbPath = System.getenv("ROCKSDB_PATH") + "signalingServer";
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

    public int getCommitteeSize(String committeeId) {
        try {
            String s = new String(db.get(distribution, committeeId.getBytes()));
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean setCommitteeSize(String committeeId, int committeeSize) {
        try {
            String sz = String.valueOf(committeeSize);
            db.put(distribution, committeeId.getBytes(), sz.getBytes());
        } catch (RocksDBException e) {
            return false;
        }
        return true;
    }

    public boolean removeCommitteeSize(String committeeId) {
        try {
            db.delete(distribution, committeeId.getBytes());
        } catch (RocksDBException e) {
            return false;
        }
        return true;
    }

    public boolean incCommitteeSize(String committeeId) {
        int sz = getCommitteeSize(committeeId);
        return setCommitteeSize(committeeId, ++sz);
    }

    public boolean decCommitteeSize(String committeeId) {
        int sz = getCommitteeSize(committeeId);
        return setCommitteeSize(committeeId, Math.max(0, --sz));
    }

    public String getRandomNodeInCommittee(String committeeId) {
        int sz = getCommitteeSize(committeeId), cur = 0;
        int randomIdx = (int) (Math.random() * sz) + 1;

        RocksIterator rocksIterator = db.newIterator(committees);
        rocksIterator.seekToFirst();

        while (rocksIterator.isValid()) {
            if (new String(rocksIterator.value()).equals(committeeId)) {
                cur++;
                if (cur == randomIdx) {
                    return new String(rocksIterator.key());
                }
            }

            rocksIterator.next();
        }

        return "";
    }

    public int getNumberOfCommittees() {
        int max = 0;

        RocksIterator rocksIterator = db.newIterator(distribution);
        rocksIterator.seekToFirst();

        while (rocksIterator.isValid()) {
            int cur = Integer.parseInt(new String(rocksIterator.key()));
            max = Math.max(max, cur);
            rocksIterator.next();
        }

        return max;
    }

    public int getNumberOfNodes(){
        RocksIterator rocksIterator = db.newIterator(distribution);
        rocksIterator.seekToFirst();
        int cnt = 0;
        while (rocksIterator.isValid()){
            cnt += Integer.parseInt(new String(rocksIterator.value()));
            rocksIterator.next();
        }
        return cnt;
    }

    public void closeHandler() {
        for (ColumnFamilyHandle handle : cfHandles) {
            handle.close();
        }
        options.close();
        db.close();
    }

    public String getParentInSameCommittee(String nodeId) {
        String committeeId = getCommitteeId(nodeId);
        HashSet<String> st = new HashSet<>();
        RocksIterator rocksIterator = db.newIterator(committees);
        rocksIterator.seekToFirst();

        while (rocksIterator.isValid()) {
            if (new String(rocksIterator.value()).equals(committeeId)) {
                st.add(new String(rocksIterator.key()));
            }
            rocksIterator.next();
        }

        st.remove(nodeId);
        int randomIdx = (int) (Math.random() * st.size());
        return (String) st.toArray()[randomIdx];
    }

    public String getParentInDiffCommittee(String nodeId) {
        HashSet<String> st = new HashSet<>();
        RocksIterator rocksIterator = db.newIterator(committees);
        rocksIterator.seekToFirst();

        while (rocksIterator.isValid()) {
            st.add(new String(rocksIterator.key()));
            rocksIterator.next();
        }

        st.remove(nodeId);
        if(st.isEmpty()){
            return "nil";
        }

        int randomIdx = (int) (Math.random() * st.size());
        return (String) st.toArray()[randomIdx];
    }
}
