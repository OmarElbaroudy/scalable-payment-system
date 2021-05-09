package persistence.models;

import java.util.Date;

public class MetaData {
    private int blockIndex;
    private String previousBlockHash;
    private Long timestamp;
    private int nonce;
    private int difficulty;
    private String merkleRootHash;

    public MetaData(int idx, String prevHash, int nonce, int difficulty) {
        this.blockIndex = idx;
        this.previousBlockHash = prevHash;
        this.nonce = nonce;
        this.difficulty = difficulty;
        this.timestamp = new Date().getTime();
    }

    public MetaData() {
    }

    public int getBlockIndex() {
        return blockIndex;
    }

    public void setBlockIndex(int blockIndex) {
        this.blockIndex = blockIndex;
    }

    public String getPreviousBlockHash() {
        return previousBlockHash;
    }

    public void setPreviousBlockHash(String previousBlockHash) {
        this.previousBlockHash = previousBlockHash;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int nBits) {
        this.difficulty = nBits;
    }

    public String getMerkleRootHash() {
        return merkleRootHash;
    }

    public void setMerkleRootHash(String merkleRootHash) {
        this.merkleRootHash = merkleRootHash;
    }
}
