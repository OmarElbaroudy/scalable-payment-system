package persistence.models;

import java.util.Date;
import java.util.Objects;

public class MetaData {
    private int blockIndex;
    private String previousBlockHash;
    private long timestamp;
    private int nonce;
    private int difficulty;
    private String merkleRootHash;

    public MetaData(int idx, String prevHash, int nonce, int difficulty) {
        this.blockIndex = idx;
        this.previousBlockHash = prevHash;
        this.nonce = nonce;
        this.difficulty = difficulty;
        this.timestamp = new Date().getTime();
        if(idx == 1){
            this.timestamp = 0L;
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaData metaData = (MetaData) o;
        return blockIndex == metaData.blockIndex &&
                timestamp == metaData.timestamp &&
                nonce == metaData.nonce &&
                difficulty == metaData.difficulty &&
                Objects.equals(previousBlockHash, metaData.previousBlockHash) &&
                Objects.equals(merkleRootHash, metaData.merkleRootHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockIndex, previousBlockHash, timestamp, nonce, difficulty, merkleRootHash);
    }

    @Override
    public String toString() {
        return "MetaData{" +
                "blockIndex=" + blockIndex +
                ", previousBlockHash='" + previousBlockHash + '\'' +
                ", timestamp=" + timestamp +
                ", nonce=" + nonce +
                ", difficulty=" + difficulty +
                ", merkleRootHash='" + merkleRootHash + '\'' +
                '}';
    }
}
