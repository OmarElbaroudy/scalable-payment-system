package services;

import persistence.RocksHandler;

import java.util.HashSet;

public class CoordinationServices {

    private final HashSet<String> validatedBlocks;
    private final int totalNumberOfNodes;
    private final RocksHandler handler;
    private int committeeNodes;

    public CoordinationServices(RocksHandler handler) {
        committeeNodes = 0;
        this.handler = handler;
        validatedBlocks = new HashSet<>();
        totalNumberOfNodes = handler.getNumberOfNodes();
    }

    public String getRandomNodeId(String committeeId) {
        return handler.getRandomNodeInCommittee(committeeId);
    }

    public boolean isMining(String nodeId, boolean isCommittee) {
        if (!isCommittee) {
            validatedBlocks.add(nodeId);
        } else {
            if(!validatedBlocks.contains(nodeId)){
                validatedBlocks.add(nodeId);
                committeeNodes += handler.getCommitteeSize(nodeId);
            }
        }

        if (validatedBlocks.size() + committeeNodes < totalNumberOfNodes)
            return true;

        committeeNodes = 0;
        validatedBlocks.clear();
        return false;
    }


}
