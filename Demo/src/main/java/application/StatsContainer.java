package application;

import application.controllers.LoggerController;
import com.google.gson.JsonObject;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.TreeMap;

public class StatsContainer {
    public static final TreeMap<Integer, ArrayList<String>> mp = new TreeMap<>();
    private static final TreeMap<String, Service> nodes = new TreeMap<>();
    private static final TreeMap<Integer, Service> committees = new TreeMap<>();
    private static final TreeMap<String, Integer> committeeOfNode = new TreeMap<>();
    private static int loginCnt;
    private static int registerCnt;
    private static int balanceCnt;
    private static int buyCnt;
    private static int sellCnt;
    private static int transferCnt;
    private static int nodesCnt;
    private static int committeesCnt;
    private static int currentBlockIdx = 1;

    public static int getLoginCnt() {
        return loginCnt;
    }

    public static int getRegisterCnt() {
        return registerCnt;
    }

    public static int getBalanceCnt() {
        return balanceCnt;
    }

    public static int getBuyCnt() {
        return buyCnt;
    }

    public static int getSellCnt() {
        return sellCnt;
    }

    public static int getTransferCnt() {
        return transferCnt;
    }

    public static int getNodesCnt() {
        return nodesCnt;
    }

    public static int getCommitteesCnt() {
        return committeesCnt;
    }

    public static int getCurrentBlockIdx() {
        return currentBlockIdx;
    }

    public static int getCommitteeCreateTransactionCnt(int committeeId) {
        return committees.get(committeeId).createTransactionCnt;
    }

    public static int getCommitteeBalanceCnt(int committeeId) {
        return committees.get(committeeId).getBalanceCnt;
    }

    public static int getCommitteeMineCnt(int committeeId) {
        return committees.get(committeeId).mineCnt;
    }

    public static int getCommitteeValidateTransactionCnt(int committeeId) {
        return committees.get(committeeId).validateTransactionCnt;
    }

    public static int getCommitteeValidateBlockCnt(int committeeId) {
        return committees.get(committeeId).validateBlockCnt;
    }

    public static int getCommitteeUpdateRocksCnt(int committeeId) {
        return committees.get(committeeId).updateRocksCnt;
    }

    public static int getNodeCreateTransactionCnt(String nodeId) {
        return nodes.get(nodeId).createTransactionCnt;
    }

    public static int getNodeBalanceCnt(String nodeId) {
        return nodes.get(nodeId).getBalanceCnt;
    }

    public static int getNodeMineCnt(String nodeId) {
        return nodes.get(nodeId).mineCnt;
    }

    public static int getNodeValidateTransactionCnt(String nodeId) {
        return nodes.get(nodeId).validateTransactionCnt;
    }

    public static int getNodeValidateBlockCnt(String nodeId) {
        return nodes.get(nodeId).validateBlockCnt;
    }

    public static int getNodeUpdateRocksCnt(String nodeId) {
        return nodes.get(nodeId).updateRocksCnt;
    }

    public static int getCommitteeOfNode(String nodeId) {
        return committeeOfNode.get(nodeId);
    }

    public static void update(JsonObject json) {
        switch (json.get("server").getAsString()) {
            case "API":
                handleAPI(json);
                return;
            case "signaling":
                handleSignaling(json);
                return;
            case "node":
                handleNode(json);
        }
    }

    private static void handleSignaling(JsonObject json) {
        if ("register".equals(json.get("task").getAsString())) {
            int committeeId = json.get("committee").getAsInt();
            String id = json.get("id").getAsString();
            ArrayList<String> arr = mp.getOrDefault(committeeId, new ArrayList<>());
            arr.add(id);

            nodesCnt++;
            mp.put(committeeId, arr);
            committeesCnt = mp.size();
            committeeOfNode.put(id, committeeId);

            registerNodeToLogger(id);
            Text text = new Text("Registering node " + id + " to committee "
                    + committeeId + " @ " + getTime());

            text.setFill(Color.MEDIUMSEAGREEN);
            LoggerController.log(text, "signaling");

            text.setText(" Node " + id + " is up and registered @ " + getTime());
            LoggerController.log(text, id);
            return;
        }

        if ("createTransaction".equals(json.get("task").getAsString())) {
            String id = json.get("id").getAsString();
            Text text = new Text("Electing node " + id +
                    " to create transaction @ " + getTime());
            text.setFill(Color.GREEN);
            LoggerController.log(text, "signaling");
            return;
        }

        if ("getBalance".equals(json.get("task").getAsString())) {
            String id = json.get("id").getAsString();
            Text text = new Text("Electing node " + id +
                    " to get user's balance @ " + getTime());
            text.setFill(Color.CHOCOLATE);
            LoggerController.log(text, "signaling");
            return;
        }

        if ("routeBalance".equals(json.get("task").getAsString())) {
            String amount = json.get("amount").getAsString();
            Text text = new Text("returning balance of amount " + amount +
                    " to the user @ " + getTime());
            text.setFill(Color.CRIMSON);
            LoggerController.log(text, "signaling");
            return;
        }

        if ("blockValidated".equals(json.get("task").getAsString())) {
            String id = json.get("id").getAsString();
            Text text = new Text("node " + id +
                    " has received and validated the last block mined @ " + getTime());
            text.setFill(Color.NAVY);
            LoggerController.log(text, "signaling");
            return;
        }

        if ("mine".equals(json.get("task").getAsString())) {
            String id = json.get("id").getAsString();
            Text text = new Text("Electing node " + id +
                    " to mine the next block for it's committee @ " + getTime());
            text.setFill(Color.TOMATO);
            LoggerController.log(text, "signaling");
        }
    }

    private static void registerNodeToLogger(String id) {
        switch (nodesCnt) {
            case 1: {
                LoggerController.mp.put(id, LoggerController.fstLoggerQueue);
                return;
            }
            case 2: {
                LoggerController.mp.put(id, LoggerController.sndLoggerQueue);
                return;
            }
            case 3: {
                LoggerController.mp.put(id, LoggerController.thrdLoggerQueue);
                return;
            }
            case 4: {
                LoggerController.mp.put(id, LoggerController.fourthLoggerQueue);
                return;
            }
            case 5: {
                LoggerController.mp.put(id, LoggerController.fifthLoggerQueue);
                return;
            }
            case 6: {
                LoggerController.mp.put(id, LoggerController.sixthLoggerQueue);
                return;
            }
            case 7: {
                LoggerController.mp.put(id, LoggerController.seventhLoggerQueue);
                return;
            }
            case 8: {
                LoggerController.mp.put(id, LoggerController.eighthLoggerQueue);
            }
        }
    }

    private static void handleNode(JsonObject json) {
        switch (json.get("task").getAsString()) {
            case "createTransaction": {
                String nodeId = json.get("id").getAsString();
                Service fst = nodes.getOrDefault(nodeId, new Service());
                Service snd = committees.getOrDefault(committeeOfNode.get(nodeId), new Service());

                fst.createTransactionCnt++;
                snd.createTransactionCnt++;

                nodes.put(nodeId, fst);
                committees.put(committeeOfNode.get(nodeId), snd);

                String amount = json.get("amount").getAsString();
                Text text = new Text("Creating Transaction with amount " +
                        amount + " @ " + getTime());

                text.setFill(Color.GREEN);
                LoggerController.log(text, nodeId);
                return;
            }

            case "getBalance": {
                String nodeId = json.get("id").getAsString();
                Service fst = nodes.getOrDefault(nodeId, new Service());
                Service snd = committees.getOrDefault(committeeOfNode.get(nodeId), new Service());

                fst.getBalanceCnt++;
                snd.getBalanceCnt++;

                nodes.put(nodeId, fst);
                committees.put(committeeOfNode.get(nodeId), snd);

                String amount = json.get("amount").getAsString();
                Text text = new Text("sending balance of amount " +
                        amount + " to signaling server @ " + getTime());

                text.setFill(Color.CHOCOLATE);
                LoggerController.log(text, nodeId);
                return;
            }

            case "validateBlock": {
                String nodeId = json.get("id").getAsString();
                Service fst = nodes.getOrDefault(nodeId, new Service());
                Service snd = committees.getOrDefault(committeeOfNode.get(nodeId), new Service());

                fst.validateBlockCnt++;
                snd.validateBlockCnt++;

                nodes.put(nodeId, fst);
                committees.put(committeeOfNode.get(nodeId), snd);

                String idx = json.get("block").getAsString();
                Text text = new Text("validated mined block of idx " +
                        idx + " @ " + getTime());

                text.setFill(Color.PURPLE);
                LoggerController.log(text, nodeId);
                return;
            }

            case "mine": {
                String nodeId = json.get("id").getAsString();
                currentBlockIdx = json.get("block").getAsInt();
                Service fst = nodes.getOrDefault(nodeId, new Service());
                Service snd = committees.getOrDefault(committeeOfNode.get(nodeId), new Service());

                fst.mineCnt++;
                snd.mineCnt++;

                nodes.put(nodeId, fst);
                committees.put(committeeOfNode.get(nodeId), snd);

                String idx = json.get("block").getAsString();
                Text text = new Text("mined block of idx " +
                        idx + " @ " + getTime());

                text.setFill(Color.TOMATO);
                LoggerController.log(text, nodeId);
                return;
            }

            case "updateUTXO": {
                String nodeId = json.get("id").getAsString();
                Service fst = nodes.getOrDefault(nodeId, new Service());
                Service snd = committees.getOrDefault(committeeOfNode.get(nodeId), new Service());

                fst.updateRocksCnt++;
                snd.updateRocksCnt++;

                nodes.put(nodeId, fst);
                committees.put(committeeOfNode.get(nodeId), snd);

                Text text = new Text("updated UTXO set @ " + getTime());

                text.setFill(Color.ROYALBLUE);
                LoggerController.log(text, nodeId);
                return;
            }

            case "validateTransaction": {
                String nodeId = json.get("id").getAsString();
                Service fst = nodes.getOrDefault(nodeId, new Service());
                Service snd = committees.getOrDefault(committeeOfNode.get(nodeId), new Service());

                fst.validateTransactionCnt++;
                snd.validateTransactionCnt++;

                nodes.put(nodeId, fst);
                committees.put(committeeOfNode.get(nodeId), snd);

                String amount = json.get("amount").getAsString();
                Text text = new Text("validated Transaction with amount " +
                        amount + " @ " + getTime());

                text.setFill(Color.MEDIUMVIOLETRED);
                LoggerController.log(text, nodeId);
            }
        }
    }

    private static void handleAPI(JsonObject json) {
        switch (json.get("task").getAsString()) {
            case "balance": {
                String id = json.get("id").getAsString();
                Text text = new Text("user " + id + " requested balance @ " +
                        getTime());
                text.setFill(Color.CHOCOLATE);
                LoggerController.log(text, "API");
                balanceCnt++;
                break;
            }

            case "buy": {
                String id = json.get("id").getAsString();
                String amount = json.get("amount").getAsString();
                Text text = new Text("user " + id + " requested to buy " +
                        amount + " @ " + getTime());
                text.setFill(Color.BROWN);
                LoggerController.log(text, "API");
                buyCnt++;
                break;
            }

            case "login": {
                String id = json.get("id").getAsString();
                Text text = new Text("user " + id + " requested to login @ " +
                        getTime());
                text.setFill(Color.MIDNIGHTBLUE);
                LoggerController.log(text, "API");
                loginCnt++;
                break;
            }

            case "register": {
                String id = json.get("id").getAsString();
                Text text = new Text("user " + id + " requested to register @ " +
                        getTime());
                text.setFill(Color.LIGHTCORAL);
                LoggerController.log(text, "API");
                registerCnt++;
                break;
            }

            case "sell": {
                String id = json.get("id").getAsString();
                String amount = json.get("amount").getAsString();
                Text text = new Text("user " + id + " requested to sell " +
                        amount + " @ " + getTime());
                text.setFill(Color.CHARTREUSE);
                LoggerController.log(text, "API");
                buyCnt++;
                sellCnt++;
                break;
            }

            case "transfer": {
                String id = json.get("id").getAsString();
                String amount = json.get("amount").getAsString();
                String to = json.get("to").getAsString();
                Text text = new Text("user " + id + " requested to transfer " +
                        amount + " to " + to + " @ " + getTime());
                text.setFill(Color.DARKGOLDENROD);
                LoggerController.log(text, "API");
                buyCnt++;
                transferCnt++;

            }
        }
    }

    public static String getTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    static class Service {
        int createTransactionCnt;
        int getBalanceCnt;
        int mineCnt;
        int validateBlockCnt;
        int validateTransactionCnt;
        int updateRocksCnt;
    }
}
