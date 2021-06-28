package application;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.*;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.ResponseBody;
import persistence.MongoHandler;
import persistence.RocksHandler;
import services.NodeServices;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class Node {
    private final String nodeId;
    private final Channel channel;
    private final NodeServices server;

    public Node() throws Exception {
        OkHttpClient client = new OkHttpClient();
        String BASE_URL = "http://localhost:5000";
        Request req = new Request.Builder().url(BASE_URL + "/register").build();
        ResponseBody resBody = client.newCall(req).execute().body();
        JsonObject json = JsonParser.parseString(resBody.string()).getAsJsonObject();

        nodeId = json.get("nodeId").getAsString();
        String primaryQueue = json.get("primaryQueue").getAsString();
        String committeeQueue = json.get("committeeQueue").getAsString();

        System.out.println("node id is " + nodeId);
        System.out.println("committeeQueue is " + committeeQueue);
        System.out.println("primaryQueue is " + primaryQueue);

        RocksHandler rocksHandler = new RocksHandler(nodeId);
        MongoHandler mongoHandler = new MongoHandler(nodeId);

        ConnectionFactory factory = new ConnectionFactory();
        Connection connection = factory.newConnection();
        channel = connection.createChannel();

        channel.queueDeclare(nodeId, false, false, true, null);

        String exchangeName = "BLOCKCHAIN";
        channel.queueBind(nodeId, exchangeName, committeeQueue);
        channel.queueBind(nodeId, exchangeName, primaryQueue);

        server = new NodeServices.Builder(nodeId).
                channel(channel).
                exchangeName(exchangeName).
                committeeQueue(committeeQueue).
                primaryQueue(primaryQueue).
                mongoHandler(mongoHandler).
                rocksHandler(rocksHandler).build();

        start();
        signalSuccessfulRegister();
    }

    private void signalSuccessfulRegister() throws Exception {
        Map<String, Object> mp = new HashMap<>();
        mp.put("task", "nodeRegistered");

        AMQP.BasicProperties props =
                new AMQP.BasicProperties().
                        builder().
                        headers(mp).
                        contentType("application/json").
                        build();

        channel.basicPublish("", "SIGNALING_SERVER", props, null);
    }

    public static void main(String[] args) throws Exception {
        new Node();
    }

    private void start() throws Exception {
        channel.basicConsume(nodeId, false, nodeId,
                new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag,
                                               Envelope envelope,
                                               AMQP.BasicProperties properties,
                                               byte[] body)
                            throws IOException {

                        long deliveryTag = envelope.getDeliveryTag();
                        LongString fst = (LongString) properties.getHeaders().get("task");
                        String task = new String(fst.getBytes());

                        try {
                            server.exec(task, body);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        channel.basicAck(deliveryTag, false);
                    }
                });
    }


}
