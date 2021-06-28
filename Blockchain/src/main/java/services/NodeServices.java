package services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import persistence.MongoHandler;
import persistence.RocksHandler;
import persistence.models.Block;
import persistence.models.Transaction;

import java.lang.reflect.Type;
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
        double amount = json.get("amount").getAsDouble();

        String[] arr = new String[]{privKey};
        Transaction t = TransactionServices.createTransaction(arr, recKey, amount, rocksHandler);

        System.out.println(t == null ? "unable to create transaction" :
                "created transaction successfully");

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

        boolean valid = TransactionServices.validateTransaction(t, rocksHandler);

        System.out.println("transaction is valid => " + valid);
        if (valid) {
            transactions.add(t);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("server", "node");
            jsonObject.addProperty("id", nodeId);
            jsonObject.addProperty("task", "validateTransaction");
            jsonObject.addProperty("recKey", t.getOutput().getScriptPublicKey());
            jsonObject.addProperty("amount", t.getOutput().getAmount());

            log(jsonObject);
        }

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


    public void getBalance(byte[] body) throws Exception {
        System.out.println("node " + nodeId + " getting balance");

        String s = new String(body);
        JsonObject json = JsonParser.parseString(s).getAsJsonObject();
        String pubKey = json.get("pubKey").getAsString();

        String[] arr = new String[]{pubKey};
        double amount = TransactionServices.getBalance(arr, rocksHandler);

        System.out.println("amount is " + amount);

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
        System.out.println("node " + nodeId + " mining");

        if (transactions.isEmpty()) {
            System.out.println("no transactions to be mined");

            Map<String, Object> mp = new HashMap<>();
            mp.put("task", "validateCommittee");

            AMQP.BasicProperties props =
                    new AMQP.BasicProperties().
                            builder().
                            headers(mp).
                            contentType("application/json").
                            build();

            channel.basicPublish("", "SIGNALING_SERVER", props, committeeQueue.getBytes());
            return;
        }

        Block b = BlockServices.mineBlock(transactions, mongoHandler, rocksHandler);
        System.out.println(b.getTransactions().getTransactions().size());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("server", "node");
        jsonObject.addProperty("id", nodeId);
        jsonObject.addProperty("task", "mine");
        jsonObject.addProperty("block", b.getIdx());

        log(jsonObject);
        signalMinedBlock(b);
    }

    private void clean(List<Transaction> rem) throws Exception {
        if (!rem.isEmpty()) {
            Map<String, Object> mp = new HashMap<>();
            mp.put("task", "cleanTransactions");

            AMQP.BasicProperties props = new AMQP.BasicProperties().
                    builder().
                    headers(mp).
                    contentType("application/json").
                    build();

            byte[] transactions = gson.toJson(rem).getBytes();
            channel.basicPublish(exchangeName, committeeQueue, props, transactions);
        }
    }


    public void validateBlock(byte[] body) throws Exception {
        System.out.println("node " + nodeId + " validating block");
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
        System.out.println("node " + nodeId + " updating rocks");

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

    public void cleanTransactions(byte[] body) {
        String jsonString = new String(body);

        Type listType = new TypeToken<List<Transaction>>() {
        }.getType();

        List<Transaction> ts = gson.fromJson(jsonString, listType);
        transactions.removeAll(ts);
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

        //TODO call clean

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

            case "cleanTransactions" -> cleanTransactions(body);

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
