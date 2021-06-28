package services;

import persistence.RocksHandler;

import java.util.HashMap;
import java.util.HashSet;

public class CoordinationServices {

    private static int totalNumberOfNodes;
    private static int totalNumberOfCommittees;
    private final HashMap<String, Integer> validatedBlocks;
    private final HashSet<String> validatedCommittees;
    private final RocksHandler handler;

    public CoordinationServices(RocksHandler handler) {
        this.handler = handler;
        validatedBlocks = new HashMap<>();
        validatedCommittees = new HashSet<>();
    }

    public static void setTotalNumberOfNodes(int totalNumberOfNodes) {
        CoordinationServices.totalNumberOfNodes = totalNumberOfNodes;
    }

    public static void setTotalNumberOfCommittees(int totalNumberOfCommittees) {
        CoordinationServices.totalNumberOfCommittees = totalNumberOfCommittees;
    }

    public String getRandomNodeId(String committeeId) {
        return handler.getRandomNodeInCommittee(committeeId);
    }

    public boolean isMining(String nodeId, boolean isCommittee) {
        if (!isCommittee) {
            int cnt = validatedBlocks.getOrDefault(nodeId, 0) + 1;
            validatedBlocks.put(nodeId, cnt);
        } else {
            validatedCommittees.add(nodeId);
        }

        int cnt = 0;
        for (int x : validatedBlocks.values()) cnt += x;
        cnt += validatedCommittees.size() * totalNumberOfNodes;
        if (cnt < totalNumberOfNodes * totalNumberOfCommittees) return true;

        validatedBlocks.clear();
        validatedCommittees.clear();
        return false;
    }
}
