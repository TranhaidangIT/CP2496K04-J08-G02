package controller.controllerManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.geometry.Insets;
import models.Seat;

import java.util.ArrayList;
import java.util.List;

public class EditSeatingLayoutController {

    @FXML
    private GridPane seatGrid;

    @FXML
    private Pane centerZone;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    // Mock data - Replace this with actual DAO call
    private List<Seat> seatList = new ArrayList<>();

    private final int maxRows = 10;
    private final int maxCols = 12;

    @FXML
    public void initialize() {
        loadSeats();
        displaySeats();
    }

    private void loadSeats() {
        // Dummy data generation
        seatList.clear();
        int id = 1;
        for (int row = 0; row < maxRows; row++) {
            for (int col = 0; col < maxCols; col++) {
                String status = (Math.random() < 0.8) ? "active" : "inactive";
                String type = (Math.random() < 0.1) ? "maintenance" : "normal";
                seatList.add(new Seat(id++, 1, row, col, type, status));
            }
        }
    }

    private void displaySeats() {
        seatGrid.getChildren().clear();

        for (Seat seat : seatList) {
            Button seatButton = new Button(getSeatLabel(seat));
            seatButton.setPrefSize(40, 40);
            updateSeatButtonStyle(seatButton, seat);

            seatButton.setOnAction(event -> {
                cycleSeatState(seat);
                updateSeatButtonStyle(seatButton, seat);
            });

            seatGrid.add(seatButton, seat.getSeatColumn(), seat.getSeatRow());
        }
    }

    private String getSeatLabel(Seat seat) {
        char rowChar = (char) ('A' + seat.getSeatRow());
        return rowChar + String.valueOf(seat.getSeatColumn() + 1);
    }

    private void cycleSeatState(Seat seat) {
        if ("maintenance".equalsIgnoreCase(seat.getSeatType())) {
            seat.setSeatType("normal");
            seat.setIsActive("active");
        } else if ("inactive".equalsIgnoreCase(seat.getIsActive())) {
            seat.setIsActive("active");
        } else if ("active".equalsIgnoreCase(seat.getIsActive())) {
            seat.setIsActive("inactive");
        }
    }

    private void updateSeatButtonStyle(Button btn, Seat seat) {
        if ("maintenance".equalsIgnoreCase(seat.getSeatType())) {
            btn.setStyle("-fx-background-color: orange; -fx-text-fill: white;");
        } else if ("inactive".equalsIgnoreCase(seat.getIsActive())) {
            btn.setStyle("-fx-background-color: gray; -fx-text-fill: white;");
        } else {
            btn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        }
    }

    @FXML
    private void handleSave() {
        // Replace this with actual saving logic to database
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Save Changes");
        alert.setHeaderText(null);
        alert.setContentText("Seat layout saved successfully!");
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        // You can add confirmation dialog here if needed
        // For now, we simply close or reload
        loadSeats();
        displaySeats();
    }

    public void setRoomId(int roomId) {
    }
}
