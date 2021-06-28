package services;

import persistence.RocksHandler;

import java.util.Objects;

public class RegistrationServices {
    private static int cur = 1;

    public static String generateNodeId() {
//        return UUID.randomUUID().toString();
        return String.valueOf(cur++);
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

    public static void deregister(String nodeId, RocksHandler handler) {
        String committeeId = handler.getCommitteeId(nodeId);
        handler.removeNodeFromCommittee(nodeId);
        handler.decCommitteeSize(committeeId);
    }
}
