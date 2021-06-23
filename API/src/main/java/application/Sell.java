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
import java.util.Objects;
import java.util.stream.Collectors;

public class Sell extends HttpServlet {
    private static MongoHandler handler;

    public static void setHandler(MongoHandler mongoHandler) {
        handler = mongoHandler;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            User user = handler.getUserById(req.getHeader("userId"));
            String privKey = user.getPrivKey();

            String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            JsonObject jsonObj = JsonParser.parseString(body).getAsJsonObject();

            double amount = jsonObj.get("amount").getAsDouble();
            String recKey = Objects.requireNonNull(System.getenv("GENESIS_PUBLIC_KEY"));

            JsonObject json = new JsonObject();
            json.addProperty("server", "API");
            json.addProperty("id", user.getUserId());
            json.addProperty("task", "sell");
            json.addProperty("amount", amount);

            API.log(json);
            API.createTransaction(resp, recKey, amount, privKey);
        }catch (Exception e){
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
