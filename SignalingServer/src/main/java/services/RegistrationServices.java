package services;

import io.github.cdimascio.dotenv.Dotenv;
import persistence.RocksHandler;

import java.util.Objects;
import java.util.UUID;

public class RegistrationServices {

    public static String generateNodeId() {
        return UUID.randomUUID().toString();
    }

    public static String getPrimaryQueue() {
        String path = "C:\\Users\\ahmed\\Documents\\GitHub\\payment-system";
        Dotenv dotenv = Dotenv.configure().directory(path).load();

        return dotenv.get("PRIMARY_QUEUE_ID");

    }

    public static String getCommitteeQueue(String nodeId, RocksHandler handler) {
        String path = "C:\\Users\\ahmed\\Documents\\GitHub\\payment-system";
        Dotenv dotenv = Dotenv.configure().directory(path).load();
        int committeeSize = Integer.parseInt(Objects.requireNonNull(dotenv.get("COMMITTEE_SIZE")));
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
    public static void deregister(String nodeId,RocksHandler handler){
        String committeeId = handler.getCommitteeId(nodeId);
        handler.removeNodeFromCommittee(nodeId);
        handler.decCommitteeSize(committeeId);
    }
}
