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
import persistence.User;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
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
            handler.saveUser(newUser);

            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_OK);

            PrintWriter out = resp.getWriter();
            out.print("you are now registered!");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
