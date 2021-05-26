package services;

import io.github.cdimascio.dotenv.Dotenv;
import persistence.RocksHandler;

import java.util.HashMap;
import java.util.Objects;

public class CoordinationServices {

    private final HashMap<String, Integer> validatedBlocks;
    private final RocksHandler handler;

    public CoordinationServices(RocksHandler handler) {
        validatedBlocks = new HashMap<>();
        this.handler = handler;
    }

    public String getRandomNodeId(String committeeId) {
        return handler.getRandomNodeInCommittee(committeeId);
    }

    public boolean endBlockValidationPhase(String nodeId) {
        String committeeId = handler.getCommitteeId(nodeId);
        int cnt = validatedBlocks.getOrDefault(committeeId, 0);
        validatedBlocks.put(committeeId, ++cnt);
        int sz = handler.getNumberOfCommittees();

        if (validatedBlocks.size() < sz) return false;

        String path = "/home/baroudy/Projects/Bachelor/payment-system/.env";
        Dotenv dotenv = Dotenv.configure().directory(path).load();
        int committeeSize = Integer.parseInt(Objects.requireNonNull(dotenv.get("COMMITTEE_SIZE")));

        for (int size : validatedBlocks.values()) {
            if (size < committeeSize) return false;
        }

        validatedBlocks.clear();
        return true;
    }


}
