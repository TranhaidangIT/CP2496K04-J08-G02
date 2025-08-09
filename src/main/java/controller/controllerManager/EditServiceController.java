package controller.controllerManager;

import dao.ServiceDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Service;
import models.ServiceCategory;

import java.math.BigDecimal;
import java.util.List;

public class EditServiceController {

    @FXML private TextField txtServiceName;
    @FXML private TextField txtPrice;
    @FXML private ComboBox<ServiceCategory> cbCategory;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnCancel;

    private Service service;
    private ServiceListController serviceListController;

    @FXML
    public void initialize() {
        // Initialize categories
        loadServiceCategories();
    }

    private void loadServiceCategories() {
        List<ServiceCategory> categories = ServiceDAO.getAllServiceCategories();
        cbCategory.getItems().clear();
        cbCategory.getItems().addAll(categories);
    }

    public void setServiceData(Service service) {
        this.service = service;

        txtServiceName.setText(service.getServiceName());
        txtPrice.setText(service.getPrice().toString());

        // Set selected category
        for (ServiceCategory category : cbCategory.getItems()) {
            if (category.getCategoryId() == service.getCategoryId()) {
                cbCategory.setValue(category);
                break;
            }
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        String serviceName = txtServiceName.getText().trim();
        String priceStr = txtPrice.getText().trim();
        ServiceCategory selectedCategory = cbCategory.getValue();

        // Validation
        if (serviceName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Field", "Please enter service name.");
            return;
        }

        if (priceStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Field", "Please enter price.");
            return;
        }

        if (selectedCategory == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Field", "Please select a category.");
            return;
        }

        try {
            BigDecimal price = new BigDecimal(priceStr);

            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(Alert.AlertType.ERROR, "Invalid Price", "Price must be greater than 0.");
                return;
            }

            // Check if service ID exists
            if (!ServiceDAO.isServiceIdExists(service.getServiceId())) {
                showAlert(Alert.AlertType.ERROR, "Error", "Service ID does not exist in database.");
                return;
            }

            // Update service object
            service.setServiceName(serviceName);
            service.setPrice(price);
            service.setCategoryId(selectedCategory.getCategoryId());

            boolean updated = ServiceDAO.updateService(service);
            if (updated) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Service updated successfully.");

                if (serviceListController != null) {
                    serviceListController.loadServiceList();
                }

                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update service.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid price (numbers only).");
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Are you sure you want to delete this service?");
        confirmAlert.setContentText("This action cannot be undone.");

        ButtonType btnYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType btnNo = new ButtonType("No", ButtonBar.ButtonData.NO);

        confirmAlert.getButtonTypes().setAll(btnYes, btnNo);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == btnYes) {
                boolean success = ServiceDAO.deleteServiceById(service.getServiceId());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Deleted", "Service deleted successfully.");

                    if (serviceListController != null) {
                        serviceListController.loadServiceList();
                    }

                    closeWindow();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Failed", "Failed to delete service.");
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

    public void setServiceListController(ServiceListController serviceListController) {
        this.serviceListController = serviceListController;
    }
}