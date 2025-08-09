package controller.controllerManager;

import dao.ServiceDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Service;
import models.ServiceCategory;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public class AddServiceController {

    @FXML private TextField txtServiceName;
    @FXML private TextField txtPrice;
    @FXML private ComboBox<ServiceCategory> cbCategory;
    @FXML private TextField txtImagePath;
    @FXML private ImageView imgService;
    @FXML private Button btnChooseImage;
    @FXML private Button btnInsert;
    @FXML private Button btnCancel;

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

    @FXML
    private void handleChooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(btnChooseImage.getScene().getWindow());
        if (selectedFile != null) {
            txtImagePath.setText(selectedFile.getAbsolutePath());
            imgService.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    private void handleInsert(ActionEvent event) {
        String serviceName = txtServiceName.getText().trim();
        String priceStr = txtPrice.getText().trim();
        String imagePath = txtImagePath.getText().trim();
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

        if (imagePath.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Field", "Please select an image.");
            return;
        }

        try {
            BigDecimal price = new BigDecimal(priceStr);

            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(Alert.AlertType.ERROR, "Invalid Price", "Price must be greater than 0.");
                return;
            }

            // Create new service
            Service service = new Service(serviceName, price, selectedCategory.getCategoryId());
            service.setImg(imagePath);

            boolean inserted = ServiceDAO.insertService(service);
            if (inserted) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Service added successfully.");

                if (serviceListController != null) {
                    serviceListController.loadServiceList();
                }

                clearForm();
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add service to the database.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid price (numbers only).");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void clearForm() {
        txtServiceName.clear();
        txtPrice.clear();
        txtImagePath.clear();
        imgService.setImage(null);
        cbCategory.setValue(null);
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