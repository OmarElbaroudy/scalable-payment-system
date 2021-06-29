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

public class CommitteesController implements Initializable {


    public PieChart balance;
    public PieChart updateRocks;
    public BarChart<String, Number> mine;
    public BarChart<String, Number> validateBlock;
    public BarChart<String, Number> createTransaction;
    public BarChart<String, Number> validateTransaction;
    public CategoryAxis createTransactionX;
    public NumberAxis createTransactionY;
    public CategoryAxis mineX;
    public NumberAxis mineY;
    public CategoryAxis validateTransactionX;
    public NumberAxis validateTransactionY;
    public CategoryAxis validateBlockX;
    public NumberAxis validateBlockY;

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


    private void update(){
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
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            int cnt = StatsContainer.getCommitteeValidateTransactionCnt(cur);
            series.getData().add(new XYChart.Data<>(String.valueOf(cur), cnt));
            validateTransaction.getData().add(series);
        }

    }

    private void updateRocks() {
        updateRocks.getData().clear();
        for (int cur : StatsContainer.mp.keySet()) {
            int cnt = StatsContainer.getCommitteeUpdateRocksCnt(cur);
            updateRocks.getData().add(new PieChart.Data(String.valueOf(cur), cnt));
        }
    }

    private void updateValidateBlock() {
        validateBlock.getData().clear();
        validateBlockX.setLabel("Committee number");
        validateBlockY.setLabel("Number of Validate Block Requests");

        for (int cur : StatsContainer.mp.keySet()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            int cnt = StatsContainer.getCommitteeValidateBlockCnt(cur);
            series.getData().add(new XYChart.Data<>(String.valueOf(cur), cnt));
            validateBlock.getData().add(series);
        }

    }

    private void updateBalance() {
        balance.getData().clear();
        for (int cur : StatsContainer.mp.keySet()) {
            int cnt = StatsContainer.getCommitteeBalanceCnt(cur);
            balance.getData().add(new PieChart.Data(String.valueOf(cur), cnt));
        }
    }


    private void updateMine() {
        mine.getData().clear();
        mineX.setLabel("Committee number");
        mineY.setLabel("Number of Mine Requests");

        for (int cur : StatsContainer.mp.keySet()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            int cnt = StatsContainer.getCommitteeMineCnt(cur);
            series.getData().add(new XYChart.Data<>(String.valueOf(cur), cnt));
            mine.getData().add(series);
        }
    }

    private void updateCreateTransaction() {
        createTransaction.getData().clear();
        createTransactionX.setLabel("Committee number");
        createTransactionY.setLabel("Number of Create Transactions Requests");

        for (int cur : StatsContainer.mp.keySet()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            int cnt = StatsContainer.getCommitteeCreateTransactionCnt(cur);
            series.getData().add(new XYChart.Data<>(String.valueOf(cur), cnt));
            createTransaction.getData().add(series);
        }
    }
}
