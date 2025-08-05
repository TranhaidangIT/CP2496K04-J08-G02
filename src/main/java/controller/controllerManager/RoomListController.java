package controller.controllerManager;

import dao.ScreeningRoomDAO;
import dao.RoomTypeDAO;
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
import models.ScreeningRoom;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class RoomListController {

    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnViewSeatLayout;

    @FXML private TableColumn<ScreeningRoom, Integer> colRoomID;
    @FXML private TableColumn<ScreeningRoom, String> colRoomNumber;
    @FXML private TableColumn<ScreeningRoom, String> colRoomType;
    @FXML private TableColumn<ScreeningRoom, String> colRoomStatus;
    @FXML private TableColumn<ScreeningRoom, Integer> colRoomCap;
    @FXML private TableView<ScreeningRoom> tblRoomList;

    @FXML private TextField tfRoomNumb;
    @FXML private TextField tfSeatLayout;
    @FXML private Spinner<Integer> spRows;
    @FXML private Spinner<Integer> spCols;
    @FXML private TextField tfTotalCap;
    @FXML private TextField tfRoomType;
    @FXML private ComboBox<String> cbRoomStatus;
    @FXML private TextArea tfEquipment;
    @FXML private TextField tfCreatedAt;
    @FXML private TextField tfFind;

    private ObservableList<ScreeningRoom> roomList;

    @FXML
    public void initialize() {
        setupTable();
        setupSpinners();
        setupRoomStatusCombo();
        loadRoomData();
        tblRoomList.setOnMouseClicked(this::handleTableClick);
    }

    private void setupTable() {
        colRoomID.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        colRoomNumber.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colRoomType.setCellValueFactory(new PropertyValueFactory<>("typeName")); // Assuming typeName is fetched via RoomTypeDAO
        colRoomStatus.setCellValueFactory(new PropertyValueFactory<>("roomStatus"));
        colRoomCap.setCellValueFactory(new PropertyValueFactory<>("totalCapacity"));
    }

    private void setupSpinners() {
        spRows = new Spinner<>(1, 26, 5); // A-Z (1-26 rows)
        spCols = new Spinner<>(1, 10, 5); // 0-9 (1-10 columns)
        spRows.valueProperty().addListener((obs, oldVal, newVal) -> updateCapacity());
        spCols.valueProperty().addListener((obs, oldVal, newVal) -> updateCapacity());
        tfSeatLayout.setText(spRows.getValue() + "x" + spCols.getValue());
        spRows.valueProperty().addListener((obs, oldVal, newVal) -> tfSeatLayout.setText(newVal + "x" + spCols.getValue()));
        spCols.valueProperty().addListener((obs, oldVal, newVal) -> tfSeatLayout.setText(spRows.getValue() + "x" + newVal));
    }

    private void setupRoomStatusCombo() {
        cbRoomStatus = new ComboBox<>(FXCollections.observableArrayList("Available", "Unavailable", "Maintenance"));
        cbRoomStatus.setValue("Available");
    }

    private void updateCapacity() {
        int rows = spRows.getValue();
        int cols = spCols.getValue();
        int capacity = rows * cols;
        tfTotalCap.setText(String.valueOf(capacity));
    }

    private void loadRoomData() {
        List<ScreeningRoom> rooms = ScreeningRoomDAO.getAllRooms();
        roomList = FXCollections.observableArrayList(rooms);
        tblRoomList.setItems(roomList);
    }

    private void showRoomDetails(ScreeningRoom room) {
        if (room != null) {
            tfRoomNumb.setText(room.getRoomNumber());

            String roomTypeName = RoomTypeDAO.getRoomTypeNameById(room.getRoomTypeId());
            tfRoomType.setText(roomTypeName);

            cbRoomStatus.setValue(room.getRoomStatus());
            tfCreatedAt.setText(room.getCreatedAt() != null ? room.getCreatedAt().toString() : LocalDateTime.now().toString());

            String[] layout = room.getSeatingLayout().split("x");
            spRows.getValueFactory().setValue(Integer.parseInt(layout[0]));
            spCols.getValueFactory().setValue(Integer.parseInt(layout[1]));
            tfSeatLayout.setText(room.getSeatingLayout());
            tfTotalCap.setText(String.valueOf(room.getTotalCapacity()));
            tfEquipment.setText(room.getEquipment() != null ? room.getEquipment() : "");
        }
    }

    @FXML
    void handleTableClick(MouseEvent event) {
        ScreeningRoom selected = tblRoomList.getSelectionModel().getSelectedItem();
        showRoomDetails(selected);
    }

    @FXML
    void handleFindClick(MouseEvent event) {
        String keyword = tfFind.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadRoomData();
            return;
        }

        ObservableList<ScreeningRoom> filteredList = FXCollections.observableArrayList();
        for (ScreeningRoom room : roomList) {
            String roomTypeName = RoomTypeDAO.getRoomTypeNameById(room.getRoomTypeId()).toLowerCase();
            if (room.getRoomNumber().toLowerCase().contains(keyword) ||
                    roomTypeName.contains(keyword) ||
                    room.getRoomStatus().toLowerCase().contains(keyword)) {
                filteredList.add(room);
            }
        }
        tblRoomList.setItems(filteredList);
    }

    @FXML
    void handleAddNewRoom(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Manager/ScreeningRoom/AddRoom.fxml"));
            Parent root = loader.load();
            AddRoomController controller = loader.getController();
            controller.initializeSpinners();
            controller.setRoomStatusCombo();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Room");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadRoomData(); // Refresh
        } catch (IOException e) {
            System.out.println();
        }
    }

    @FXML
    void handleEditRoom(ActionEvent event) {
        ScreeningRoom selected = tblRoomList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a room to edit.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Manager/ScreeningRoom/EditRoom.fxml"));
            Parent root = loader.load();

            EditRoomController controller = loader.getController();
            controller.initializeSpinners();
            controller.setRoomData(selected);
            controller.setRoomStatusCombo();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Room");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadRoomData(); // Refresh
        } catch (IOException e) {
            System.out.println();
        }
    }

    @FXML
    void handleViewSeatLayout(ActionEvent event) {
        ScreeningRoom selected = tblRoomList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a room to view seat layout.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Manager/ScreeningRoom/ViewSeatingLayout.fxml"));
            Parent root = loader.load();

            SeatingLayoutController controller = loader.getController();
            controller.setRoomData(selected);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Seat Layout: Room " + selected.getRoomNumber());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadRoomData();
        } catch (IOException e) {
            System.out.println();
        }
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText("No Room Selected");
        alert.setContentText(content);
        alert.showAndWait();
    }
}