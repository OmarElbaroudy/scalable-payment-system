package application;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.*;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.ResponseBody;
import io.github.cdimascio.dotenv.Dotenv;
import org.web3j.crypto.ECKeyPair;
import persistence.MongoHandler;
import persistence.RocksHandler;
import persistence.models.Block;
import persistence.models.Transaction;
import services.BlockServices;
import services.TransactionServices;
import utilities.Sign;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Node {
    private final String BASE_URL = "http://localhost:5000";
    private final Gson gson;
    private final String nodeId;
    private final Channel channel;
    private final OkHttpClient client;
    private final String primaryQueue;
    private final Connection connection;
    private final String committeeQueue;
    private final RocksHandler rocksHandler;
    private final MongoHandler mongoHandler;
    private final List<Transaction> transactions;
    private String EXCHANGE_NAME;

    public Node() throws Exception {
        client = new OkHttpClient();
        gson = new Gson();
        Request req = new Request.Builder().url(BASE_URL + "/register").build();
        ResponseBody resBody = client.newCall(req).execute().body();
        JsonObject json = JsonParser.parseString(resBody.string()).getAsJsonObject();

        nodeId = json.get("nodeId").getAsString();
        committeeQueue = json.get("committeeQueue").getAsString();
        primaryQueue = json.get("primaryQueue").getAsString();

        System.out.println("node id is " + nodeId);
        System.out.println("committeeQueue is " + committeeQueue);
        System.out.println("primaryQueue is " + primaryQueue);

        rocksHandler = new RocksHandler(nodeId);
        mongoHandler = new MongoHandler(nodeId);

        setEXCHANGE_NAME();
        ConnectionFactory factory = new ConnectionFactory();
        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.queueDeclare(nodeId, false, false, true, null);
        channel.queueBind(nodeId, EXCHANGE_NAME, committeeQueue);
        channel.queueBind(nodeId, EXCHANGE_NAME, primaryQueue);
        channel.queueBind(nodeId, EXCHANGE_NAME, nodeId);

        transactions = new ArrayList<>();

        start();
    }

    public static void main(String[] args) throws Exception {
        new Node();
    }

    private void start() throws Exception {
        BlockServices.generateGenesis(mongoHandler, rocksHandler);

        channel.basicConsume(nodeId, false, nodeId,
                new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag,
                                               Envelope envelope,
                                               AMQP.BasicProperties properties,
                                               byte[] body)
                            throws IOException {

                        long deliveryTag = envelope.getDeliveryTag();
                        LongString ls = (LongString) properties.getHeaders().get("task");
                        String task = new String(ls.getBytes());

                        if (task.equals("createTransaction")) {
                            System.out.println("node " + nodeId + " creating transaction");

                            String s = new String(body);

                            JsonObject json = JsonParser.parseString(s).getAsJsonObject();
                            String privKey = json.get("privKey").getAsString();
                            String recKey = json.get("recKey").getAsString();
                            double amount = json.get("amount").getAsDouble();


                            String[] arr = new String[]{privKey};

                            Transaction t = TransactionServices.createTransaction(arr, recKey, amount, rocksHandler);

                            System.out.println(t == null ? "unable to create transaction" :
                                    "created transaction successfully");

                            //validate transaction all over committee
                            String jsonString = gson.toJson(t);
                            Map<String, Object> mp = new HashMap<>();
                            mp.put("task", "validateTransaction");

                            AMQP.BasicProperties props =
                                    new AMQP.BasicProperties().
                                            builder().
                                            headers(mp).
                                            contentType("application/json").
                                            build();

                            channel.basicPublish(EXCHANGE_NAME, committeeQueue, props, jsonString.getBytes());
                        }

                        if (task.equals("validateTransaction")) {
                            System.out.println("node " + nodeId + " validating transaction");

                            String s = new String(body);
                            Transaction t = gson.fromJson(s, Transaction.class);
                            try {
                                boolean valid = TransactionServices.validateTransaction(t, rocksHandler);

                                System.out.println(t + " is valid " + valid);
                                if (valid) {
                                    transactions.add(t);
                                }
                            } catch (SignatureException e) {
                                e.printStackTrace();
                            }
                        }

                        if (task.equals("getBalance")) {
                            System.out.println("node " + nodeId + " getting balance");

                            String s = new String(body);
                            JsonObject json = JsonParser.parseString(s).getAsJsonObject();
                            String pubKey = json.get("pubKey").getAsString();

                            String[] arr = new String[]{pubKey};
                            double amount = TransactionServices.getBalance(arr, rocksHandler);

                            System.out.println("amount is " + amount);

                            json = new JsonObject();
                            json.addProperty("amount", amount);

                            Map<String, Object> mp = new HashMap<>();
                            mp.put("task", "routeBalance");

                            AMQP.BasicProperties props =
                                    new AMQP.BasicProperties().
                                            builder().
                                            headers(mp).
                                            contentType("application/json").
                                            build();

                            channel.basicPublish(EXCHANGE_NAME, "SIGNALING_SERVER", props, json.toString().getBytes());
                        }

                        if (task.equals("mine")) {
                            System.out.println("node " + nodeId + " mining");
                            List<Transaction> rem = List.copyOf(transactions);
                            Block b = BlockServices.mineBlock(rem, mongoHandler);

                            //committee validate block
                            Map<String, Object> mp = new HashMap<>();
                            mp.put("task", "validateBlock");

                            AMQP.BasicProperties props =
                                    new AMQP.BasicProperties().
                                            builder().
                                            headers(mp).
                                            contentType("application/json").
                                            build();

                            String block = gson.toJson(b);
                            channel.basicPublish(EXCHANGE_NAME, committeeQueue, props, block.getBytes());


                            //update utxos
                            mp.clear();
                            mp.put("task", "updateUTXO");
                            props = new AMQP.BasicProperties().
                                    builder().
                                    headers(mp).
                                    contentType("application/json").
                                    build();

                            channel.basicPublish(EXCHANGE_NAME, primaryQueue, props, block.getBytes());
                        }

                        if (task.equals("validateBlock")) {
                            System.out.println("node " + nodeId + " validating block");
                            String blockJson = new String(body);
                            Block block = gson.fromJson(blockJson, Block.class);
                            List<Transaction> rem = BlockServices.validateAndAddBlock(block, 5, mongoHandler);
                            System.out.println(rem == null ? "null" : rem.toString());
                            if (rem != null) transactions.removeAll(rem);
                        }


                        if (task.equals("updateUTXO")) {
                            System.out.println("node " + nodeId + " updating rocks");

                            String blockJson = new String(body);
                            Block block = gson.fromJson(blockJson, Block.class);
                            rocksHandler.update(block);

                            Map<String, Object> mp = new HashMap<>();
                            mp.put("task", "blockValidated");

                            AMQP.BasicProperties props =
                                    new AMQP.BasicProperties().
                                            builder().
                                            headers(mp).
                                            contentType("application/json").
                                            build();

                            channel.basicPublish(EXCHANGE_NAME, "SIGNALING_SERVER", props, null);
                        }


                        channel.basicAck(deliveryTag, false);
                    }
                });
    }

    private void setEXCHANGE_NAME() {
        String path = "/home/baroudy/Projects/Bachelor/payment-system/.env";
        Dotenv dotenv = Dotenv.configure().directory(path).load();
        EXCHANGE_NAME = dotenv.get("EXCHANGE_NAME");
    }
}
