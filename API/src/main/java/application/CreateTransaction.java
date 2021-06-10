package application;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import persistence.MongoHandler;
import persistence.User;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateTransaction extends HttpServlet {
    private static MongoHandler handler;
    private static String EXCHANGE_NAME;

    public static void setHandler(MongoHandler mongoHandler) {
        handler = mongoHandler;
    }

    private static void setEXCHANGE_NAME() {
        String path = "C://Users//ahmed//Documents//GitHub//payment-system//.env";
        Dotenv dotenv = Dotenv.configure().directory(path).load();
        EXCHANGE_NAME = dotenv.get("EXCHANGE_NAME");
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

            setEXCHANGE_NAME();

            ConnectionFactory factory = new ConnectionFactory();
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            Map<String, Object> mp = new HashMap<>();
            mp.put("task", "createTransaction");

            AMQP.BasicProperties props =
                    new AMQP.BasicProperties().
                            builder().
                            headers(mp).
                            contentType("application/json").
                            build();

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("privKey", privKey);
            jsonObject.addProperty("recKey", recKey);
            jsonObject.addProperty("amount", amount);

            String json = jsonObject.toString();

            channel.basicPublish(EXCHANGE_NAME, "SIGNALING_SERVER", props, json.getBytes());

            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_OK);

            PrintWriter out = resp.getWriter();
            out.print("transaction created!");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }
}
