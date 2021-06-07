import com.google.gson.JsonObject;
import com.rabbitmq.client.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.HashMap;
import java.util.Map;

public class API {
    private static String EXCHANGE_NAME;

    private static void setEXCHANGE_NAME() {
        String path = "/home/baroudy/Projects/Bachelor/payment-system/.env";
        Dotenv dotenv = Dotenv.configure().directory(path).load();
        EXCHANGE_NAME = dotenv.get("EXCHANGE_NAME");
    }

    public static void main(String[] args) throws Exception {
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
        jsonObject.addProperty("privKey", "88411c8f80dedcc9af37b7e243c24d0d0c05b235d6e9f63ca224fd056a54b346");
        jsonObject.addProperty("recKey", "62ffed2867dc8bddbb1e95152a9f24b603416dd1bcd2a685c7ca9c1ea0553ead0447d8d77dfaca3c0a074c7de571c6d03a664f43a98a3f828ba28933e36757d7");
        jsonObject.addProperty("amount", 1000);


        String json = jsonObject.toString();

        for (int i = 0; i < 10; i++){
            channel.basicPublish(EXCHANGE_NAME, "SIGNALING_SERVER", props, json.getBytes());
        }
    }
}
