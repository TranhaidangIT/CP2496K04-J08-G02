package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class DashboardController {

    @FXML
    public AnchorPane centerContent;

    // Singleton cho phép các controller khác gọi loadUI
    private static DashboardController instance;

    @FXML
    public void initialize() {
        instance = this;
        loadUI("/views/fxml_Manager/Overview.fxml");
    }

    public static DashboardController getInstance() {
        return instance;
    }

    public void loadUI(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent pane = loader.load();
            centerContent.getChildren().setAll(pane);
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlPath + ": " + e.getMessage());
        }
    }

    // ===== SIDEBAR BUTTON HANDLERS =====
    @FXML
    private void onOverviewClicked() {
        loadUI("/views/fxml_Manager/Overview.fxml");
    }

    @FXML
    private void onMovieClicked() {
        loadUI("/views/fxml_Manager/Movie/MovieList.fxml");
    }

    @FXML
    private void onRoomClicked() {
        loadUI("/views/fxml_Manager/ScreeningRoom/RoomList.fxml");
    }

    @FXML
    private void onShowtimeClicked() {
        loadUI("/views/fxml_Manager/Showtime/ShowtimeList.fxml");
    }

    @FXML
    private void onServiceClicked() {
        loadUI("/views/fxml_Manager/Service/ServiceList.fxml");
    }

    @FXML
    private void onLogoutClicked() {
        System.out.println("User clicked logout.");
        // TODO: Thêm logic logout nếu cần
    }
}
