package services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import persistence.MongoHandler;
import persistence.RocksHandler;
import persistence.models.Block;
import persistence.models.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeServices {
    private Gson gson;
    private String nodeId;
    private Channel channel;
    private String primaryQueue;
    private String exchangeName;
    private String committeeQueue;
    private RocksHandler rocksHandler;
    private MongoHandler mongoHandler;
    private List<Transaction> transactions;


    public void createTransaction(byte[] body) throws Exception {
        String s = new String(body);

        JsonObject json = JsonParser.parseString(s).getAsJsonObject();
        String privKey = json.get("privKey").getAsString();
        String recKey = json.get("recKey").getAsString();
        int amount = json.get("amount").getAsInt();

        Transaction t = TransactionServices.handleTransactionCreation(
                transactions, privKey, recKey, amount, rocksHandler);

        if (t != null) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("server", "node");
            jsonObject.addProperty("id", nodeId);
            jsonObject.addProperty("task", "createTransaction");
            jsonObject.addProperty("recKey", recKey);
            jsonObject.addProperty("amount", amount);

            log(jsonObject);
        }

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

        channel.basicPublish(exchangeName, committeeQueue, props, jsonString.getBytes());
    }


    public void validateTransaction(byte[] body) throws Exception {
        String s = new String(body);

        Transaction t = gson.fromJson(s, Transaction.class);
        boolean valid = TransactionServices.handleTransactionValidation(
                transactions, t, rocksHandler);

        System.out.println("transaction is valid => " + valid);

        if (valid) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("server", "node");
            jsonObject.addProperty("id", nodeId);
            jsonObject.addProperty("task", "validateTransaction");
            jsonObject.addProperty("amount", t.getOutputAmount());

            log(jsonObject);

            Map<String, Object> mp = new HashMap<>();
            mp.put("task", "incTransactions");

            AMQP.BasicProperties props =
                    new AMQP.BasicProperties().
                            builder().
                            headers(mp).
                            contentType("application/json").
                            build();

            channel.basicPublish("", "SIGNALING_SERVER", props, null);
        }
    }


    public void getBalance(byte[] body) throws Exception {

        String s = new String(body);
        JsonObject json = JsonParser.parseString(s).getAsJsonObject();
        String pubKey = json.get("pubKey").getAsString();

        String[] arr = new String[]{pubKey};
        int amount = TransactionServices.getBalance(arr, rocksHandler);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("server", "node");
        jsonObject.addProperty("id", nodeId);
        jsonObject.addProperty("task", "getBalance");
        jsonObject.addProperty("amount", amount);

        log(jsonObject);

        json = new JsonObject();
        json.addProperty("amount", amount);
        json.addProperty("pubKey", pubKey);

        Map<String, Object> mp = new HashMap<>();
        mp.put("task", "routeBalance");

        AMQP.BasicProperties props =
                new AMQP.BasicProperties().
                        builder().
                        headers(mp).
                        contentType("application/json").
                        build();

        channel.basicPublish("", "SIGNALING_SERVER", props, json.toString().getBytes());
    }

    public void mine() throws Exception {
        if (transactions.isEmpty()) {
            System.out.println("no transactions to be mined");

            Map<String, Object> mp = new HashMap<>();
            mp.put("task", "validateCommittee");

            JsonObject json = new JsonObject();
            json.addProperty("committeeId", committeeQueue);

            AMQP.BasicProperties props =
                    new AMQP.BasicProperties().
                            builder().
                            headers(mp).
                            contentType("application/json").
                            build();

            channel.basicPublish("", "SIGNALING_SERVER", props, json.toString().getBytes());
            return;
        }

        Block b = BlockServices.mineBlock(transactions, mongoHandler, rocksHandler);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("server", "node");
        jsonObject.addProperty("id", nodeId);
        jsonObject.addProperty("task", "mine");
        jsonObject.addProperty("block", b.getIdx());

        log(jsonObject);
        signalMinedBlock(b);
    }


    public void validateBlock(byte[] body) throws Exception {
        String blockJson = new String(body);
        Block block = gson.fromJson(blockJson, Block.class);
        List<Transaction> rem = BlockServices.validateAndAddBlock(block, mongoHandler);
        System.out.println(rem == null ? "block is invalid" : "block is valid");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("server", "node");
        jsonObject.addProperty("id", nodeId);
        jsonObject.addProperty("task", "validateBlock");
        jsonObject.addProperty("block", block.getIdx());

        log(jsonObject);

        if (rem != null) transactions.removeAll(rem);
    }


    public void updateUTXO(byte[] body) throws Exception {
        String blockJson = new String(body);
        Block block = gson.fromJson(blockJson, Block.class);
        rocksHandler.update(block);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("server", "node");
        jsonObject.addProperty("id", nodeId);
        jsonObject.addProperty("task", "updateUTXO");

        log(jsonObject);

        Map<String, Object> mp = new HashMap<>();
        mp.put("task", "blockValidated");

        AMQP.BasicProperties props =
                new AMQP.BasicProperties().
                        builder().
                        headers(mp).
                        contentType("application/json").
                        build();

        JsonObject json = new JsonObject();
        json.addProperty("nodeId", nodeId);
        String jsonString = json.toString();

        channel.basicPublish("", "SIGNALING_SERVER", props, jsonString.getBytes());
    }

    private void generateGenesis() throws Exception {
        System.out.println(nodeId + " generating genesis block");
        Block b = BlockServices.generateGenesis(mongoHandler, rocksHandler, false, false);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("server", "node");
        jsonObject.addProperty("id", nodeId);
        jsonObject.addProperty("task", "generateGenesis");

        log(jsonObject);
        signalMinedBlock(b);
    }

    private void signalMinedBlock(Block b) throws Exception {
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
        channel.basicPublish(exchangeName, committeeQueue, props, block.getBytes());

        //update utxos
        mp.put("task", "updateUTXO");
        props = new AMQP.BasicProperties().
                builder().
                headers(mp).
                contentType("application/json").
                build();

        channel.basicPublish(exchangeName, primaryQueue, props, block.getBytes());
    }


    private void log(JsonObject json) throws Exception {
        AMQP.BasicProperties props =
                new AMQP.BasicProperties().
                        builder().
                        contentType("application/json").
                        build();

        channel.basicPublish("", "Logger", props, json.toString().getBytes());
    }

    public void exec(String task, byte[] body) throws Exception {
        switch (task) {
            case "createTransaction" -> createTransaction(body);

            case "validateTransaction" -> validateTransaction(body);

            case "getBalance" -> getBalance(body);

            case "mine" -> mine();

            case "validateBlock" -> validateBlock(body);

            case "updateUTXO" -> updateUTXO(body);

            case "generateGenesis" -> generateGenesis();
        }
    }


    public static class Builder {
        private final Gson gson;
        private final String nodeId;
        private final List<Transaction> transactions;
        private Channel channel;
        private String primaryQueue;
        private String exchangeName;
        private String committeeQueue;
        private RocksHandler rocksHandler;
        private MongoHandler mongoHandler;

        public Builder(String nodeId) {
            gson = new Gson();
            this.nodeId = nodeId;
            transactions = new ArrayList<>();
        }

        public Builder channel(Channel channel) {
            this.channel = channel;
            return this;
        }

        public Builder primaryQueue(String primaryQueue) {
            this.primaryQueue = primaryQueue;
            return this;
        }

        public Builder committeeQueue(String committeeQueue) {
            this.committeeQueue = committeeQueue;
            return this;
        }

        public Builder exchangeName(String exchangeName) {
            this.exchangeName = exchangeName;
            return this;
        }

        public Builder rocksHandler(RocksHandler handler) {
            this.rocksHandler = handler;
            return this;
        }

        public Builder mongoHandler(MongoHandler handler) {
            this.mongoHandler = handler;
            return this;
        }

        public NodeServices build() {
            NodeServices services = new NodeServices();

            services.gson = this.gson;
            services.nodeId = this.nodeId;
            services.channel = this.channel;
            services.exchangeName = this.exchangeName;
            services.rocksHandler = this.rocksHandler;
            services.mongoHandler = this.mongoHandler;
            services.committeeQueue = this.committeeQueue;
            services.primaryQueue = this.primaryQueue;
            services.transactions = this.transactions;

            return services;
        }
    }
}
