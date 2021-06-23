package application.controllers;

import application.App;
import com.google.gson.JsonObject;

import java.io.IOException;

public class LoggerController {

    public static void update(JsonObject json) {

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
}
