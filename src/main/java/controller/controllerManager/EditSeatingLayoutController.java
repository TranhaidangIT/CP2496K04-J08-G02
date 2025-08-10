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

    // Legend elements for seat types color coding
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

    private int activeSeatCount;

    @FXML
    public void initialize() {
        // Set background color of seating area and spacing between seats
        seatingArea.setStyle("-fx-background-color: #f0f0f0;");
        seatGrid.setHgap(5);
        seatGrid.setVgap(5);
    }

    /**
     * Sets the current screening room data, loads seat types,
     * loads seats for the room, and generates the seating grid UI.
     * @param room The ScreeningRoom object to edit
     */
    public void setRoomData(ScreeningRoom room) {
        this.currentRoom = room;

        // Load all seat types from database
        loadSeatTypes();

        // Load all seats for the current room from DB
        seatsInRoom = SeatDAO.getSeatsByRoomId(room.getRoomId());
        if (seatsInRoom.isEmpty()) {
            System.err.println("No seats found for room " + room.getRoomNumber());
            return;
        }

        // Calculate max rows (A-Z) and columns (1-N) based on seats data
        int maxRow = 0, maxCol = 0;
        for (Seat s : seatsInRoom) {
            int rowIndex = s.getSeatRow() - 'A';
            maxRow = Math.max(maxRow, rowIndex);
            maxCol = Math.max(maxCol, s.getSeatColumn());
        }

        // Generate the seat grid UI with proper row and column counts
        generateSeatGrid(maxRow + 1, maxCol);

        // Collect all seat types present in this room for showing legends
        Set<String> seatTypesInRoom = new HashSet<>();
        for (Seat seat : seatsInRoom) {
            SeatType type = seat.getSeatType();
            if (type != null && type.getSeatTypeName() != null) {
                seatTypesInRoom.add(type.getSeatTypeName().toLowerCase());
            }
        }
        // Update legend visibility based on seat types found in the room
        updateLegendVisibility(seatTypesInRoom);
    }

    /**
     * Loads all seat types from the database.
     */
    private void loadSeatTypes() {
        allSeatTypes = SeatTypeDAO.getAllSeatTypes();
    }

    /**
     * Updates the visibility of seat type legends based on seat types in the room.
     * @param seatTypesInRoom Set of seat type names present in the current room
     */
    private void updateLegendVisibility(Set<String> seatTypesInRoom) {
        // Hide all legend elements initially
        legendStandardColor.setVisible(false);
        legendStandardLabel.setVisible(false);
        legendVIPColor.setVisible(false);
        legendVIPLabel.setVisible(false);
        legendSweetboxColor.setVisible(false);
        legendSweetboxLabel.setVisible(false);
        legendGoldColor.setVisible(false);
        legendGoldLabel.setVisible(false);

        // Show only legends for seat types that exist in this room
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

    /**
     * Generates the seating grid layout with seat rectangles and labels.
     * Supports clicking seats to toggle active/inactive status (hide/show).
     * @param rowCount Number of seat rows
     * @param columnCount Number of seat columns
     */
    private void generateSeatGrid(int rowCount, int columnCount) {
        seatGrid.getChildren().clear();

        // Add column headers (1, 2, ..., columnCount)
        for (int col = 1; col <= columnCount; col++) {
            Label colLabel = new Label(String.valueOf(col));
            colLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");
            seatGrid.add(colLabel, col, 0);
        }

        // Add row headers (A, B, ..., based on rowCount)
        for (int row = 1; row <= rowCount; row++) {
            char rowChar = (char) ('A' + row - 1);
            Label rowLabel = new Label(String.valueOf(rowChar));
            rowLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");
            seatGrid.add(rowLabel, 0, row);
        }

        // Add seat rectangles with labels in the grid
        for (Seat seat : seatsInRoom) {
            int rowIndex = seat.getSeatRow() - 'A' + 1; // grid row index (1-based)
            int colIndex = seat.getSeatColumn();       // grid column index

            String seatLabel = seat.getSeatRow() + String.valueOf(seat.getSeatColumn());

            // Create a rectangle representing the seat
            Rectangle rect = new Rectangle(30, 30);
            String seatTypeName = (seat.getSeatType() != null) ? seat.getSeatType().getSeatTypeName() : "";
            Color originalColor = getColorForSeatType(seatTypeName);

            // Fill color: original color if active, light gray if inactive
            if (seat.isActive()) {
                rect.setFill(originalColor);
            } else {
                rect.setFill(Color.LIGHTGRAY);
            }
            rect.setStroke(Color.WHITE);
            rect.setStrokeWidth(1);

            // Label on top of rectangle showing seat code (e.g., A1)
            Label label = new Label(seatLabel);
            label.setStyle("-fx-font-size: 10px; -fx-text-fill: white; -fx-font-weight: bold;");

            StackPane seatPane = new StackPane(rect, label);
            seatPane.setId(seatLabel);

            // Click event handler to toggle seat active status and update UI
            seatPane.setOnMouseClicked(event -> {
                if (seat.isActive()) {
                    if (rect.getStroke() != Color.RED) {
                        // Select seat: add red border
                        rect.setStroke(Color.RED);
                        rect.setStrokeWidth(2);

                        // Remove red border from other seats
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
                        // Hide seat by graying out and removing stroke, mark inactive
                        rect.setFill(Color.LIGHTGRAY);
                        rect.setStroke(Color.TRANSPARENT);
                        seat.setActive(false);
                    }
                } else {
                    // Show seat again with original color and stroke, mark active
                    rect.setFill(originalColor);
                    rect.setStroke(Color.WHITE);
                    rect.setStrokeWidth(1);
                    seat.setActive(true);
                }

                // Debug print seat status
                System.out.println("Click " + seatLabel + " -> active: " + seat.isActive());
            });

            seatGrid.add(seatPane, colIndex, rowIndex);
        }
    }

    /**
     * Returns the color for a seat type.
     * @param typeName Seat type name string
     * @return Corresponding Color object
     */
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

    /**
     * Updates the room's total capacity in the database based on active seats.
     */
    private void updateRoomCapacityInDatabase() {
        int activeSeats = 0;
        for (Seat seat : seatsInRoom) {
            if (seat.isActive()) {
                activeSeats++;
            }
        }

        currentRoom.setTotalCapacity(activeSeats);
        ScreeningRoomDAO.updateRoomCapacity(currentRoom.getRoomId(), activeSeats);
    }

    /**
     * Handles the update button action.
     * Saves changes to seats and shows confirmation or error alerts.
     * @param actionEvent The action event triggered by the update button
     */
    public void handleUpdate(ActionEvent actionEvent) {
        // Validate that room and seats are loaded
        if (currentRoom == null || seatsInRoom == null) {
            System.err.println("Room or seat data is missing");
            return;
        }

        // Call DAO to update seats in the room
        boolean success = SeatDAO.updateSeatsInRoom(currentRoom.getRoomId(), seatsInRoom);

        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Update Successful");
            alert.setHeaderText(null);
            alert.setContentText("Seating layout updated successfully!");
            alert.showAndWait();

            // Close the form after successful update
            handleCancel(actionEvent);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Update Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to update seating layout.");
            alert.showAndWait();
        }
    }

    /**
     * Handles the cancel button action.
     * Closes the current window.
     * @param actionEvent The action event triggered by the cancel button
     */
    public void handleCancel(ActionEvent actionEvent) {
        // Get the current stage from any node and close it
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
