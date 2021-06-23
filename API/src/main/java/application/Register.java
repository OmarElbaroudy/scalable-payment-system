package application;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import persistence.MongoHandler;
import org.web3j.crypto.Keys;
import org.web3j.crypto.ECKeyPair;
import persistence.models.User;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.stream.Collectors;

public class Register extends HttpServlet {
    private static MongoHandler handler;

    public Register() {
    }

    public static void setHandler(MongoHandler Mongohandler) {
        handler = Mongohandler;
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();

            String userName = json.get("userName").getAsString();
            String password = json.get("password").getAsString();

            String userId = UUID.randomUUID().toString();
            ECKeyPair pair = Keys.createEcKeyPair();

            String privKey = pair.getPrivateKey().toString(16);
            String pubKey = pair.getPublicKey().toString(16);

            User newUser = new User(userName, userId, password, pubKey, privKey);

            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_OK);

            json = new JsonObject();

            if(handler.userExists(userName)){
                json.addProperty("message", "username already exists");

            }else{
                json.addProperty("message", "you are now registered!");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("server", "API");
                jsonObject.addProperty("id", newUser.getUserId());
                jsonObject.addProperty("task", "register");
                handler.saveUser(newUser);
            }

            PrintWriter out = resp.getWriter();
            out.print(json);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
