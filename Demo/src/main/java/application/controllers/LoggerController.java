package application.controllers;

import application.App;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

public class LoggerController implements Initializable {

    public static boolean change = false;

    public static final HashMap<String, ArrayList<Text>> mp = new HashMap<>();
    public static final ArrayList<Text> APILoggerQueue = new ArrayList<>();
    public static final ArrayList<Text> signalingLoggerQueue = new ArrayList<>();
    public static final ArrayList<Text> fstLoggerQueue = new ArrayList<>();
    public static final ArrayList<Text> sndLoggerQueue = new ArrayList<>();
    public static final ArrayList<Text> thrdLoggerQueue = new ArrayList<>();
    public static final ArrayList<Text> fourthLoggerQueue = new ArrayList<>();
    public static final ArrayList<Text> fifthLoggerQueue = new ArrayList<>();
    public static final ArrayList<Text> sixthLoggerQueue = new ArrayList<>();
    public static final ArrayList<Text> seventhLoggerQueue = new ArrayList<>();
    public static final ArrayList<Text> eighthLoggerQueue = new ArrayList<>();

    public ListView<Text> APILogger;
    public ListView<Text> signalingLogger;
    public ListView<Text> fstLogger;
    public ListView<Text> sndLogger;
    public ListView<Text> thrdLogger;
    public ListView<Text> fourthLogger;
    public ListView<Text> fifthLogger;
    public ListView<Text> sixthLogger;
    public ListView<Text> seventhLogger;
    public ListView<Text> eighthLogger;

    public static void log(Text text, String nodeId) {
        mp.get(nodeId).add(text);
        change = true;
    }

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
                    if(change){
                        update();
                    }

                })
        );

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void update(){
        updateLogger(APILogger, APILoggerQueue);
        updateLogger(signalingLogger, signalingLoggerQueue);
        updateLogger(fstLogger, fstLoggerQueue);
        updateLogger(sndLogger, sndLoggerQueue);
        updateLogger(thrdLogger, thrdLoggerQueue);
        updateLogger(fourthLogger, fourthLoggerQueue);
        updateLogger(fifthLogger, fifthLoggerQueue);
        updateLogger(sixthLogger, sixthLoggerQueue);
        updateLogger(seventhLogger, seventhLoggerQueue);
        updateLogger(eighthLogger, eighthLoggerQueue);
        change = false;
    }

    private void updateLogger(ListView<Text> logger, ArrayList<Text> queue) {
        if (logger.getItems().size() < queue.size()) {
            int idx = logger.getItems().size();
            for (; idx < queue.size(); idx++) {
                logger.getItems().add(queue.get(idx));
            }
        }
    }

}
