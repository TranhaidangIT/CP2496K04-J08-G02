package controller.controllerManager;

import dao.ScreeningRoomDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.ScreeningRoom;

import java.io.IOException;
import java.util.Optional;

public class EditRoomController {

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnEditSLayout;

    @FXML
    private Button btnUpdate;

    @FXML
    private ComboBox<String> cbRoomStatus;

    @FXML
    private ComboBox<String> cbRoomType;

    @FXML
    private TextField tfEquipment;

    @FXML
    private TextField tfRoomNumber;

    @FXML
    private TextField tfTotalCap;

    private ScreeningRoom selectedRoom;
    private final ScreeningRoomDAO roomDAO = new ScreeningRoomDAO();
    private RoomListController roomListController;

    public void setRoom(ScreeningRoom room) {
        this.selectedRoom = room;

        tfRoomNumber.setText(room.getRoomNumber());
        cbRoomType.setValue(room.getRoomType());
        cbRoomStatus.setValue(room.getRoomStatus());
        tfEquipment.setText(room.getEquipment());
        tfTotalCap.setText(String.valueOf(room.getTotalCapacity()));
    }

    @FXML
    public void initialize() {
        cbRoomType.getItems().addAll("2D", "3D", "IMAX", "4DX");
        cbRoomStatus.getItems().addAll("Available", "Maintenance", "Closed");
    }

    @FXML
    void handleCancel(ActionEvent event) {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }

    @FXML
    void handleDelete(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Confirmation");
        confirm.setHeaderText("Delete Room " + selectedRoom.getRoomNumber());
        confirm.setContentText("Are you sure you want to delete this room?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = roomDAO.deleteRoom(selectedRoom.getRoomId());
            if (deleted) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Room deleted successfully!");
                ((Stage) btnDelete.getScene().getWindow()).close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete the room.");
            }
        }
    }

    @FXML
    void handleOpenEditStLayout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Manager/ScreeningRoom/EditSeatingLayout.fxml"));
            Parent root = loader.load();

            EditSeatingLayoutController controller = loader.getController();
            controller.setRoomId(selectedRoom.getRoomId()); // Set roomId instead of roomNumber for better ID integrity

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Seating Layout - Room " + selectedRoom.getRoomNumber());
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open seat layout editor.");
        }
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        try {
            String roomNumber = tfRoomNumber.getText().trim();
            String roomType = cbRoomType.getValue();
            String roomStatus = cbRoomStatus.getValue();
            String equipment = tfEquipment.getText().trim();
            int totalCap = Integer.parseInt(tfTotalCap.getText().trim());

            if (roomNumber.isEmpty() || roomType == null || roomStatus == null || equipment.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill all required fields.");
                return;
            }

            ScreeningRoom updatedRoom = new ScreeningRoom(
                    selectedRoom.getRoomId(),
                    roomNumber,
                    roomType,
                    roomStatus,
                    selectedRoom.getSeatingLayout(),
                    totalCap,
                    equipment,
                    selectedRoom.getCreatedAt()
            );

            boolean updated = roomDAO.updateRoom(updatedRoom);

            if (updated) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Room updated successfully!");

                // ✅ Refresh the room list in RoomListController
                if (roomListController != null) {
                    roomListController.refreshRoomList();
                }

                ((Stage) btnUpdate.getScene().getWindow()).close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update the room.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Capacity must be a number.");
        }
    }


    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    

    public void setRoomListController(RoomListController controller) {
        this.roomListController = controller;
    }


    public void setRoomData(ScreeningRoom room) {
        this.selectedRoom = room;

        tfRoomNumber.setText(room.getRoomNumber());
        cbRoomType.setValue(room.getRoomType());
        cbRoomStatus.setValue(room.getRoomStatus());
        tfEquipment.setText(room.getEquipment());
        tfTotalCap.setText(String.valueOf(room.getTotalCapacity()));
    }




}
