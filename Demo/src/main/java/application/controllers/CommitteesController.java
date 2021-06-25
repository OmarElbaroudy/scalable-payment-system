package application.controllers;

import application.App;
import application.StatsContainer;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;

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
        updateCreateTransaction();
        updateMine();
        updateBalance();
        updateValidateBlock();
        updateRocks();
        updateValidateTransaction();
    }

    private void updateValidateTransaction() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Committee number");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Validate Transaction Requests");

        validateTransaction = new BarChart<>(xAxis, yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (int cur : StatsContainer.mp.keySet()) {
            int cnt = StatsContainer.getCommitteeValidateTransactionCnt(cur);
            series.getData().add(new XYChart.Data<>(String.valueOf(cur), cnt));
        }

        validateTransaction.getData().add(series);
    }

    private void updateRocks() {
        updateRocks = new PieChart();
        for (int cur : StatsContainer.mp.keySet()) {
            int cnt = StatsContainer.getCommitteeUpdateRocksCnt(cur);
            updateRocks.getData().add(new PieChart.Data(String.valueOf(cur), cnt));
        }
    }

    private void updateValidateBlock() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Committee number");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Validate Block Requests");

        validateBlock = new BarChart<>(xAxis, yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (int cur : StatsContainer.mp.keySet()) {
            int cnt = StatsContainer.getCommitteeValidateBlockCnt(cur);
            series.getData().add(new XYChart.Data<>(String.valueOf(cur), cnt));
        }

        validateBlock.getData().add(series);
    }

    private void updateBalance() {
        balance = new PieChart();
        for (int cur : StatsContainer.mp.keySet()) {
            int cnt = StatsContainer.getCommitteeBalanceCnt(cur);
            balance.getData().add(new PieChart.Data(String.valueOf(cur), cnt));
        }
    }


    private void updateMine() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Committee number");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Mine Requests");

        mine = new BarChart<>(xAxis, yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (int cur : StatsContainer.mp.keySet()) {
            int cnt = StatsContainer.getCommitteeMineCnt(cur);
            series.getData().add(new XYChart.Data<>(String.valueOf(cur), cnt));
        }

        mine.getData().add(series);
    }

    private void updateCreateTransaction() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Committee number");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Create Transactions Requests");

        createTransaction = new BarChart<>(xAxis, yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (int cur : StatsContainer.mp.keySet()) {
            int cnt = StatsContainer.getCommitteeCreateTransactionCnt(cur);
            series.getData().add(new XYChart.Data<>(String.valueOf(cur), cnt));
        }

        createTransaction.getData().add(series);
    }
}
