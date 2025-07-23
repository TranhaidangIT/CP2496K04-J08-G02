module cemina_management {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics; // Rất cần thiết
    requires javafx.base;     // Rất cần thiết

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.bootstrapfx.core;

    opens application to javafx.fxml;
    exports application;

    opens controllerAdmin to javafx.fxml;
    exports controllerAdmin;
    opens controllerEmployees to javafx.fxml, javafx.graphics;
    // opens models to javafx.base; // Chỉ cần nếu dùng PropertyValueFactory
    // exports models;
}