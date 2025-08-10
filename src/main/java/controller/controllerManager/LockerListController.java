package controller.controllerManager;

import dao.LockerDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Locker;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class LockerListController {

    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnFind;

    @FXML private ComboBox<String> cbStatusFilter;
    @FXML private TextField txtSearch;

    @FXML private TableView<Locker> tblLockers;
    @FXML private TableColumn<Locker, Integer> colId;
    @FXML private TableColumn<Locker, String> colNumber;
    @FXML private TableColumn<Locker, String> colLocation;
    @FXML private TableColumn<Locker, String> colStatus;
    @FXML private TableColumn<Locker, String> colPinCode; // NEW: Column for PIN code

    @FXML private TextField txtLockerNumber;
    @FXML private TextField txtLocationInfo;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Label lblStatusInfo;

    @FXML private GridPane lockerGrid;
    @FXML private Label selectedLockerLabel;

    private ObservableList<Locker> lockerList = FXCollections.observableArrayList();
    private List<Map<String, Object>> allLockersWithAssignments; // Updated to handle assignment info
    private Locker selectedLocker = null;

    @FXML
    public void initialize() {
        // Initialize status filter
        loadStatusOptions();

        // Set up table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("lockerId"));
        colNumber.setCellValueFactory(new PropertyValueFactory<>("lockerNumber"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("locationInfo"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPinCode.setCellValueFactory(cellData -> {
            Locker locker = cellData.getValue();
            Map<String, String> assignmentInfo = LockerDAO.getLockerAssignmentInfo(locker.getLockerId());
            String pinCode = assignmentInfo.get("pinCode");
            return new javafx.beans.property.SimpleStringProperty(pinCode != null ? pinCode : "");
        });

        // Format status column with colors
        colStatus.setCellFactory(column -> new TableCell<Locker, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status.toLowerCase()) {
                        case "available":
                            setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                            break;
                        case "occupied":
                            setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                            break;
                        case "maintenance":
                            setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #9E9E9E;");
                            break;
                    }
                }
            }
        });

        // Format PIN code column
        colPinCode.setCellFactory(column -> new TableCell<Locker, String>() {
            @Override
            protected void updateItem(String pinCode, boolean empty) {
                super.updateItem(pinCode, empty);
                if (empty || pinCode == null || pinCode.isEmpty()) {
                    setText("N/A");
                    setStyle("-fx-text-fill: #9E9E9E;");
                } else {
                    setText(pinCode);
                    setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
                }
            }
        });

        // Load locker data
        loadLockerList();

        // Display locker grid
        displayLockersGrid();
    }

    private void loadStatusOptions() {
        cbStatusFilter.getItems().clear();
        cbStatusFilter.getItems().addAll("All Status", "Available", "Occupied", "Maintenance");
        cbStatusFilter.setValue("All Status");

        cbStatus.getItems().clear();
        cbStatus.getItems().addAll("Available", "Occupied", "Maintenance");
    }

    public void loadLockerList() {
        allLockersWithAssignments = LockerDAO.getAllLockersWithAssignments();
        System.out.println("DEBUG: Loaded lockers count = " + (allLockersWithAssignments != null ? allLockersWithAssignments.size() : "null"));

        lockerList.clear();
        for (Map<String, Object> lockerInfo : allLockersWithAssignments) {
            Locker locker = new Locker(
                    (Integer) lockerInfo.get("lockerId"),
                    (String) lockerInfo.get("lockerNumber"),
                    (String) lockerInfo.get("locationInfo"),
                    (String) lockerInfo.get("status")
            );
            lockerList.add(locker);
        }
        tblLockers.setItems(lockerList);

        // Refresh grid display
        displayLockersGrid();
    }

    private void displayLockersGrid() {
        if (lockerGrid == null || allLockersWithAssignments == null) return;

        lockerGrid.getChildren().clear();
        int cols = 8;
        int row = 0, col = 0;

        for (Map<String, Object> lockerInfo : allLockersWithAssignments) {
            Locker locker = new Locker(
                    (Integer) lockerInfo.get("lockerId"),
                    (String) lockerInfo.get("lockerNumber"),
                    (String) lockerInfo.get("locationInfo"),
                    (String) lockerInfo.get("status")
            );
            Button lockerButton = createLockerButton(locker);
            lockerGrid.add(lockerButton, col, row);
            GridPane.setMargin(lockerButton, new Insets(3));

            col++;
            if (col >= cols) {
                col = 0;
                row++;
            }
        }
    }

    private Button createLockerButton(Locker locker) {
        Button button = new Button(locker.getLockerNumber());
        button.setPrefSize(65, 50);

        String style = getLockerButtonStyle(locker.getStatus());
        button.setStyle(style);

        button.setOnAction(e -> handleLockerGridClick(locker));

        return button;
    }

    private void handleLockerGridClick(Locker locker) {
        selectLockerInGrid(locker);
        selectLockerInTable(locker);
        showLockerDetails(locker);
    }

    private void selectLockerInGrid(Locker locker) {
        this.selectedLocker = locker;

        selectedLockerLabel.setText("Selected: Locker " + locker.getLockerNumber() +
                " - " + locker.getLocationInfo() + " (" + locker.getStatus() + ")");

        refreshLockerGridDisplay();
    }

    private void selectLockerInTable(Locker locker) {
        for (int i = 0; i < tblLockers.getItems().size(); i++) {
            Locker item = tblLockers.getItems().get(i);
            if (item.getLockerId() == locker.getLockerId()) {
                tblLockers.getSelectionModel().select(i);
                tblLockers.scrollTo(i);
                break;
            }
        }
    }

    private void refreshLockerGridDisplay() {
        lockerGrid.getChildren().forEach(node -> {
            if (node instanceof Button) {
                Button btn = (Button) node;
                String lockerNum = btn.getText();

                Locker locker = lockerList.stream()
                        .filter(l -> l.getLockerNumber().equals(lockerNum))
                        .findFirst().orElse(null);

                if (locker != null) {
                    if (selectedLocker != null && locker.getLockerId() == selectedLocker.getLockerId()) {
                        btn.setStyle(getSelectedLockerButtonStyle());
                    } else {
                        btn.setStyle(getLockerButtonStyle(locker.getStatus()));
                    }
                }
            }
        });
    }

    private String getLockerButtonStyle(String status) {
        switch (status.toLowerCase()) {
            case "available":
                return "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-background-radius: 5;";
            case "occupied":
                return "-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-background-radius: 5;";
            case "maintenance":
                return "-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-opacity: 0.8; -fx-background-radius: 5;";
            default:
                return "-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-opacity: 0.7; -fx-background-radius: 5;";
        }
    }

    private String getSelectedLockerButtonStyle() {
        return "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-background-radius: 5; -fx-border-color: #0056b3; -fx-border-width: 2;";
    }

    @FXML
    void handleAddNewLocker(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/fxml_Manager/Locker/AddLocker.fxml")
            );
            Parent root = loader.load();

            AddLockerController addCtrl = loader.getController();
            addCtrl.setLockerListController(this);

            Stage stage = new Stage();
            stage.setTitle("Add New Locker");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            System.out.println(e);
            showErrorAlert("Error", "Failed to load the Add Locker form.");
        }
    }

    @FXML
    void openEditLockerForm(ActionEvent event) {
        Locker selected = tblLockers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("No Locker Selected", "Please select a locker to edit.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/fxml_Manager/Locker/EditLocker.fxml")
            );
            Parent root = loader.load();

            EditLockerController editCtrl = loader.getController();
            editCtrl.setLockerData(selected);
            editCtrl.setLockerListController(this);

            Stage stage = new Stage();
            stage.setTitle("Edit Locker");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to open Edit Locker form.");
        }
    }

    @FXML
    void handleTableClick(MouseEvent event) {
        Locker selected = tblLockers.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectLockerInGrid(selected);
            showLockerDetails(selected);
        }
    }

    private void showLockerDetails(Locker locker) {
        txtLockerNumber.setText(locker.getLockerNumber());
        txtLocationInfo.setText(locker.getLocationInfo());
        cbStatus.setValue(locker.getStatus());

        updateStatusInfo(locker.getStatus());
    }

    private void updateStatusInfo(String status) {
        String info;
        switch (status.toLowerCase()) {
            case "available":
                info = "✓ Ready for use";
                lblStatusInfo.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                break;
            case "occupied":
                info = "⚠ Currently in use";
                lblStatusInfo.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                break;
            case "maintenance":
                info = "🔧 Under maintenance";
                lblStatusInfo.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                break;
            default:
                info = "Status unknown";
                lblStatusInfo.setStyle("-fx-text-fill: #9E9E9E;");
                break;
        }
        lblStatusInfo.setText(info);
    }

    @FXML
    void handleFind(ActionEvent event) {
        String selectedStatus = cbStatusFilter.getValue();
        String searchText = txtSearch.getText().trim().toLowerCase();

        if ("All Status".equals(selectedStatus) && searchText.isEmpty()) {
            loadLockerList();
            return;
        }

        List<Map<String, Object>> filteredLockers = allLockersWithAssignments;

        if (!"All Status".equals(selectedStatus)) {
            filteredLockers = LockerDAO.getAllLockersWithAssignments().stream()
                    .filter(locker -> selectedStatus.equals(locker.get("status")))
                    .toList();
        }

        if (!searchText.isEmpty()) {
            filteredLockers = filteredLockers.stream()
                    .filter(locker ->
                            locker.get("lockerNumber").toString().toLowerCase().contains(searchText) ||
                                    locker.get("locationInfo").toString().toLowerCase().contains(searchText))
                    .toList();
        }

        lockerList.clear();
        for (Map<String, Object> lockerInfo : filteredLockers) {
            Locker locker = new Locker(
                    (Integer) lockerInfo.get("lockerId"),
                    (String) lockerInfo.get("lockerNumber"),
                    (String) lockerInfo.get("locationInfo"),
                    (String) lockerInfo.get("status")
            );
            lockerList.add(locker);
        }
        tblLockers.setItems(lockerList);

        allLockersWithAssignments = filteredLockers;
        displayLockersGrid();
    }

    @FXML
    void handleClearFilter(ActionEvent event) {
        cbStatusFilter.setValue("All Status");
        txtSearch.clear();
        loadLockerList();
    }

    public void refreshData() {
        loadLockerList();

        selectedLocker = null;
        selectedLockerLabel.setText("Click on a locker to view details");

        txtLockerNumber.clear();
        txtLocationInfo.clear();
        cbStatus.setValue(null);
        lblStatusInfo.setText("Select a locker to view details");
        lblStatusInfo.setStyle("");

        tblLockers.getSelectionModel().clearSelection();
    }

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