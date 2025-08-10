package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DashboardController {
    // Các nút điều hướng cho Manager
    @FXML private Button btnMovie;
    @FXML private Button btnRoom;
    @FXML private Button btnService;
    @FXML private Button btnLocker;
    @FXML private Button btnShowtime;
    @FXML private Button btnOverviewManager;
    @FXML private Button btnLogoutManager;

    @FXML private Pane mainContent;


    // List of buttons for easy management
    private Button[] navigationButtons;

    // Cache for preloaded FXML content
    private final Map<String, Node> viewCache = new HashMap<>();

    @FXML
    public void initialize() {
        // Initialize the array of navigation buttons
        navigationButtons = new Button[] {
                btnMovie, btnRoom, btnService, btnLocker, btnShowtime, btnOverviewManager, btnLogoutManager
        };

        // Apply .btnSelect style to all buttons
        for (Button button : navigationButtons) {
            button.getStyleClass().add("btnSelect");
        }

        // Preload all FXML views
        preloadViews();

        // Set default selected button and view (e.g., Overview)
        setSelectedButton(btnOverviewManager);
        showView("/views/fxml_Manager/Overview.fxml");
    }

    private void preloadViews() {
        // List of FXML paths to preload
        String[] fxmlPaths = {
                "/views/fxml_Manager/Overview.fxml",
                "/views/fxml_Manager/Movie/MovieList.fxml",
                "/views/fxml_Manager/ScreeningRoom/RoomList.fxml",
                "/views/fxml_Manager/Service/ServiceList.fxml",
                "/views/fxml_Manager/Locker/LockerList.fxml",
                "/views/fxml_Manager/Showtime/ShowtimeList.fxml"
        };

        // Load each FXML and store in cache
        for (String fxmlPath : fxmlPaths) {
            try {
                Node content = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
                viewCache.put(fxmlPath, content);
            } catch (IOException e) {
                System.err.println("Error preloading FXML: " + fxmlPath + " - " + e.getMessage());
            }
        }
    }

    private void showView(String fxmlPath) {
        // Retrieve cached view or load if not cached
        Node content = viewCache.computeIfAbsent(fxmlPath, path -> {
            try {
                return FXMLLoader.load(Objects.requireNonNull(getClass().getResource(path)));
            } catch (IOException e) {
                System.err.println("Error loading UI: " + e.getMessage());
                return null;
            }
        });

        if (content != null) {
            mainContent.getChildren().setAll(content);
        }
    }

    // Method to set the selected button style
    private void setSelectedButton(Button selectedButton) {
        // Reset style for all buttons
        for (Button button : navigationButtons) {
            button.getStyleClass().remove("selected-button");
        }
        // Apply selected style to the clicked button
        selectedButton.getStyleClass().add("selected-button");
    }

    // ------------------MANAGER-------------------
    @FXML
    void onMovieClickedbyManager(ActionEvent event) {
        showView("/views/fxml_Manager/Movie/MovieList.fxml");
        setSelectedButton(btnMovie);
    }

    @FXML
    void onRoomClickedbbyManager(ActionEvent event) {
        showView("/views/fxml_Manager/ScreeningRoom/RoomList.fxml");
        setSelectedButton(btnRoom);
    }

    @FXML
    void onServiceClicked(ActionEvent event) {
        showView("/views/fxml_Manager/Service/ServiceList.fxml");
        setSelectedButton(btnService);
    }

    @FXML
    void onShowtimeClickedbyManager(ActionEvent event) {
        showView("/views/fxml_Manager/Showtime/ShowtimeList.fxml");
        setSelectedButton(btnShowtime);
    }

    @FXML
    void onOverviewClickedbyManager(ActionEvent event) {
        showView("/views/fxml_Manager/Overview.fxml");
        setSelectedButton(btnOverviewManager);
    }

    @FXML
    void onLockerClicked(ActionEvent event) {
        showView("/views/fxml_Manager/Locker/LockerList.fxml");
        setSelectedButton(btnLocker);
    }

    @FXML
    private void onLogoutClicked(ActionEvent event) {
        try {
            // Load login.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Admin/login.fxml"));
            Parent loginRoot = loader.load();

            // Create a new stage for the login screen
            Stage loginStage = new Stage();
            loginStage.setTitle("Login");
            loginStage.setScene(new Scene(loginRoot));
            loginStage.show();

            // Close the current window (Dashboard)
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            System.err.println("Error loading login UI: " + e.getMessage());
        }
        setSelectedButton(btnLogoutManager);
    }
}