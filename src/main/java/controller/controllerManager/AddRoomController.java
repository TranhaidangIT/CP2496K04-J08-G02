package controller.controllerManager;

import dao.RoomTypeDAO;
import dao.ScreeningRoomDAO;
import dao.SeatDAO;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.RoomType;
import models.ScreeningRoom;

import java.time.LocalDateTime;
import java.util.List;

public class AddRoomController {

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnInsert;

    @FXML
    private ComboBox<String> cbRoomStatus;

    @FXML
    private ComboBox<RoomType> cbRoomType;

    @FXML
    private Label lblLayoutWarning;

    @FXML
    private Spinner<Integer> spinnerRows;

    @FXML
    private Spinner<Integer> spinnerColumns;

    @FXML
    private TextField tfRoomNumber;

    @FXML
    private TextField tfTotalCap;

    @FXML
    private TextArea tfEquipment;

    @FXML
    private Label txtRoomStatus;

    /**
     * Initialize method called automatically after FXML loads.
     * Sets up combo boxes, spinners, and listeners for UI components.
     */
    @FXML
    public void initialize() {
        setRoomStatusCombo();  // Populate room status options

        // Load all room types from database and set to ComboBox
        List<RoomType> roomTypes = RoomTypeDAO.getAllRoomTypes();
        cbRoomType.setItems(FXCollections.observableArrayList(roomTypes));

        if (!roomTypes.isEmpty()) {
            cbRoomType.getSelectionModel().selectFirst();  
        }

        initializeSpinners();

        // Add listeners to update total capacity when rows or columns change
        spinnerRows.valueProperty().addListener((obs, oldVal, newVal) -> updateTotalCapacityAndValidate());
        spinnerColumns.valueProperty().addListener((obs, oldVal, newVal) -> updateTotalCapacityAndValidate());

        // Adjust spinner limits when room type changes (based on max allowed rows/columns)
        cbRoomType.valueProperty().addListener((obs, oldVal, newVal) -> updateSpinnerLimits(newVal));

        // Initialize spinner limits for initially selected room type
        if (cbRoomType.getValue() != null) {
            updateSpinnerLimits(cbRoomType.getValue());
        }
    }

    /**
     * Updates the min/max values of rows and columns spinners according to
     * the selected room type's max row/column limits (capped at 15).
     * Also adjusts current spinner values if they exceed new limits.
     */
    public void updateSpinnerLimits(RoomType roomType) {
        if (roomType == null) return;

        int maxRow = Math.min(roomType.getMaxRows(), 15);
        int maxCol = Math.min(roomType.getMaxColumns(), 15);

        int currentRow = spinnerRows.getValue() != null ? spinnerRows.getValue() : 1;
        int currentCol = spinnerColumns.getValue() != null ? spinnerColumns.getValue() : 1;

        // Cap current values to new max limits if necessary
        currentRow = Math.min(currentRow, maxRow);
        currentCol = Math.min(currentCol, maxCol);

        // Reset spinners with new limits and adjusted current values
        spinnerRows.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxRow, currentRow));
        spinnerColumns.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxCol, currentCol));

        updateTotalCapacityAndValidate();
    }

    /**
     * Calculates total seat capacity (rows * columns) and updates
     * the capacity text field. Also changes text color based on validity.
     */
    public void updateTotalCapacityAndValidate() {
        int rows = spinnerRows.getValue() != null ? spinnerRows.getValue() : 1;
        int cols = spinnerColumns.getValue() != null ? spinnerColumns.getValue() : 1;

        int totalCapacity = rows * cols;
        tfTotalCap.setText(String.valueOf(totalCapacity));

        // Highlight total capacity in red if invalid (<= 0)
        if (totalCapacity <= 0) {
            tfTotalCap.setStyle("-fx-text-fill: red;");
        } else {
            tfTotalCap.setStyle("-fx-text-fill: black;");
        }
    }

    /**
     * Handler for Cancel button.
     * Closes the current window without saving.
     */
    @FXML
    void handleCancel(ActionEvent event) {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    /**
     * Handler for Insert button.
     * Validates inputs, creates ScreeningRoom and Seat records in DB.
     * Shows alerts on success or failure.
     */
    @FXML
    void handleInsert(ActionEvent event) {
        String roomNumber = tfRoomNumber.getText().trim();
        RoomType selectedRoomType = cbRoomType.getValue();
        String roomStatus = cbRoomStatus.getValue();
        int rows = spinnerRows.getValue() != null ? spinnerRows.getValue() : 1;
        int cols = spinnerColumns.getValue() != null ? spinnerColumns.getValue() : 1;

        int capacity = rows * cols;

        // Validate required fields
        if (roomNumber.isEmpty() || selectedRoomType == null) {
            showAlert("Validation Error", "Room number and room type are required.");
            return;
        }

        // Check for duplicate room number
        if (ScreeningRoomDAO.isRoomNumberExists(roomNumber)) {
            showAlert("Duplicate Room", "Room number already exists.");
            return;
        }

        String equipmentText = tfEquipment.getText().trim();

        // Create new ScreeningRoom object with form data
        ScreeningRoom newRoom = new ScreeningRoom();
        newRoom.setRoomNumber(roomNumber);
        newRoom.setRoomTypeId(selectedRoomType.getRoomTypeId());

        String seatingLayout = rows + "x" + cols;
        newRoom.setSeatingLayout(seatingLayout);

        newRoom.setTotalCapacity(capacity);
        newRoom.setEquipment(equipmentText);
        newRoom.setRoomStatus(roomStatus);
        newRoom.setCreatedAt(LocalDateTime.now());

        // Insert room to database
        boolean inserted = ScreeningRoomDAO.insertRoom(newRoom);

        if (inserted) {
            // Retrieve newly created room ID by room number
            int roomId = ScreeningRoomDAO.getRoomIdByRoomNumber(roomNumber); // Make sure this method exists

            // Insert seat records for the room
            boolean seatsInserted = SeatDAO.insertSeatsForRoom(roomId, rows, cols, selectedRoomType.getRoomTypeId());

            if (seatsInserted) {
                showAlert("Success", "Room and seats added successfully.");
                handleCancel(event);  // Close form on success
            } else {
                showAlert("Error", "Room added, but failed to create seats.");
            }
        } else {
            showAlert("Error", "Failed to insert room.");
        }
    }

    /**
     * Shows an information alert dialog with given title and message.
     * @param title Dialog title
     * @param message Dialog message
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Initializes row and column spinners with default values and limits
     * based on the selected room type or default max values.
     */
    public void initializeSpinners() {
        RoomType defaultRoomType = cbRoomType.getValue();
        if (defaultRoomType != null) {
            int maxRow = Math.min(defaultRoomType.getMaxRows(), 15);
            int maxCol = Math.min(defaultRoomType.getMaxColumns(), 15);
            spinnerRows.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxRow, 5));
            spinnerColumns.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxCol, 5));
        } else {
            spinnerRows.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 15, 5));
            spinnerColumns.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 15, 5));
        }
    }

    /**
     * Populates the room status combo box with fixed options.
     */
    public void setRoomStatusCombo() {
        cbRoomStatus.setItems(FXCollections.observableArrayList("Available", "Unavailable", "Maintenance"));
        cbRoomStatus.getSelectionModel().selectFirst();  // Default to first option
    }
}
