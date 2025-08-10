package controller.controllerManager;

import dao.SeatDAO;
import dao.SeatTypeDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.ScreeningRoom;
import models.Seat;
import models.SeatType;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeatingLayoutController {

    @FXML private AnchorPane anchorPane;
    @FXML private AnchorPane seatingArea;
    @FXML private GridPane seatGrid;
    @FXML private Button btnEditLayout;

    // Legends
    @FXML private AnchorPane legendStandardColor;
    @FXML private Label legendStandardLabel;
    @FXML private AnchorPane legendVIPColor;
    @FXML private Label legendVIPLabel;
    @FXML private AnchorPane legendSweetboxColor;
    @FXML private Label legendSweetboxLabel;
    @FXML private AnchorPane legendGoldColor;
    @FXML private Label legendGoldLabel;

    private List<Seat> seatsInRoom;
    private List<SeatType> allSeatTypes;
    private ScreeningRoom currentRoom;

    @FXML
    public void initialize() {
        seatingArea.setStyle("-fx-background-color: #f0f0f0;");
        seatGrid.setHgap(5);
        seatGrid.setVgap(5);
    }

    public void setRoomData(ScreeningRoom room) {
        this.currentRoom = room;

        // Load seat types from DB
        loadSeatTypes();

        // Load seats for the room
        seatsInRoom = SeatDAO.getSeatsByRoomId(room.getRoomId());
        if (seatsInRoom.isEmpty()) {
            System.err.println("No seats found for room " + room.getRoomNumber());
            return;
        }

        // Calculate max row and column
        int maxRow = 0, maxCol = 0;
        for (Seat s : seatsInRoom) {
            int rowIndex = s.getSeatRow() - 'A';
            maxRow = Math.max(maxRow, rowIndex);
            maxCol = Math.max(maxCol, s.getSeatColumn());
        }

        generateSeatGrid(maxRow + 1, maxCol);

        // Update legends based on seat types
        Set<String> seatTypesInRoom = new HashSet<>();
        for (Seat seat : seatsInRoom) {
            if (!seat.isActive()) continue;
            SeatType type = seat.getSeatType();
            if (type != null && type.getSeatTypeName() != null) {
                seatTypesInRoom.add(type.getSeatTypeName().toLowerCase());
            }
        }
        updateLegendVisibility(seatTypesInRoom);
    }

    private void loadSeatTypes() {
        allSeatTypes = SeatTypeDAO.getAllSeatTypes();
    }

    private void updateLegendVisibility(Set<String> seatTypesInRoom) {
        // Hide all legends first
        legendStandardColor.setVisible(false);
        legendStandardLabel.setVisible(false);
        legendVIPColor.setVisible(false);
        legendVIPLabel.setVisible(false);
        legendSweetboxColor.setVisible(false);
        legendSweetboxLabel.setVisible(false);
        legendGoldColor.setVisible(false);
        legendGoldLabel.setVisible(false);

        // Show only the legends present in the room
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

        // Column headers (1, 2, ..., columnCount)
        for (int col = 1; col <= columnCount; col++) {
            Label colLabel = new Label(String.valueOf(col));
            colLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");
            seatGrid.add(colLabel, col, 0);
        }

        // Row headers (A, B, ..., Z)
        for (int row = 1; row <= rowCount; row++) {
            char rowChar = (char) ('A' + row - 1);
            Label rowLabel = new Label(String.valueOf(rowChar));
            rowLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");
            seatGrid.add(rowLabel, 0, row);
        }

        for (Seat seat : seatsInRoom) {
            if (!seat.isActive()) continue;

            int rowIndex = seat.getSeatRow() - 'A' + 1;
            int colIndex = seat.getSeatColumn(); // 1-based

            String seatLabel = seat.getSeatRow() + String.valueOf(seat.getSeatColumn());

            // Rectangle for seat background
            Rectangle rect = new Rectangle(30, 30);
            String seatTypeName = (seat.getSeatType() != null) ? seat.getSeatType().getSeatTypeName() : "";
            Color fillColor = getColorForSeatType(seatTypeName);
            rect.setFill(fillColor);
            rect.setStroke(Color.WHITE);
            rect.setStrokeWidth(1);

            // Label for seat identifier
            Label label = new Label(seatLabel);
            label.setStyle("-fx-font-size: 10px; -fx-text-fill: white; -fx-font-weight: bold;");

            // StackPane to hold Rectangle and Label
            StackPane seatPane = new StackPane(rect, label);
            seatPane.setId(seatLabel);

            // Click handling for selection
            seatPane.setOnMouseClicked(e -> {
                System.out.println("Click " + seatLabel);

                // Update stroke for selected seat
                rect.setStroke(Color.RED);
                rect.setStrokeWidth(3);

                // Reset stroke for other seats
                for (Node node : seatGrid.getChildren()) {
                    if (node instanceof StackPane && node != seatPane) {
                        Node child = ((StackPane) node).getChildren().get(0);
                        if (child instanceof Rectangle) {
                            ((Rectangle) child).setStroke(Color.WHITE);
                            ((Rectangle) child).setStrokeWidth(1);
                        }
                    }
                }
            });

            seatGrid.add(seatPane, colIndex, rowIndex);
        }
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

    @FXML
    void handleEditLayout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Manager/ScreeningRoom/EditSeatingLayout.fxml"));
            Parent root = loader.load();

            EditSeatingLayoutController controller = loader.getController();
            controller.setRoomData(currentRoom);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Seating Layout");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh data after edit is complete
            if (currentRoom != null) {
                setRoomData(currentRoom);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}