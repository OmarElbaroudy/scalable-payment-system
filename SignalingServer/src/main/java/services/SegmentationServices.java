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
    private final CoordinationServices services;
    private final int committeeSize;
    private int numberOfCommittees;
    private int transactionNumber = 0;
    private boolean mining = false, initialized = false;
    private int curCommittee = 0;

    public SegmentationServices(RocksHandler handler, Channel channel) {
        this.handler = handler;
        this.channel = channel;
        services = new CoordinationServices(handler);
        committeeSize = Integer.parseInt(System.getenv("COMMITTEE_SIZE"));
    }

    private void createTransaction(byte[] body) throws Exception {
        curCommittee = curCommittee % numberOfCommittees + 1;
        int nodeId = (curCommittee - 1) * committeeSize + 1;

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

        channel.basicPublish("", String.valueOf(nodeId), props, body);
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
        String s = new String(body);
        JsonObject json = JsonParser.parseString(s).getAsJsonObject();
        String nodeId = json.get("nodeId").getAsString();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("server", "signaling");
        jsonObject.addProperty("id", nodeId);
        jsonObject.addProperty("task", "blockValidated");

        SignalingServer.log(jsonObject);
        if (mining) mining = services.isMining(nodeId, false);
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

    private void validateCommittee(byte[] body) {
        String s = new String(body);
        JsonObject json = JsonParser.parseString(s).getAsJsonObject();
        String id = json.get("committeeId").getAsString();
        mining = services.isMining(id, true);
    }

    private void init() throws Exception {
        System.out.println("initialized");
        numberOfCommittees = handler.getNumberOfCommittees();
        int totalNumberOfNodes = handler.getNumberOfNodes();

        CoordinationServices.setTotalNumberOfCommittees(numberOfCommittees);
        CoordinationServices.setTotalNumberOfNodes(totalNumberOfNodes);

        for (int i = 1; i <= numberOfCommittees; i++) {
            String nodeId = services.getRandomNodeId(String.valueOf(i));

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

            case "validateCommittee" -> validateCommittee(body);

            case "nodeRegistered" -> nodeRegistered();
        }
        segment();
    }

    public boolean isMining(){
        return mining;
    }
}
