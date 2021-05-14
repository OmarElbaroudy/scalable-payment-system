package persistence.models;

import utilities.MerkelTree;

public class Block {
    private MerkelTree transactions;
    private MetaData metaData;

    public Block(MetaData metaData, MerkelTree transactions) {
        this.metaData = metaData;
        this.transactions = transactions;
        metaData.setMerkleRootHash(transactions.getRoot());
    }

    public Block() {
    }


    public int getIdx(){
        return this.metaData.getBlockIndex();
    }

    public MerkelTree getTransactions() {
        return transactions;
    }

    public void setTransactions(MerkelTree transactions) {
        this.transactions = transactions;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }
}
