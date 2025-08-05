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

    @FXML
    public void initialize() {
        setRoomStatusCombo();
        List<RoomType> roomTypes = RoomTypeDAO.getAllRoomTypes();
        cbRoomType.setItems(FXCollections.observableArrayList(roomTypes));
        if (!roomTypes.isEmpty()) {
            cbRoomType.getSelectionModel().selectFirst();
        }
        initializeSpinners();
        spinnerRows.valueProperty().addListener((obs, oldVal, newVal) -> updateTotalCapacityAndValidate());
        spinnerColumns.valueProperty().addListener((obs, oldVal, newVal) -> updateTotalCapacityAndValidate());
        cbRoomType.valueProperty().addListener((obs, oldVal, newVal) -> updateSpinnerLimits(newVal));
        if (cbRoomType.getValue() != null) {
            updateSpinnerLimits(cbRoomType.getValue());
        }
    }

    public void updateSpinnerLimits(RoomType roomType) {
        if (roomType == null) return;

        int maxRow = Math.min(roomType.getMaxRows(), 15);
        int maxCol = Math.min(roomType.getMaxColumns(), 15);

        int currentRow = spinnerRows.getValue() != null ? spinnerRows.getValue() : 1;
        int currentCol = spinnerColumns.getValue() != null ? spinnerColumns.getValue() : 1;

        // Giới hạn lại nếu đang vượt quá
        currentRow = Math.min(currentRow, maxRow);
        currentCol = Math.min(currentCol, maxCol);

        spinnerRows.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxRow, currentRow));
        spinnerColumns.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxCol, currentCol));

        updateTotalCapacityAndValidate();
    }


    public void updateTotalCapacityAndValidate() {
        int rows = spinnerRows.getValue() != null ? spinnerRows.getValue() : 1;
        int cols = spinnerColumns.getValue() != null ? spinnerColumns.getValue() : 1;

        int totalCapacity = rows * cols;
        tfTotalCap.setText(String.valueOf(totalCapacity));

        if (totalCapacity <= 0) {
            tfTotalCap.setStyle("-fx-text-fill: red;");
        } else {
            tfTotalCap.setStyle("-fx-text-fill: black;");
        }
    }


    @FXML
    void handleCancel(ActionEvent event) {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    @FXML
    void handleInsert(ActionEvent event) {
        String roomNumber = tfRoomNumber.getText().trim();
        RoomType selectedRoomType = cbRoomType.getValue();
        String roomStatus = cbRoomStatus.getValue();
        int rows = spinnerRows.getValue() != null ? spinnerRows.getValue() : 1;
        int cols = spinnerColumns.getValue() != null ? spinnerColumns.getValue() : 1;

        int capacity = rows * cols;

        if (roomNumber.isEmpty() || selectedRoomType == null) {
            showAlert("Validation Error", "Room number and room type are required.");
            return;
        }

        if (ScreeningRoomDAO.isRoomNumberExists(roomNumber)) {
            showAlert("Duplicate Room", "Room number already exists.");
            return;
        }

        String equipmentText = tfEquipment.getText().trim();

        ScreeningRoom newRoom = new ScreeningRoom();
        newRoom.setRoomNumber(roomNumber);
        newRoom.setRoomTypeId(selectedRoomType.getRoomTypeId());

        String seatingLayout = rows + "x" + cols;
        newRoom.setSeatingLayout(seatingLayout);

        newRoom.setTotalCapacity(capacity);
        newRoom.setEquipment(equipmentText);
        newRoom.setRoomStatus(roomStatus);
        newRoom.setCreatedAt(LocalDateTime.now());

        boolean inserted = ScreeningRoomDAO.insertRoom(newRoom);

        if (inserted) {
            int roomId = ScreeningRoomDAO.getRoomIdByRoomNumber(roomNumber); // bạn cần viết hàm này nếu chưa có

            boolean seatsInserted = SeatDAO.insertSeatsForRoom(roomId, rows, cols, selectedRoomType.getRoomTypeId());

            if (seatsInserted) {
                showAlert("Success", "Room and seats added successfully.");
                handleCancel(event);
            } else {
                showAlert("Error", "Room added, but failed to create seats.");
            }
        } else {
            showAlert("Error", "Failed to insert room.");
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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

    public void setRoomStatusCombo() {
        cbRoomStatus.setItems(FXCollections.observableArrayList("Available", "Unavailable", "Maintenance"));
        cbRoomStatus.getSelectionModel().selectFirst();
    }
}
