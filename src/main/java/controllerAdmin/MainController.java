package controllerAdmin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;

public class MainController {
    @FXML
    private StackPane mainContent;

    public void loadContent(String fxmlPath) {
        try {
            Node content = FXMLLoader.load(getClass().getResource(fxmlPath));
            mainContent.getChildren().setAll(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
