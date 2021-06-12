package persistence;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import persistence.models.User;

import java.util.Collection;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class MongoHandler extends MongoConnectionHandler {
    private String fieldName = "userId";

    public MongoHandler() {
        super();
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
    public User getUserById(String userId){
        return this.getUsers().find(eq(fieldName,userId)).first();
    }

    public User findUser(String userName, String password) {
        return this.getUsers().find(and(eq("userName", userName), eq("password", password))).first();
    }

    public User findUser(String userName){
        return this.getUsers().find(eq("userName", userName)).first();
    }

    public boolean userExists(String userName) {
        return this.getUsers().find(eq(fieldName, userName)).first() != null;
    }
}
