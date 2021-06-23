package services;

import persistence.RocksHandler;

import java.util.Objects;
import java.util.UUID;

public class RegistrationServices {

    public static String generateNodeId() {
        return UUID.randomUUID().toString();
    }

    public static String getPrimaryQueue() {
        return System.getenv("PRIMARY_QUEUE_ID");

    }

    public static String getCommitteeQueue(String nodeId, RocksHandler handler) {
        int committeeSize = Integer.parseInt(Objects.requireNonNull(System.getenv("COMMITTEE_SIZE")));
        int committeeId = 1;
        while (true) {
            int currentSize = handler.getCommitteeSize(String.valueOf(committeeId));
            if (currentSize < committeeSize) {
                handler.assignNodeToCommittee(nodeId, String.valueOf(committeeId));
                handler.incCommitteeSize(String.valueOf(committeeId));
                return String.valueOf(committeeId);
            }
            committeeId++;
        }
    }

    public static String[] getParentNodeId(String nodeId, RocksHandler handler) {
        String[] ret = new String[2];
        String committeeId = handler.getCommitteeId(nodeId);

        if (handler.getCommitteeSize(committeeId) > 1) {
            ret[1] = "SAME_COMMITTEE";
            ret[0] = handler.getParentInSameCommittee(nodeId);
            return ret;
        }

        ret[1] = "DIFFERENT_COMMITTEE";
        ret[0] = handler.getParentInDiffCommittee(nodeId);
        return ret;
    }

    public static void deregister(String nodeId, RocksHandler handler) {
        String committeeId = handler.getCommitteeId(nodeId);
        handler.removeNodeFromCommittee(nodeId);
        handler.decCommitteeSize(committeeId);
    }
}
