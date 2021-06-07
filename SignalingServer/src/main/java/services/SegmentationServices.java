package services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import persistence.RocksHandler;

import java.util.HashMap;
import java.util.Map;

public class SegmentationServices {
    private final Channel channel;
    private final String exchangeName = "BLOCKCHAIN";
    private final CoordinationServices services;
    private final int numberOfCommittees;
    private int transactionNumber = 0;
    private boolean mining = false;
    private int curCommittee = 0;

    public SegmentationServices(RocksHandler handler, Channel channel) {
        this.channel = channel;
        services = new CoordinationServices(handler);
        numberOfCommittees = handler.getNumberOfCommittees();
    }

    private void createTransaction(byte[] body) throws Exception {
        transactionNumber++;
        curCommittee = curCommittee % numberOfCommittees + 1;
        String nodeId = services.getRandomNodeId(String.valueOf(curCommittee));

        System.out.println(nodeId + " assigned to create transaction");

        Map<String, Object> mp = new HashMap<>();
        mp.put("task", "createTransaction");

        AMQP.BasicProperties props =
                new AMQP.BasicProperties().
                        builder().
                        headers(mp).
                        contentType("application/json").
                        build();

        channel.basicPublish(exchangeName, nodeId, props, body);
    }

    private void getBalance(byte[] body) throws Exception {
        curCommittee = curCommittee % numberOfCommittees + 1;
        String nodeId = services.getRandomNodeId(String.valueOf(curCommittee));

        Map<String, Object> mp = new HashMap<>();
        mp.put("task", "getBalance");

        AMQP.BasicProperties props =
                new AMQP.BasicProperties().
                        builder().
                        headers(mp).
                        contentType("application/json").
                        build();

        channel.basicPublish(exchangeName, nodeId, props, body);
    }

    private void routeBalance(byte[] body) throws Exception {
        Map<String, Object> mp = new HashMap<>();
        mp.put("task", "balance");

        AMQP.BasicProperties props =
                new AMQP.BasicProperties().
                        builder().
                        headers(mp).
                        contentType("application/json").
                        build();

        channel.basicPublish(exchangeName, "API", props, body);
    }

    private void blockValidated(byte[] body) {
        String s = new String(body);
        JsonObject json = JsonParser.parseString(s).getAsJsonObject();
        String nodeId = json.get("nodeId").getAsString();
        mining = !services.endBlockValidationPhase(nodeId);
    }

    private void segment() throws Exception {
        if (!mining && transactionNumber >= 1000) {
            for (int i = 1; i <= numberOfCommittees; i++) {
                String nodeId = services.getRandomNodeId(String.valueOf(i));

                Map<String, Object> mp = new HashMap<>();
                mp.put("task", "mine");

                AMQP.BasicProperties props =
                        new AMQP.BasicProperties().
                                builder().
                                headers(mp).
                                contentType("application/json").
                                build();

                channel.basicPublish(exchangeName, nodeId, props, null);
            }
        }
    }

    public void exec(String task, byte[] body) throws Exception {
        switch (task) {
            case "createTransaction" -> createTransaction(body);

            case "getBalance" -> getBalance(body);

            case "routeBalance" -> routeBalance(body);

            case "blockValidated" -> blockValidated(body);
        }

        segment();
    }
}
