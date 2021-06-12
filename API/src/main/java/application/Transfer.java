package application;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import persistence.MongoHandler;
import persistence.models.User;

import java.io.IOException;
import java.util.stream.Collectors;

public class Transfer extends HttpServlet {
    private static MongoHandler handler;


    public static void setHandler(MongoHandler mongoHandler) {
        handler = mongoHandler;
    }


    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            User user = handler.getUserById(req.getHeader("userId"));
            String privKey = user.getPrivKey();

            String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            JsonObject jsonObj = JsonParser.parseString(body).getAsJsonObject();

            String userName = jsonObj.get("userName").getAsString();
            double amount = jsonObj.get("amount").getAsDouble();

            User receiver = handler.getUser(userName);
            String recKey = receiver.getPubKey();

            API.createTransaction(resp, recKey, amount, privKey);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }
}
