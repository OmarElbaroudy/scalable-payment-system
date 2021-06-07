package persistence;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class MongoHandler extends MongoConnectionHandler {
    private String fieldName = "userId";

    public MongoHandler(String id) {
        super(id);
    }

    public void saveUser(User user) {
        this.getUsers().findOneAndDelete(eq(fieldName, user.getUserId()));
        this.getUsers().insertOne(user);
        System.out.println("user inserted successfully");
    }

    public void deleteUser(User user) {
        this.getUsers().findOneAndDelete(eq(fieldName, user.getUserId()));
        System.out.println("user deleted successfully");
    }

    public User getUser(String userName) {
        String fieldName = "userName";
        return this.getUsers().find(eq(fieldName, userName)).first();
    }

    public User findUser(String userName, String password) {
        return this.getUsers().find(and(eq(fieldName, userName), eq("password", password))).first();
    }
}
