module application {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.rabbitmq.client;
    requires com.google.gson;

    opens application to javafx.fxml;
    exports application;
    exports application.controllers;
    opens application.controllers to javafx.fxml;
}
