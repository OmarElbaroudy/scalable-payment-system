import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class API {
    private static final String exchangeName = "BLOCKCHAIN";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare("API", false, false, true, null);
        channel.queueBind("API", exchangeName, "API");

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
        jsonObject.addProperty("amount", 5);


        String json = jsonObject.toString();

        for (int i = 0; i < 25; i++) {
            channel.basicPublish(exchangeName, "SIGNALING_SERVER", props, json.getBytes());
        }

        mp.put("task", "getBalance");

        props = new AMQP.BasicProperties().
                builder().
                headers(mp).
                contentType("application/json").
                build();

        jsonObject = new JsonObject();
        jsonObject.addProperty("pubKey", "62ffed2867dc8bddbb1e95152a9f24b603416dd1bcd2a685c7ca9c1ea0553ead0447d8d77dfaca3c0a074c7de571c6d03a664f43a98a3f828ba28933e36757d7");
        json = jsonObject.toString();

        channel.basicPublish(exchangeName, "SIGNALING_SERVER", props, json.getBytes());

        jsonObject.addProperty("pubKey", "dc26e78e63ca3741c0d77c46be3369c4e40a9a156206b95f26fd7464f88e37717d195e21f5f7d16a6d2a9bac346d8919f3fc600ed35aad8269cc8d4a2546f28e");
        json = jsonObject.toString();

        channel.basicPublish(exchangeName, "SIGNALING_SERVER", props, json.getBytes());


        channel.basicConsume("API", false, "API",
                new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag,
                                               Envelope envelope,
                                               AMQP.BasicProperties properties,
                                               byte[] body)
                            throws IOException {

                        long deliveryTag = envelope.getDeliveryTag();

                        LongString ls = (LongString) properties.getHeaders().get("pubKey");
                        String pubKey = new String(ls.getBytes());

                        String s = new String(body);
                        JsonObject json = JsonParser.parseString(s).getAsJsonObject();
                        double amount = json.get("amount").getAsDouble();

                        System.out.println("amount is " + amount);
                        System.out.println(pubKey);


                        channel.basicAck(deliveryTag, false);
                    }
                });

    }
}
