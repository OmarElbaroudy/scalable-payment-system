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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

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
    private final UUID nodeId = UUID.randomUUID();
    private final MongoClient mc;
    private final MongoDatabase db;
    private final MongoCollection<Block> blockchain;


    /**
     * @throws IOException happens when cannot load data from properties file
     */
    public MongoConnectionHandler() throws IOException {
        this.mc = MongoClients.create(getMongoClientSettings());
        this.db = mc.getDatabase(nodeId.toString());
        this.blockchain = this.db.getCollection("Block", Block.class);
        System.out.printf("connected to db %s successfully %n", nodeId);
    }

    /**
     * @return mongodb uri connection String
     * @throws IOException happens when the props file is not found
     */
    private ConnectionString getConnectionString() throws IOException {
        try (FileInputStream f = new FileInputStream("/home/baroudy/Projects/Bachelor/payment-system/Blockchain/src/main/java/config/props.properties")) {
            Properties props = new Properties();
            props.load(f);
            return new ConnectionString(props.getProperty("mongodb.uri"));
        }
    }

    /**
     * @return client setting for mapping pojo models to documents
     * @throws IOException when connection string is invalid
     */
    private MongoClientSettings getMongoClientSettings() throws IOException {
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
