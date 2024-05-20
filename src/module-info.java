module accountLogging {
    requires javafx.fxml;
    requires javafx.controls;
    requires java.sql;
    requires com.google.gson;
    opens Main;
    opens UIfiles;
    opens model;
    opens contollers;
    opens model.interfaces;
    opens model.cellFactories;
    opens model.objects;
}