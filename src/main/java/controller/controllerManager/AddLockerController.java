package controller.controllerManager;

import dao.LockerDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Locker;

public class AddLockerController {

    @FXML private TextField txtLockerNumber;
    @FXML private TextField txtLocationInfo;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Button btnInsert;
    @FXML private Button btnCancel;

    private LockerListController lockerListController;

    @FXML
    public void initialize() {
        // Initialize status options
        cbStatus.getItems().addAll("Available", "Occupied", "Maintenance");
        cbStatus.setValue("Available"); // Default status
    }

    @FXML
    private void handleInsert(ActionEvent event) {
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

        // Check if locker number already exists
        if (LockerDAO.isLockerNumberExists(lockerNumber)) {
            showAlert(Alert.AlertType.ERROR, "Duplicate Locker", "Locker number already exists. Please choose a different number.");
            return;
        }

        try {
            // Create new locker
            Locker locker = new Locker(0, lockerNumber, locationInfo, status);

            boolean inserted = LockerDAO.insertLocker(locker);
            if (inserted) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Locker added successfully.");

                if (lockerListController != null) {
                    lockerListController.loadLockerList();
                }

                clearForm();
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add locker to the database.");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void clearForm() {
        txtLockerNumber.clear();
        txtLocationInfo.clear();
        cbStatus.setValue("Available");
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