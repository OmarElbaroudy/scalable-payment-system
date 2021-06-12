package persistence;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import persistence.models.User;

import java.util.Objects;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


public class MongoConnectionHandler {
    private final MongoClient mc;
    private final MongoDatabase db;
    private final MongoCollection<User> users;

    public MongoConnectionHandler() {
        this.mc = MongoClients.create(getMongoClientSettings());
        this.db = mc.getDatabase("API");
        this.users = this.db.getCollection("User", User.class);
        System.out.printf("connected to db %s successfully %n", "API");
    }

    /**
     * @return mongodb uri connection String
     */
    private ConnectionString getConnectionString() {
        String path = "/home/baroudy/Projects/Bachelor/payment-system";
        Dotenv dotenv = Dotenv.configure().directory(path).load();
        return new ConnectionString(Objects.requireNonNull(dotenv.get("MONGODB_URI")));
    }

    /**
     * @return client setting for mapping pojo models to documents
     */
    private MongoClientSettings getMongoClientSettings() {
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


    public MongoCollection<User> getUsers() {
        return users;
    }


    /**
     * used to close connection after mongo client is used
     */
    public void closeConnection() {
        mc.close();
    }
}
