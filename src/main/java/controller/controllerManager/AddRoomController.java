package controller.controllerManager;

import dao.ScreeningRoomDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.ScreeningRoom;

import java.time.LocalDateTime;

public class AddRoomController {

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnInsert;

    @FXML
    private ComboBox<String> cbRoomStatus;

    @FXML
    private ComboBox<String> cbRoomType;

    @FXML
    private ComboBox<String> cbSeatLayout;

    @FXML
    private TextField tfEquipment;

    @FXML
    private TextField tfRoomNumber;

    @FXML
    private TextField tfTotalCap;

    @FXML
    public void initialize() {
        cbRoomStatus.getItems().addAll("Available", "Under Maintenance", "Closed");
        cbRoomType.getItems().addAll("Standard", "VIP", "IMAX");
        cbSeatLayout.getItems().addAll("Regular", "Premium", "Mixed");
    }

    @FXML
    void handleInsert(ActionEvent event) {
        String roomNumber = tfRoomNumber.getText().trim();
        String roomType = cbRoomType.getValue();
        String roomStatus = cbRoomStatus.getValue();
        String seatLayout = cbSeatLayout.getValue();
        String equipment = tfEquipment.getText().trim();
        int totalCapacity;

        try {
            totalCapacity = Integer.parseInt(tfTotalCap.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Total capacity must be a number.", Alert.AlertType.WARNING);
            return;
        }

        if (roomNumber.isEmpty() || roomType == null || roomStatus == null || seatLayout == null) {
            showAlert("Please fill all required fields.", Alert.AlertType.WARNING);
            return;
        }

        System.out.println("Checking duplicate for roomNumber: " + roomNumber);
        boolean isExists = ScreeningRoomDAO.isRoomNumberExists(roomNumber);
        System.out.println("isRoomNumberExists: " + isExists);

        if (isExists) {
            showAlert("Room number already exists. Please enter a different number.", Alert.AlertType.WARNING);
            return;
        }

        ScreeningRoom newRoom = new ScreeningRoom(
                0, roomNumber, roomType, roomStatus, seatLayout, totalCapacity, equipment, LocalDateTime.now()
        );

        boolean success = ScreeningRoomDAO.insertRoom(newRoom);
        if (success) {
            showAlert("Room added successfully!", Alert.AlertType.INFORMATION);
            Stage stage = (Stage) btnInsert.getScene().getWindow();
            stage.close();
        } else {
            showAlert("Failed to add room.", Alert.AlertType.ERROR);
        }
    }


    @FXML
    void handleCancel(ActionEvent event) {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(msg);
        alert.show();
    }

    public void setRoomListController(RoomListController roomListController) {
        // Optional: use this to refresh room list after adding
    }
}
