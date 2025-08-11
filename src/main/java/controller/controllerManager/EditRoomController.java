package controller.controllerManager;

import dao.RoomTypeDAO;
import dao.ScreeningRoomDAO;
import dao.SeatDAO;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.RoomType;
import models.ScreeningRoom;

import java.sql.SQLException;
import java.util.List;

public class EditRoomController {

    @FXML private Button btnCancel;
    @FXML private Button btnDelete;
    @FXML private Button btnUpdate;

    @FXML private ComboBox<String> cbRoomStatus;
    @FXML private ComboBox<RoomType> cbRoomType;

    @FXML private Label lblLayoutWarning;
    @FXML private Spinner<Integer> spinnerColumns;
    @FXML private Spinner<Integer> spinnerRows;

    @FXML private TextArea tfEquipment;
    @FXML private TextField tfRoomNumber;
    @FXML private TextField tfTotalCap;


    private ScreeningRoom currentRoom;


    @FXML
    void handleCancel(ActionEvent event) {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    @FXML
    void handleDelete(ActionEvent event) throws SQLException {
        if (currentRoom == null) return;

        // Confirm deletion
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this room?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        boolean success = ScreeningRoomDAO.deleteRoomById(currentRoom.getRoomId());

        if (success) {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Room Deleted");
            info.setHeaderText(null);
            info.setContentText("Room deleted successfully along with its seats.");
            info.showAndWait();
            ((Stage) btnDelete.getScene().getWindow()).close();
        } else {
            Alert warn = new Alert(Alert.AlertType.WARNING);
            warn.setTitle("Cannot Delete Room");
            warn.setHeaderText(null);
            warn.setContentText("This room has scheduled showtimes.\n" +
                    "Please delete those showtimes or wait until they are finished.");
            warn.showAndWait();
        }
    }


    @FXML
    void handleUpdate(ActionEvent event) {
        try {
            String roomNumber = tfRoomNumber.getText();
            int rows = spinnerRows.getValue();
            int cols = spinnerColumns.getValue();
            String layout = rows + "x" + cols;
            int totalCap = rows * cols;

            RoomType selectedType = cbRoomType.getValue();
            if (selectedType == null) {
                System.out.println("Please select type of room");
                return;
            }

            String equipment = tfEquipment.getText();
            String status = cbRoomStatus.getValue();
            if (status == null) {
                System.out.println("Please select room status.");
                return;
            }

            currentRoom.setRoomNumber(roomNumber);
            currentRoom.setSeatingLayout(layout);
            currentRoom.setTotalCapacity(totalCap);
            currentRoom.setRoomTypeId(selectedType.getRoomTypeId());
            currentRoom.setEquipment(equipment);
            currentRoom.setRoomStatus(status);

            //Update Seat
            boolean updated = ScreeningRoomDAO.updateRoom(currentRoom);

            if (updated) {
                //Delete old seat
                SeatDAO.deleteSeatsByRoomId(currentRoom.getRoomId());

                //Insert new seat
                SeatDAO.insertSeatsForRoom(
                        currentRoom.getRoomId(),
                        rows,
                        cols,
                        selectedType.getRoomTypeId()
                );

                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Room Updated");
                info.setHeaderText(null);
                info.setContentText("Room and seat layout updated successfully!");
                info.showAndWait();

                ((Stage) btnUpdate.getScene().getWindow()).close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void setRoomData(ScreeningRoom selected) {
        this.currentRoom = selected;

        // Init combo + spinner if not done in controller loading
        initializeSpinners();
        setRoomStatusCombo();
        setRoomTypeCombo();

        tfRoomNumber.setText(selected.getRoomNumber());
        tfEquipment.setText(selected.getEquipment());
        tfTotalCap.setText(String.valueOf(selected.getTotalCapacity()));

        // Parse seating layout
        String[] layout = selected.getSeatingLayout().split("x");
        int rows = Integer.parseInt(layout[0]);
        int cols = Integer.parseInt(layout[1]);
        spinnerRows.getValueFactory().setValue(rows);
        spinnerColumns.getValueFactory().setValue(cols);

        // Set room status
        cbRoomStatus.setValue(selected.getRoomStatus());

        // Set room type
        for (RoomType type : cbRoomType.getItems()) {
            if (type.getRoomTypeId() == selected.getRoomTypeId()) {
                cbRoomType.setValue(type);
                break;
            }
        }
    }

    public void initializeSpinners() {
        spinnerRows.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 5));
        spinnerColumns.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 5));

        spinnerRows.valueProperty().addListener((obs, oldVal, newVal) -> updateSeatLayout());
        spinnerColumns.valueProperty().addListener((obs, oldVal, newVal) -> updateSeatLayout());
    }

    private void updateSeatLayout() {
        int rows = spinnerRows.getValue();
        int cols = spinnerColumns.getValue();
        tfTotalCap.setText(String.valueOf(rows * cols));
    }

    public void setRoomStatusCombo() {
        cbRoomStatus.setItems(FXCollections.observableArrayList(
                "Available", "Unavailable", "Maintenance"
        ));
    }

    public void setRoomTypeCombo() {
        List<RoomType> roomTypes = RoomTypeDAO.getAllRoomTypes();
        cbRoomType.setItems(FXCollections.observableArrayList(roomTypes));
    }
}
