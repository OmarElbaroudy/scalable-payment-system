package application;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import persistence.MongoHandler;
import persistence.models.User;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Collectors;

public class Sell extends HttpServlet {
    private static MongoHandler handler;

    public static void setHandler(MongoHandler mongoHandler) {
        handler = mongoHandler;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = handler.getUserById(req.getHeader("userId"));
        String privKey = user.getPrivKey();

        String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        JsonObject jsonObj = JsonParser.parseString(body).getAsJsonObject();

        double amount = jsonObj.get("amount").getAsDouble();

        String path = "/home/baroudy/Projects/Bachelor/payment-system";
        Dotenv dotenv = Dotenv.configure().directory(path).load();
        String recKey = Objects.requireNonNull(dotenv.get("GENESIS_PUBLIC_KEY"));

        API.createTransaction(resp, recKey, amount, privKey);
    }
}
