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
        }

        Map<String, Object> mp = new HashMap<>();
        mp.put("task", "incTransactions");

        AMQP.BasicProperties props =
                new AMQP.BasicProperties().
                        builder().
                        headers(mp).
                        contentType("application/json").
                        build();

        channel.basicPublish(exchangeName, "SIGNALING_SERVER", props, null);
    }


    public void getBalance(byte[] body) throws Exception {
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

        channel.basicPublish(exchangeName, "SIGNALING_SERVER", props, json.toString().getBytes());
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

            channel.basicPublish(exchangeName, "SIGNALING_SERVER", props, committeeQueue.getBytes());
            return;
        }

        Block b = BlockServices.mineBlock(transactions, mongoHandler, rocksHandler);
        System.out.println(b.getTransactions().getTransactions().size());

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

//        if (!rem.isEmpty()) {
//            mp.put("task", "cleanTransactions");
//
//            props = new AMQP.BasicProperties().
//                    builder().
//                    headers(mp).
//                    contentType("application/json").
//                    build();
//
//            byte[] transactions = gson.toJson(rem).getBytes();
//            channel.basicPublish(exchangeName, committeeQueue, props, transactions);
//        }


        //update utxos
        mp.put("task", "updateUTXO");
        props = new AMQP.BasicProperties().
                builder().
                headers(mp).
                contentType("application/json").
                build();

        channel.basicPublish(exchangeName, primaryQueue, props, block.getBytes());
    }


    public void validateBlock(byte[] body) {
        System.out.println("node " + nodeId + " validating block");
        String blockJson = new String(body);
        Block block = gson.fromJson(blockJson, Block.class);
        List<Transaction> rem = BlockServices.validateAndAddBlock(block, mongoHandler);
        System.out.println(rem == null ? "block is invalid" : "block is valid");
        if (rem != null) transactions.removeAll(rem);
    }


    public void updateUTXO(byte[] body) throws Exception {
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

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("nodeId", nodeId);
        String jsonString = jsonObject.toString();

        channel.basicPublish(exchangeName, "SIGNALING_SERVER", props, jsonString.getBytes());
    }


    public void sendGenesis(String senderId) throws Exception {
        System.out.println("node " + nodeId + " sending Genesis");
        Block b = mongoHandler.getBlock(1);
        String json = gson.toJson(b);

        Map<String, Object> mp = new HashMap<>();
        mp.put("task", "recGenesis");

        AMQP.BasicProperties props =
                new AMQP.BasicProperties().
                        builder().
                        headers(mp).
                        contentType("application/json").
                        build();

        channel.basicPublish(exchangeName, senderId, props, json.getBytes());
    }


    public void recGenesis(byte[] body) {
        String jsonString = new String(body);
        Block b = gson.fromJson(jsonString, Block.class);

        System.out.println("node " + nodeId + " received genesis successfully");

        mongoHandler.saveBlock(b);
        rocksHandler.update(b);
    }

    public void cleanTransactions(byte[] body) {
        String jsonString = new String(body);

        Type listType = new TypeToken<List<Transaction>>() {
        }.getType();

        List<Transaction> ts = gson.fromJson(jsonString, listType);
        transactions.removeAll(ts);
    }

    public void sendGenesisUTXO(String senderId) throws Exception{
        Block b = mongoHandler.getBlock(1);
        String json = gson.toJson(b);

        Map<String, Object> mp = new HashMap<>();
        mp.put("task", "recGenesisUTXO");

        AMQP.BasicProperties props =
                new AMQP.BasicProperties().
                        builder().
                        headers(mp).
                        contentType("application/json").
                        build();

        channel.basicPublish(exchangeName, senderId, props, json.getBytes());
    }

    public void recGenesisUTXO(byte[] body){
        String jsonString = new String(body);
        Block b = gson.fromJson(jsonString, Block.class);

        rocksHandler.update(b);
    }

    public void exec(String task, String senderId, byte[] body) throws Exception {
        switch (task) {
            case "createTransaction" -> createTransaction(body);

            case "validateTransaction" -> validateTransaction(body);

            case "getBalance" -> getBalance(body);

            case "mine" -> mine();

            case "validateBlock" -> validateBlock(body);

            case "updateUTXO" -> updateUTXO(body);

            case "sendGenesis" -> sendGenesis(senderId);

            case "recGenesis" -> recGenesis(body);

            case "cleanTransactions" -> cleanTransactions(body);

            case "sendGenesisUTXO" -> sendGenesisUTXO(senderId);

            case "recGenesisUTXO" -> recGenesisUTXO(body);
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
