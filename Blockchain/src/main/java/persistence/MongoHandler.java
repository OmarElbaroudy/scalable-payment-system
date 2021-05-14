package persistence;

import persistence.models.Block;

import static com.mongodb.client.model.Filters.eq;

//TODO index blocks using blockIndex for faster queries

public class MongoHandler extends MongoConnectionHandler {
    private final String fieldName = "metaData.blockIndex";

    public MongoHandler(){
        super();
    }

    public void saveBlock(Block block) {
        int idx = block.getMetaData().getBlockIndex();
        this.getBlockchain().findOneAndDelete(eq(fieldName, idx));
        this.getBlockchain().insertOne(block);
        System.out.println("block inserted successfully");
    }

    public void deleteBlock(int idx) {
        this.getBlockchain().findOneAndDelete(eq(fieldName, idx));
        System.out.println("block deleted successfully");
    }

    public Block getBlock(int idx) {
        String fieldName = "metaData.blockIndex";
        return this.getBlockchain().find(eq(fieldName, idx)).first();
    }
}
