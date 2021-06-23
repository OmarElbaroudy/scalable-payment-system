package application;

import application.controllers.StatsController;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.TreeMap;

public class StatsContainer {
    private static int loginCnt;
    private static int registerCnt;
    private static int balanceCnt;
    private static int buyCnt;
    private static int sellCnt;
    private static int transferCnt;
    private static int nodesCnt;
    private static int committeesCnt;
    private static int currentBlockIdx;
    private static TreeMap<String, Service> nodes = new TreeMap<>();
    private static TreeMap<Integer, Service> committees = new TreeMap<>();
    private static TreeMap<Integer, ArrayList<String>> mp = new TreeMap<>();
    private static TreeMap<String, Integer> committeeOfNode = new TreeMap<>();

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
            ArrayList<String> arr = mp.getOrDefault(committeeId, new ArrayList<>());
            arr.add(json.get("id").getAsString());

            nodesCnt++;
            mp.put(committeeId, arr);
            committeesCnt = mp.size();
            committeeOfNode.put(json.get("id").getAsString(), committeeId);
        }
    }

    private static void handleNode(JsonObject json) {
        switch (json.get("task").getAsString()) {
            case "createTransaction": {
                String nodeId = json.get("id").getAsString();
                nodes.get(nodeId).createTransactionCnt++;
                committees.get(committeeOfNode.get(nodeId)).createTransactionCnt++;
                return;
            }

            case "getBalance": {
                String nodeId = json.get("id").getAsString();
                nodes.get(nodeId).getBalanceCnt++;
                committees.get(committeeOfNode.get(nodeId)).getBalanceCnt++;
                return;
            }

            case "validateBlock": {
                String nodeId = json.get("id").getAsString();
                nodes.get(nodeId).validateBlockCnt++;
                committees.get(committeeOfNode.get(nodeId)).validateBlockCnt++;
                return;
            }

            case "mine": {
                String nodeId = json.get("id").getAsString();
                nodes.get(nodeId).mineCnt++;
                committees.get(committeeOfNode.get(nodeId)).mineCnt++;
                currentBlockIdx = json.get("block").getAsInt();
                return;
            }

            case "updateUTXO": {
                String nodeId = json.get("id").getAsString();
                nodes.get(nodeId).updateRocksCnt++;
                committees.get(committeeOfNode.get(nodeId)).updateRocksCnt++;
                return;
            }

            case "validateTransaction": {
                String nodeId = json.get("id").getAsString();
                nodes.get(nodeId).validateTransactionCnt++;
                committees.get(committeeOfNode.get(nodeId)).validateTransactionCnt++;
            }
        }

    }

    private static void handleAPI(JsonObject json) {
        switch (json.get("task").getAsString()) {
            case "balance":
                balanceCnt++;
                break;
            case "buy":
                buyCnt++;
                break;
            case "login":
                loginCnt++;
                break;
            case "register":
                registerCnt++;
                break;
            case "sell":
                sellCnt++;
                break;
            case "transfer":
                transferCnt++;
        }
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
