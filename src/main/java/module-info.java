module cemina_management {
    // JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires javafx.base;

    // External libraries
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
//    requires mssql.jdbc;
    requires java.desktop;

    // Allow FXML to access internal packages
    opens application to javafx.fxml;
    exports application;

    opens controller to javafx.fxml;
    exports controller;

    opens controller.controllerAdmin to javafx.fxml;
    exports controller.controllerAdmin;

    opens controller.controllerEmployees to javafx.fxml;
    exports controller.controllerEmployees;

    opens controller.controllerManager to javafx.fxml;
    exports controller.controllerManager;

    opens models to javafx.base;
    exports models;

    opens configs to javafx.base;
    exports configs;
}
