package application;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.*;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.ResponseBody;
import persistence.MongoHandler;
import persistence.RocksHandler;
import persistence.models.Block;
import services.BlockServices;
import services.NodeServices;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class Node {
    private final String BASE_URL = "http://localhost:5000";
    private final String exchangeName = "BLOCKCHAIN";
    private final Gson gson;
    private final String nodeId;
    private final Channel channel;
    private final String parentId;
    private final String parentType;
    private final String primaryQueue;
    private final NodeServices server;
    private final RocksHandler rocksHandler;
    private final MongoHandler mongoHandler;

    public Node() throws Exception {
        gson = new Gson();
        OkHttpClient client = new OkHttpClient();
        Request req = new Request.Builder().url(BASE_URL + "/register").build();
        ResponseBody resBody = client.newCall(req).execute().body();
        JsonObject json = JsonParser.parseString(resBody.string()).getAsJsonObject();

        nodeId = json.get("nodeId").getAsString();
        parentId = json.get("parentId").getAsString();
        parentType = json.get("parentType").getAsString();
        primaryQueue = json.get("primaryQueue").getAsString();
        String committeeQueue = json.get("committeeQueue").getAsString();

        System.out.println("node id is " + nodeId);
        System.out.println("committeeQueue is " + committeeQueue);
        System.out.println("primaryQueue is " + primaryQueue);

        rocksHandler = new RocksHandler(nodeId);
        mongoHandler = new MongoHandler(nodeId);

        ConnectionFactory factory = new ConnectionFactory();
        Connection connection = factory.newConnection();
        channel = connection.createChannel();

        channel.queueDeclare(nodeId, false, false, true, null);
        channel.queueBind(nodeId, exchangeName, committeeQueue);
        channel.queueBind(nodeId, exchangeName, primaryQueue);
        channel.queueBind(nodeId, exchangeName, nodeId);

        server = new NodeServices.Builder(nodeId).
                channel(channel).
                exchangeName(exchangeName).
                committeeQueue(committeeQueue).
                primaryQueue(primaryQueue).
                mongoHandler(mongoHandler).
                rocksHandler(rocksHandler).build();


        initChain();
        start();
    }

    public static void main(String[] args) throws Exception {
        new Node();
    }

    private void initChain() throws Exception {
        switch (parentType) {
            case "nil" -> {
                System.out.println("generating genesis block");
                BlockServices.generateGenesis(mongoHandler, rocksHandler, true);
            }

            case "SAME_COMMITTEE" -> {
                System.out.println("same committee parent id is " + parentId);
                Map<String, Object> mp = new HashMap<>();
                mp.put("task", "sendGenesis");
                mp.put("senderId", nodeId);

                AMQP.BasicProperties props =
                        new AMQP.BasicProperties().
                                builder().
                                headers(mp).
                                contentType("application/json").
                                build();

                channel.basicPublish(exchangeName, parentId, props, null);
            }

            case "DIFFERENT_COMMITTEE" -> {
                System.out.println("generating genesis block");
                Block b = BlockServices.generateGenesis(mongoHandler, rocksHandler, false);
                String json = gson.toJson(b);

                Map<String, Object> mp = new HashMap<>();
                mp.put("task", "updateUTXO");

                AMQP.BasicProperties props =
                        new AMQP.BasicProperties().
                                builder().
                                headers(mp).
                                contentType("application/json").
                                build();

                System.out.println("sending update request");
                channel.basicPublish(exchangeName, primaryQueue, props, json.getBytes());
            }
        }
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

                        LongString snd = (LongString) properties.getHeaders().get("senderId");
                        String senderId = snd == null ? null : new String(snd.getBytes());

                        try {
                            server.exec(task, senderId, body);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        channel.basicAck(deliveryTag, false);
                    }
                });
    }
}
