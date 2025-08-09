package controller.controllerManager;

import dao.ServiceDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Service;
import models.ServiceCategory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class ServiceListController {

    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnFind;

    @FXML private ComboBox<ServiceCategory> cbCategoryFilter;
    @FXML private TextField txtSearch;

    @FXML private TableView<Service> tblServices;
    @FXML private TableColumn<Service, Integer> colId;
    @FXML private TableColumn<Service, String> colName;
    @FXML private TableColumn<Service, String> colCategory;
    @FXML private TableColumn<Service, BigDecimal> colPrice;

    @FXML private TextField txtServiceName;
    @FXML private TextField txtPrice;
    @FXML private ComboBox<ServiceCategory> cbCategory;

    private ObservableList<Service> serviceList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize default categories
        ServiceDAO.initializeDefaultCategories();

        // Initialize ComboBoxes
        loadServiceCategories();

        // Set up table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("serviceId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Format price column to show currency
        colPrice.setCellFactory(column -> new TableCell<Service, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", price));
                }
            }
        });

        // Load service data
        loadServiceList();
    }

    private void loadServiceCategories() {
        List<ServiceCategory> categories = ServiceDAO.getAllServiceCategories();

        // Add "All Categories" option for filter
        ServiceCategory allCategories = new ServiceCategory(0, "All Categories");

        cbCategoryFilter.getItems().clear();
        cbCategoryFilter.getItems().add(allCategories);
        cbCategoryFilter.getItems().addAll(categories);
        cbCategoryFilter.setValue(allCategories);

        // Load categories for detail form
        cbCategory.getItems().clear();
        cbCategory.getItems().addAll(categories);
    }

    public void loadServiceList() {
        List<Service> services = ServiceDAO.getAllServices();
        System.out.println("DEBUG: Loaded services count = " + (services != null ? services.size() : "null"));
        serviceList.setAll(services);
        tblServices.setItems(serviceList);
    }

    @FXML
    void handleAddNewService(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/fxml_Manager/Service/AddService.fxml")
            );
            Parent root = loader.load();

            AddServiceController addCtrl = loader.getController();
            addCtrl.setServiceListController(this);

            Stage stage = new Stage();
            stage.setTitle("Add New Service");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            System.out.println(e);
            showErrorAlert("Error", "Failed to load the Add Service form.");
        }
    }

    @FXML
    void openEditServiceForm(ActionEvent event) {
        Service selected = tblServices.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("No Service Selected", "Please select a service to edit.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/fxml_Manager/Service/EditService.fxml")
            );
            Parent root = loader.load();

            EditServiceController editCtrl = loader.getController();
            editCtrl.setServiceData(selected);
            editCtrl.setServiceListController(this);

            Stage stage = new Stage();
            stage.setTitle("Edit Service");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to open Edit Service form.");
        }
    }

    @FXML
    void handleTableClick(MouseEvent event) {
        Service selected = tblServices.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showServiceDetails(selected);
        }
    }

    private void showServiceDetails(Service service) {
        txtServiceName.setText(service.getServiceName());
        txtPrice.setText(String.format("$%.2f", service.getPrice()));

        // Set category
        for (ServiceCategory category : cbCategory.getItems()) {
            if (category.getCategoryId() == service.getCategoryId()) {
                cbCategory.setValue(category);
                break;
            }
        }
    }

    @FXML
    void handleFind(ActionEvent event) {
        ServiceCategory selectedCategory = cbCategoryFilter.getValue();
        String searchText = txtSearch.getText().trim().toLowerCase();

        if (selectedCategory != null && selectedCategory.getCategoryId() == 0 && searchText.isEmpty()) {
            // Show all services
            loadServiceList();
            return;
        }

        List<Service> filteredServices;

        if (selectedCategory != null && selectedCategory.getCategoryId() > 0) {
            // Filter by category
            filteredServices = ServiceDAO.getServicesByCategory(selectedCategory.getCategoryId());
        } else {
            // Get all services for text search
            filteredServices = ServiceDAO.getAllServices();
        }

        // Apply text search filter
        if (!searchText.isEmpty()) {
            filteredServices = filteredServices.stream()
                    .filter(service -> service.getServiceName().toLowerCase().contains(searchText))
                    .toList();
        }

        serviceList.setAll(filteredServices);
        tblServices.setItems(serviceList);
    }

    @FXML
    void handleClearFilter(ActionEvent event) {
        cbCategoryFilter.setValue(cbCategoryFilter.getItems().get(0)); // "All Categories"
        txtSearch.clear();
        loadServiceList();
    }

    // ALERT HELPERS
    private void showWarningAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }
}