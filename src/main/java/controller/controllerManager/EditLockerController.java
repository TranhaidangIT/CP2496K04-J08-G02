package controller.controllerManager;

import dao.LockerDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Locker;

public class EditLockerController {

    @FXML private TextField txtLockerNumber;
    @FXML private TextField txtLocationInfo;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnCancel;

    private Locker locker;
    private LockerListController lockerListController;

    @FXML
    public void initialize() {
        // Initialize status options
        cbStatus.getItems().addAll("Available", "Occupied", "Maintenance");
    }

    public void setLockerData(Locker locker) {
        this.locker = locker;

        txtLockerNumber.setText(locker.getLockerNumber());
        txtLocationInfo.setText(locker.getLocationInfo());
        cbStatus.setValue(locker.getStatus());
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        String lockerNumber = txtLockerNumber.getText().trim();
        String locationInfo = txtLocationInfo.getText().trim();
        String status = cbStatus.getValue();

        // Validation
        if (lockerNumber.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Field", "Please enter locker number.");
            return;
        }

        if (locationInfo.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Field", "Please enter location information.");
            return;
        }

        if (status == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Field", "Please select a status.");
            return;
        }

        // Check if locker number already exists (but not for current locker)
        if (!lockerNumber.equals(locker.getLockerNumber()) && LockerDAO.isLockerNumberExists(lockerNumber)) {
            showAlert(Alert.AlertType.ERROR, "Duplicate Locker", "Locker number already exists. Please choose a different number.");
            return;
        }

        // Check if trying to change status from Occupied to Available
        if ("Occupied".equals(locker.getStatus()) && "Available".equals(status)) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Status Change Warning");
            confirmAlert.setHeaderText("Change Occupied Locker to Available?");
            confirmAlert.setContentText("This locker is currently occupied. Are you sure you want to mark it as available?\nThis might affect stored items.");

            ButtonType btnYes = new ButtonType("Yes, Change", ButtonBar.ButtonData.YES);
            ButtonType btnNo = new ButtonType("No, Cancel", ButtonBar.ButtonData.NO);
            confirmAlert.getButtonTypes().setAll(btnYes, btnNo);

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == btnNo) {
                    return;
                }
            });
        }

        try {
            // Update locker object
            locker.setLockerNumber(lockerNumber);
            locker.setLocationInfo(locationInfo);
            locker.setStatus(status);

            boolean updated = LockerDAO.updateLocker(locker);
            if (updated) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Locker updated successfully.");

                if (lockerListController != null) {
                    lockerListController.loadLockerList();
                }

                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update locker.");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        // Check if locker is currently occupied
        if ("Occupied".equals(locker.getStatus())) {
            showAlert(Alert.AlertType.ERROR, "Cannot Delete", "Cannot delete an occupied locker. Please retrieve stored items first.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Are you sure you want to delete this locker?");
        confirmAlert.setContentText("Locker: " + locker.getLockerNumber() + "\nLocation: " + locker.getLocationInfo() + "\n\nThis action cannot be undone.");

        ButtonType btnYes = new ButtonType("Yes, Delete", ButtonBar.ButtonData.YES);
        ButtonType btnNo = new ButtonType("No, Cancel", ButtonBar.ButtonData.NO);
        confirmAlert.getButtonTypes().setAll(btnYes, btnNo);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == btnYes) {
                boolean success = LockerDAO.deleteLockerById(locker.getLockerId());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Deleted", "Locker deleted successfully.");

                    if (lockerListController != null) {
                        lockerListController.loadLockerList();
                    }

                    closeWindow();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Failed", "Failed to delete locker. It might be referenced by other records.");
                }
            }
        });
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setLockerListController(LockerListController lockerListController) {
        this.lockerListController = lockerListController;
    }
}