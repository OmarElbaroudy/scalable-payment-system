package persistence;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import persistence.models.Block;

import java.util.Objects;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

//TODO create indexing on index of blocks in blockchain

/**
 * an object of mongoConnectionHandler should be only once for each node
 * then closed when the program terminates.
 * <p>
 * nodes are given a unique UUID which is used to create a unique database
 */
public class MongoConnectionHandler {
    private final String nodeId;
    private final MongoClient mc;
    private final MongoDatabase db;
    private final MongoCollection<Block> blockchain;

    public MongoConnectionHandler(String id){
        this.nodeId = id;
        this.mc = MongoClients.create(getMongoClientSettings());
        this.db = mc.getDatabase(nodeId);
        this.blockchain = this.db.getCollection("Block", Block.class);
        System.out.printf("connected to db %s successfully %n", nodeId);
    }

    /**
     * @return mongodb uri connection String
     */
    private ConnectionString getConnectionString(){
        return new ConnectionString(Objects.requireNonNull(System.getenv("MONGODB_URI")));
    }

    /**
     * @return client setting for mapping pojo models to documents
     */
    private MongoClientSettings getMongoClientSettings(){
        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
        return MongoClientSettings.builder()
                .applyConnectionString(getConnectionString())
                .codecRegistry(codecRegistry)
                .build();
    }

    /**
     * @return mongo client session
     */
    public MongoClient getMc() {
        return mc;
    }


    /**
     * @return unique data base for this mongo client
     */
    public MongoDatabase getDb() {
        return db;
    }


    /**
     * @return blockchain collection
     */
    public MongoCollection<Block> getBlockchain() {
        return blockchain;
    }


    /**
     * used to close connection after mongo client is used
     */
    public void closeConnection() {
        mc.close();
    }
}
