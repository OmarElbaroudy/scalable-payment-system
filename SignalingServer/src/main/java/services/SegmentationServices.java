package services;

import application.SignalingServer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import persistence.RocksHandler;

import java.util.HashMap;
import java.util.Map;

public class SegmentationServices {
    private final Channel channel;
    private final RocksHandler handler;
    private final String exchangeName = "BLOCKCHAIN";
    private final CoordinationServices services;
    private int numberOfCommittees;
    private int transactionNumber = 0;
    private boolean mining = false, initialized = false;
    private int curCommittee = 0;

    public SegmentationServices(RocksHandler handler, Channel channel) throws Exception {
        this.handler = handler;
        this.channel = channel;
        services = new CoordinationServices(handler);
    }

    private void createTransaction(byte[] body) throws Exception {
        curCommittee = curCommittee % numberOfCommittees + 1;
        String nodeId = services.getRandomNodeId(String.valueOf(curCommittee));

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("server", "signaling");
        jsonObject.addProperty("id", nodeId);
        jsonObject.addProperty("task", "createTransaction");

        SignalingServer.log(jsonObject);

        Map<String, Object> mp = new HashMap<>();
        mp.put("task", "createTransaction");

        AMQP.BasicProperties props =
                new AMQP.BasicProperties().
                        builder().
                        headers(mp).
                        contentType("application/json").
                        build();

        channel.basicPublish("", nodeId, props, body);
    }

    private void getBalance(byte[] body) throws Exception {
        curCommittee = curCommittee % numberOfCommittees + 1;
        String nodeId = services.getRandomNodeId(String.valueOf(curCommittee));

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("server", "signaling");
        jsonObject.addProperty("id", nodeId);
        jsonObject.addProperty("task", "getBalance");

        SignalingServer.log(jsonObject);

        Map<String, Object> mp = new HashMap<>();
        mp.put("task", "getBalance");

        AMQP.BasicProperties props =
                new AMQP.BasicProperties().
                        builder().
                        headers(mp).
                        contentType("application/json").
                        build();

        channel.basicPublish("", nodeId, props, body);
    }

    private void routeBalance(byte[] body) throws Exception {
        Map<String, Object> mp = new HashMap<>();

        String s = new String(body);
        JsonObject json = JsonParser.parseString(s).getAsJsonObject();
        String pubKey = json.get("pubKey").getAsString();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("server", "signaling");
        jsonObject.addProperty("task", "routeBalance");
        jsonObject.addProperty("amount", json.get("amount").getAsString());

        SignalingServer.log(jsonObject);

        mp.put("pubKey", pubKey);

        AMQP.BasicProperties props =
                new AMQP.BasicProperties().
                        builder().
                        headers(mp).
                        contentType("application/json").
                        build();

        channel.basicPublish("", "API", props, body);
    }

    private void blockValidated(byte[] body) throws Exception {
        if (!mining) return;

        String s = new String(body);
        JsonObject json = JsonParser.parseString(s).getAsJsonObject();
        String nodeId = json.get("nodeId").getAsString();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("server", "signaling");
        jsonObject.addProperty("id", nodeId);
        jsonObject.addProperty("task", "blockValidated");

        System.out.println(nodeId + " validated block");

        SignalingServer.log(jsonObject);

        mining = services.isMining(nodeId, false);
    }

    private void nodeRegistered() throws Exception {
        if (initialized) return;
        int cnt = handler.getNumberOfNodes();
        if (cnt == Integer.parseInt(System.getenv("TOTAL_NUMBER_OF_NODES"))) {
            initialized = true;
            init();
        }
    }

    private void segment() throws Exception {
        int limit = Integer.parseInt(System.getenv("TRANSACTION_NUMBER"));
        limit *= Integer.parseInt(System.getenv("COMMITTEE_SIZE"));
        if (!mining && transactionNumber >= limit) {
            transactionNumber = 0;
            mining = true;

            for (int i = 1; i <= numberOfCommittees; i++) {
                String nodeId = services.getRandomNodeId(String.valueOf(i));

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("server", "signaling");
                jsonObject.addProperty("id", nodeId);
                jsonObject.addProperty("task", "mine");

                SignalingServer.log(jsonObject);

                Map<String, Object> mp = new HashMap<>();
                mp.put("task", "mine");

                AMQP.BasicProperties props =
                        new AMQP.BasicProperties().
                                builder().
                                headers(mp).
                                contentType("application/json").
                                build();

                channel.basicPublish("", nodeId, props, null);
            }
        }
    }

    private void incTransactions() {
        transactionNumber++;
    }

    private void incTransactionsByCommittee(byte[] body) {
        String committeeId = new String(body);
        mining = services.isMining(committeeId, true);
    }

    private void init() throws Exception {
        System.out.println("initialized");
        numberOfCommittees = handler.getNumberOfCommittees();
        for (int i = 1; i <= numberOfCommittees; i++) {
            String nodeId = services.getRandomNodeId(String.valueOf(i));
            System.out.println("electing node" + nodeId + " to generate genesis");

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("server", "signaling");
            jsonObject.addProperty("id", nodeId);
            jsonObject.addProperty("task", "generateGenesis");

            SignalingServer.log(jsonObject);


            Map<String, Object> mp = new HashMap<>();
            mp.put("task", "generateGenesis");

            AMQP.BasicProperties props =
                    new AMQP.BasicProperties().
                            builder().
                            headers(mp).
                            contentType("application/json").
                            build();

            channel.basicPublish("", nodeId, props, null);
        }
    }

    public void exec(String task, byte[] body) throws Exception {
        switch (task) {
            case "createTransaction" -> createTransaction(body);

            case "getBalance" -> getBalance(body);

            case "routeBalance" -> routeBalance(body);

            case "blockValidated" -> blockValidated(body);

            case "incTransactions" -> incTransactions();

            case "validateCommittee" -> incTransactionsByCommittee(body);

            case "nodeRegistered" -> nodeRegistered();
        }
        segment();
    }
}
