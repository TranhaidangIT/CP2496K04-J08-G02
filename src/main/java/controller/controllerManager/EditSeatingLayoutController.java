package controller.controllerManager;

import dao.ScreeningRoomDAO;
import dao.SeatDAO;
import dao.SeatTypeDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import models.ScreeningRoom;
import models.Seat;
import models.SeatType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditSeatingLayoutController {

    @FXML private AnchorPane anchorPane;
    @FXML private AnchorPane seatingArea;
    @FXML private GridPane seatGrid;
    @FXML private Button btnEditLayout;

    @FXML private AnchorPane legendStandardColor;
    @FXML private Label legendStandardLabel;
    @FXML private AnchorPane legendVIPColor;
    @FXML private Label legendVIPLabel;
    @FXML private AnchorPane legendSweetboxColor;
    @FXML private Label legendSweetboxLabel;
    @FXML private AnchorPane legendGoldColor;
    @FXML private Label legendGoldLabel;

    @FXML private Label lblTotalCapacity;

    private List<Seat> seatsInRoom;
    private List<SeatType> allSeatTypes;
    private ScreeningRoom currentRoom;

    private int activeSeatCount;

    @FXML
    public void initialize() {
        seatingArea.setStyle("-fx-background-color: #f0f0f0;");
        seatGrid.setHgap(5);
        seatGrid.setVgap(5);
    }

    public void setRoomData(ScreeningRoom room) {
        this.currentRoom = room;

        loadSeatTypes();
        seatsInRoom = SeatDAO.getSeatsByRoomId(room.getRoomId());

        if (seatsInRoom.isEmpty()) {
            System.err.println("No seats found for room " + room.getRoomNumber());
            return;
        }

        int maxRow = 0, maxCol = 0;
        for (Seat s : seatsInRoom) {
            int rowIndex = s.getSeatRow() - 'A';
            maxRow = Math.max(maxRow, rowIndex);
            maxCol = Math.max(maxCol, s.getSeatColumn());
        }

        generateSeatGrid(maxRow + 1, maxCol);

        // Cập nhật hiển thị legend
        Set<String> seatTypesInRoom = new HashSet<>();
        for (Seat seat : seatsInRoom) {
            SeatType type = seat.getSeatType();
            if (type != null && type.getSeatTypeName() != null) {
                seatTypesInRoom.add(type.getSeatTypeName().toLowerCase());
            }
        }
        updateLegendVisibility(seatTypesInRoom);

        updateActiveSeatCount();
    }

    private void loadSeatTypes() {
        allSeatTypes = SeatTypeDAO.getAllSeatTypes();
    }

    private void updateLegendVisibility(Set<String> seatTypesInRoom) {
        legendStandardColor.setVisible(false);
        legendStandardLabel.setVisible(false);
        legendVIPColor.setVisible(false);
        legendVIPLabel.setVisible(false);
        legendSweetboxColor.setVisible(false);
        legendSweetboxLabel.setVisible(false);
        legendGoldColor.setVisible(false);
        legendGoldLabel.setVisible(false);

        for (String type : seatTypesInRoom) {
            switch (type) {
                case "standard":
                    legendStandardColor.setVisible(true);
                    legendStandardLabel.setVisible(true);
                    break;
                case "vip":
                    legendVIPColor.setVisible(true);
                    legendVIPLabel.setVisible(true);
                    break;
                case "sweetbox":
                    legendSweetboxColor.setVisible(true);
                    legendSweetboxLabel.setVisible(true);
                    break;
                case "gold":
                    legendGoldColor.setVisible(true);
                    legendGoldLabel.setVisible(true);
                    break;
            }
        }
    }

    private void generateSeatGrid(int rowCount, int columnCount) {
        seatGrid.getChildren().clear();

        // Header cột
        for (int col = 1; col <= columnCount; col++) {
            Label colLabel = new Label(String.valueOf(col));
            colLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");
            seatGrid.add(colLabel, col, 0);
        }

        // Header hàng
        for (int row = 1; row <= rowCount; row++) {
            char rowChar = (char) ('A' + row - 1);
            Label rowLabel = new Label(String.valueOf(rowChar));
            rowLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");
            seatGrid.add(rowLabel, 0, row);
        }

        // Ghế
        for (Seat seat : seatsInRoom) {
            int rowIndex = seat.getSeatRow() - 'A' + 1;
            int colIndex = seat.getSeatColumn();
            String seatLabel = seat.getSeatRow() + String.valueOf(seat.getSeatColumn());

            Rectangle rect = new Rectangle(30, 30);
            String seatTypeName = (seat.getSeatType() != null) ? seat.getSeatType().getSeatTypeName() : "";
            Color originalColor = getColorForSeatType(seatTypeName);

            if (seat.isActive()) {
                rect.setFill(originalColor);
            } else {
                rect.setFill(Color.LIGHTGRAY);
            }
            rect.setStroke(Color.WHITE);
            rect.setStrokeWidth(1);

            Label label = new Label(seatLabel);
            label.setStyle("-fx-font-size: 10px; -fx-text-fill: white; -fx-font-weight: bold;");

            StackPane seatPane = new StackPane(rect, label);
            seatPane.setId(seatLabel);

            seatPane.setOnMouseClicked(event -> {
                if (seat.isActive()) {
                    if (rect.getStroke() != Color.RED) {
                        rect.setStroke(Color.RED);
                        rect.setStrokeWidth(2);
                        for (Node node : seatGrid.getChildren()) {
                            if (node instanceof StackPane && node != seatPane) {
                                Node child = ((StackPane) node).getChildren().get(0);
                                if (child instanceof Rectangle) {
                                    ((Rectangle) child).setStroke(Color.WHITE);
                                    ((Rectangle) child).setStrokeWidth(1);
                                }
                            }
                        }
                    } else {
                        rect.setFill(Color.LIGHTGRAY);
                        rect.setStroke(Color.TRANSPARENT);
                        seat.setActive(false);
                    }
                } else {
                    rect.setFill(originalColor);
                    rect.setStroke(Color.WHITE);
                    rect.setStrokeWidth(1);
                    seat.setActive(true);
                }

                updateActiveSeatCount();
                System.out.println("Click " + seatLabel + " -> active: " + seat.isActive());
            });

            seatGrid.add(seatPane, colIndex, rowIndex);
        }
    }

    private void updateActiveSeatCount() {
        activeSeatCount = (int) seatsInRoom.stream().filter(Seat::isActive).count();
        if (lblTotalCapacity != null) {
            lblTotalCapacity.setText(String.valueOf(activeSeatCount));
        }
        currentRoom.setTotalCapacity(activeSeatCount);
    }

    private Color getColorForSeatType(String typeName) {
        if (typeName == null) return Color.GRAY;

        switch (typeName.toLowerCase()) {
            case "standard": return Color.HOTPINK;
            case "vip": return Color.DEEPSKYBLUE;
            case "sweetbox": return Color.CRIMSON;
            case "gold": return Color.GOLD;
            default: return Color.GRAY;
        }
    }

    public void handleUpdate(ActionEvent actionEvent) {
        if (currentRoom == null || seatsInRoom == null) {
            System.err.println("Room or seat data is missing");
            return;
        }

        ScreeningRoomDAO.updateRoomCapacity(currentRoom.getRoomId(), activeSeatCount);

        boolean success = SeatDAO.updateSeatsInRoom(currentRoom.getRoomId(), seatsInRoom);

        Alert alert;
        if (success) {
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Update Successful");
            alert.setContentText("Seating layout updated successfully!");
            alert.showAndWait();
            handleCancel(actionEvent);
        } else {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Update Failed");
            alert.setContentText("Failed to update seating layout.");
            alert.showAndWait();
        }
    }

    public void handleCancel(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
