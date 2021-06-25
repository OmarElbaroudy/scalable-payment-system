package application.controllers;

import application.App;
import application.StatsContainer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class StatsController implements Initializable {
    public Label loginCnt;
    public Label registerCnt;
    public Label balanceCnt;
    public Label buyCnt;
    public Label sellCnt;
    public Label transferCnt;
    public Label nodesCnt;
    public Label committeesCnt;
    public Label blockIdx;

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
                new KeyFrame(Duration.seconds(1.0), e -> {
                   update();
                })
        );

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void update(){
        loginCnt.setText(String.valueOf(StatsContainer.getLoginCnt()));
        registerCnt.setText(String.valueOf(StatsContainer.getRegisterCnt()));
        balanceCnt.setText(String.valueOf(StatsContainer.getBalanceCnt()));
        buyCnt.setText(String.valueOf(StatsContainer.getBuyCnt()));
        sellCnt.setText(String.valueOf(StatsContainer.getSellCnt()));
        transferCnt.setText(String.valueOf(StatsContainer.getTransferCnt()));
        nodesCnt.setText(String.valueOf(StatsContainer.getNodesCnt()));
        committeesCnt.setText(String.valueOf(StatsContainer.getCommitteesCnt()));
        int idx = StatsContainer.getCurrentBlockIdx();
        blockIdx.setText(idx == 1 ? "Genesis" : String.valueOf(idx));
    }
}
