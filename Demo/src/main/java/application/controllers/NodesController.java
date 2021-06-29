package application.controllers;

import application.App;
import application.StatsContainer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class NodesController implements Initializable {

    public PieChart balance;
    public PieChart updateRocks;
    public BarChart<String, Number> mine;
    public BarChart<String, Number> validateBlock;
    public BarChart<String, Number> createTransaction;
    public BarChart<String, Number> validateTransaction;
    public CategoryAxis createTransactionX;
    public CategoryAxis validateTransactionX;
    public NumberAxis validateTransactionY;
    public NumberAxis createTransactionY;
    public CategoryAxis validateBlockX;
    public NumberAxis validateBlockY;
    public CategoryAxis mineX;
    public NumberAxis mineY;

    public void switchToLoggerScene() throws IOException {
        App.setRoot("logger");
    }

    public void switchToStatsScene() throws IOException {
        App.setRoot("stats");
    }

    public void switchToCommitteesScene() throws IOException {
        App.setRoot("committees");
    }

    public void switchToNodesScene() throws IOException {
        App.setRoot("nodes");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        update();

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(15.0), e -> {
                    update();
                })
        );

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void update() {
        updateCreateTransaction();
        updateMine();
        updateBalance();
        updateValidateBlock();
        updateRocks();
        updateValidateTransaction();
    }

    private void updateValidateTransaction() {
        validateTransaction.getData().clear();
        validateTransactionX.setLabel("Committee number");
        validateTransactionY.setLabel("Number of Validate Transaction Requests");

        for (int cur : StatsContainer.mp.keySet()) {
            for (String id : StatsContainer.mp.get(cur)) {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                int cnt = StatsContainer.getNodeValidateTransactionCnt(id);
                series.getData().add(new XYChart.Data<>(id, cnt));
                validateTransaction.getData().add(series);
            }
        }
    }

    private void updateRocks() {
        updateRocks.getData().clear();
        for (int cur : StatsContainer.mp.keySet()) {
            for (String id : StatsContainer.mp.get(cur)) {
                int cnt = StatsContainer.getNodeUpdateRocksCnt(id);
                updateRocks.getData().add(new PieChart.Data(id, cnt));
            }
        }
    }

    private void updateValidateBlock() {
        validateBlock.getData().clear();
        validateBlockX.setLabel("Committee number");
        validateBlockY.setLabel("Number of Validate Block Requests");

        for (int cur : StatsContainer.mp.keySet()) {
            for (String id : StatsContainer.mp.get(cur)) {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                int cnt = StatsContainer.getNodeValidateBlockCnt(id);
                series.getData().add(new XYChart.Data<>(id, cnt));
                validateBlock.getData().add(series);
            }
        }

    }

    private void updateBalance() {
        balance.getData().clear();
        for (int cur : StatsContainer.mp.keySet()) {
            for (String id : StatsContainer.mp.get(cur)) {
                int cnt = StatsContainer.getNodeBalanceCnt(id);
                balance.getData().add(new PieChart.Data(id, cnt));
            }
        }
    }


    private void updateMine() {
        mine.getData().clear();
        mineX.setLabel("Committee number");
        mineY.setLabel("Number of Mine Requests");

        for (int cur : StatsContainer.mp.keySet()) {
            for (String id : StatsContainer.mp.get(cur)) {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                int cnt = StatsContainer.getNodeMineCnt(id);
                series.getData().add(new XYChart.Data<>(id, cnt));
                mine.getData().add(series);
            }
        }

    }

    private void updateCreateTransaction() {
        createTransaction.getData().clear();
        createTransactionX.setLabel("Committee number");
        createTransactionY.setLabel("Number of Create Transactions Requests");

        for (int cur : StatsContainer.mp.keySet()) {
            for (String id : StatsContainer.mp.get(cur)) {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                int cnt = StatsContainer.getNodeCreateTransactionCnt(id);
                series.getData().add(new XYChart.Data<>(id, cnt));
                createTransaction.getData().add(series);
            }
        }
    }
}
