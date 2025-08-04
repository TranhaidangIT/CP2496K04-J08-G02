package controller.controllerManager;

import dao.ScreeningRoomDAO;
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
import java.util.List;

public class RoomListController {

    @FXML
    private Button btnAdd, btnEdit, btnFind;

    @FXML
    private TableView<ScreeningRoom> tblRoomList;

    @FXML
    private TableColumn<ScreeningRoom, Integer> colRoomID, colRoomNumber, colRoomCap;

    @FXML
    private TableColumn<ScreeningRoom, String> colRoomType, colRoomStatus, colSeatLayout;

    @FXML
    private Label lbRoomId;

    @FXML
    private TextField tfRoomNumb, tfSeatLayout, tfTotalCap, tfRoomType, tfRoomStatus, tfEquipment, tfCreatedAt;

    private ScreeningRoom selected;

    @FXML
    public void initialize() {
        setupTable();
        loadRoomList();
        tblRoomList.setOnMouseClicked(this::handleSelectRow);
    }

    private void setupTable() {
        colRoomID.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        colRoomNumber.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colRoomCap.setCellValueFactory(new PropertyValueFactory<>("totalCapacity"));
        colRoomType.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        colRoomStatus.setCellValueFactory(new PropertyValueFactory<>("roomStatus"));
        colSeatLayout.setCellValueFactory(new PropertyValueFactory<>("seatingLayout"));
    }

    public void loadRoomList() {
        List<ScreeningRoom> rooms = ScreeningRoomDAO.getAllRooms();
        tblRoomList.getItems().setAll(rooms);
    }

    @FXML
    void handleAddNewRoom(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Manager/ScreeningRoom/AddRoom.fxml"));
            Parent root = loader.load();

            AddRoomController addCtrl = loader.getController();
            addCtrl.setRoomListController(this);

            Stage stage = new Stage();
            stage.setTitle("Add New Room");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadRoomList();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Add Room form.");
        }
    }

    @FXML
    void handleEditRoom(ActionEvent event) {
        if (selected == null) {
            showAlert("No Room Selected", "Please select a room to edit.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Manager/ScreeningRoom/EditRoom.fxml"));
            Parent root = loader.load();

            EditRoomController editCtrl = loader.getController();
            editCtrl.setRoomListController(this);
            editCtrl.setRoomData(selected);

            Stage stage = new Stage();
            stage.setTitle("Edit Room");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadRoomList();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Edit Room form.");
        }
    }

    @FXML
    void handleSelectRow(MouseEvent event) {
        selected = tblRoomList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            tfRoomNumb.setText(selected.getRoomNumber());
            tfSeatLayout.setText(selected.getSeatingLayout());
            tfTotalCap.setText(String.valueOf(selected.getTotalCapacity()));
            tfRoomType.setText(selected.getRoomType());
            tfRoomStatus.setText(selected.getRoomStatus());
            tfEquipment.setText(selected.getEquipment());
            tfCreatedAt.setText(String.valueOf(selected.getCreatedAt()));
        }
    }

    @FXML
    void btnFindRoom(ActionEvent event) {
        showAlert("Coming Soon", "Find room functionality will be implemented later.");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void refreshRoomList() {
        loadRoomList();
    }
}
