package controller.controllerEmployees;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.io.IOException;

public class SeatDemoController {

    @FXML private Label messageLabel;
    @FXML private GridPane seatGrid;
    @FXML private Button backButton;
    @FXML private Label totalSeatsLabel;
    @FXML private Label availableSeatsLabel;

    private int totalSeats = 100;
    private int availableSeats = 85; // Demo: some seats are occupied

    @FXML
    public void initialize() {
        System.out.println("Initializing SeatDemoController");

        // Check if FXML elements are properly injected
        if (seatGrid == null) {
            System.err.println("seatGrid is null! Check fx:id in SeatDemo.fxml");
        }
        if (messageLabel == null) {
            System.err.println("messageLabel is null! Check fx:id in SeatDemo.fxml");
        }
        if (backButton == null) {
            System.err.println("backButton is null! Check fx:id in SeatDemo.fxml");
        }

        setupUI();
        loadDemoSeats();
        updateSeatInfo();
    }

    private void setupUI() {
        // Style the message label
        if (messageLabel != null) {
            messageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: #fdf2f2; " +
                    "-fx-background-radius: 8px; -fx-padding: 15px; " +
                    "-fx-border-color: #e74c3c; -fx-border-width: 1px; " +
                    "-fx-border-radius: 8px;");
        }

        // Style the back button
        if (backButton != null) {
            backButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-font-weight: bold; " +
                    "-fx-background-radius: 8px; -fx-cursor: hand; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);");
        }
    }

    private void loadDemoSeats() {
        if (seatGrid == null) return;

        seatGrid.getChildren().clear();
        seatGrid.setAlignment(Pos.CENTER);
        seatGrid.setHgap(8);
        seatGrid.setVgap(8);

        System.out.println("Creating 10x10 demo seat grid");

        // Create 10x10 demo seat grid
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                Button seat = createSeatButton(row, col);
                seatGrid.add(seat, col, row);
            }
        }

        System.out.println("Number of seats added to seatGrid: " + seatGrid.getChildren().size());
    }

    private Button createSeatButton(int row, int col) {
        String seatLabel = (char)('A' + row) + String.valueOf(col + 1);
        Button seat = new Button(seatLabel);

        // Set button size
        seat.setPrefSize(45, 45);
        seat.setMinSize(45, 45);
        seat.setMaxSize(45, 45);

        // Determine seat type and color
        SeatInfo seatInfo = getSeatInfo(row);

        // Randomly make some seats occupied for demo
        boolean isOccupied = Math.random() < 0.15; // 15% occupied

        if (isOccupied) {
            seat.setStyle(getOccupiedSeatStyle());
            seat.setDisable(true);
        } else {
            seat.setStyle(getAvailableSeatStyle(seatInfo.color));
        }

        // Set hover effects and click handler
        setupSeatInteraction(seat, seatInfo, isOccupied);

        return seat;
    }

    private void setupSeatInteraction(Button seat, SeatInfo seatInfo, boolean isOccupied) {
        if (!isOccupied) {
            // Hover effects
            seat.setOnMouseEntered(e -> {
                seat.setStyle(getHoverSeatStyle(seatInfo.color));
                seat.setScaleX(1.1);
                seat.setScaleY(1.1);
            });

            seat.setOnMouseExited(e -> {
                seat.setStyle(getAvailableSeatStyle(seatInfo.color));
                seat.setScaleX(1.0);
                seat.setScaleY(1.0);
            });

            // Click handler
            seat.setOnAction(e -> {
                System.out.println("Demo seat clicked: " + seat.getText() + " (" + seatInfo.type + ")");
                showSeatSelectionAlert(seat.getText(), seatInfo.type);
            });
        }
    }

    private SeatInfo getSeatInfo(int row) {
        if (row < 3) { // Rows A, B, C (Standard)
            return new SeatInfo("#FF6B9D", "Standard"); // Pink
        } else if (row == 9) { // Row J (Sweet Box)
            return new SeatInfo("#E74C3C", "Sweet Box"); // Red
        } else { // Rows D, E, F, G, H, I (VIP)
            return new SeatInfo("#3498DB", "VIP"); // Blue
        }
    }

    private String getAvailableSeatStyle(String color) {
        return String.format("-fx-background-color: %s; " +
                "-fx-background-radius: 10px; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 12px; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: #34495e; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 10px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 3, 0, 0, 1);", color);
    }

    private String getHoverSeatStyle(String color) {
        return String.format("-fx-background-color: derive(%s, -20%%); " +
                "-fx-background-radius: 10px; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 12px; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: #2c3e50; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 10px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0, 0, 2);", color);
    }

    private String getOccupiedSeatStyle() {
        return "-fx-background-color: #7f8c8d; " +
                "-fx-background-radius: 10px; " +
                "-fx-text-fill: #bdc3c7; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 12px; " +
                "-fx-border-color: #95a5a6; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 10px; " +
                "-fx-opacity: 0.6;";
    }

    private void updateSeatInfo() {
        if (totalSeatsLabel != null) {
            totalSeatsLabel.setText("Total Seats: " + totalSeats);
        }
        if (availableSeatsLabel != null) {
            availableSeatsLabel.setText("Available: " + availableSeats);
        }
    }

    private void showSeatSelectionAlert(String seatNumber, String seatType) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Chưa chọn suất chiếu");
        alert.setHeaderText("Không thể chọn ghế");
        alert.setContentText(String.format("Bạn đã chọn ghế %s (%s).\nVui lòng chọn phim và suất chiếu trước khi chọn ghế.",
                seatNumber, seatType));

        // Tùy chỉnh DialogPane để hiển thị toàn bộ nội dung
        alert.getDialogPane().setStyle("-fx-font-family: Arial; -fx-font-size: 14px;");
        alert.getDialogPane().setMinWidth(400); // Tăng chiều rộng tối thiểu
        alert.getDialogPane().setMinHeight(150); // Tăng chiều cao tối thiểu
        alert.getDialogPane().setPadding(new Insets(15)); // Thêm padding
        alert.getDialogPane().setExpandableContent(null); // Vô hiệu hóa nội dung mở rộng
        alert.getDialogPane().setExpanded(false); // Đảm bảo không thu gọn

        alert.showAndWait().ifPresent(response -> handleBack());
    }

    @FXML
    private void handleBack() {
        try {
            System.out.println("Back to movie selection, loading ListMovies.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/ListMovies.fxml"));
            Parent listMoviesRoot = loader.load();
            ListMoviesController controller = loader.getController();

            AnchorPane parent = (AnchorPane) backButton.getScene().lookup("#contentArea");
            if (parent != null) {
                controller.setContentArea(parent);
                parent.getChildren().setAll(listMoviesRoot);
                AnchorPane.setTopAnchor(listMoviesRoot, 0.0);
                AnchorPane.setBottomAnchor(listMoviesRoot, 0.0);
                AnchorPane.setLeftAnchor(listMoviesRoot, 0.0);
                AnchorPane.setRightAnchor(listMoviesRoot, 0.0);
            } else {
                System.err.println("Could not find #contentArea in scene!");
            }
        } catch (IOException ex) {
            System.err.println("Error loading ListMovies.fxml: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Helper class for seat information
    private static class SeatInfo {
        final String color;
        final String type;

        SeatInfo(String color, String type) {
            this.color = color;
            this.type = type;
        }
    }
}