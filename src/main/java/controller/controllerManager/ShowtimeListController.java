package controller.controllerManager;

import dao.ShowtimeDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import models.Showtime;

import java.io.IOException;
import java.util.List;

public class ShowtimeListController {

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnFind;

    @FXML
    private TableColumn<Showtime, String> colDate;

    @FXML
    private TableColumn<Showtime, Integer> colID;

    @FXML
    private TableColumn<Showtime, String> colMovieTitle;

    @FXML
    private TableColumn<Showtime, String> colRoom;

    @FXML
    private AnchorPane paneFind;

    @FXML
    private AnchorPane paneShowtimetable;

    @FXML
    private TableView<Showtime> tbfShowtimes;

    @FXML
    private TextField tfFind;

    private ObservableList<Showtime> showtimeList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadShowtimeList();
    }

    private void setupTableColumns() {
        colID.setCellValueFactory(new PropertyValueFactory<>("showtimeId"));
        colMovieTitle.setCellValueFactory(new PropertyValueFactory<>("movieTitle"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomName"));
        colDate.setCellValueFactory(cellData -> {
            Showtime s = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    s.getShowDate() + " " + s.getShowTime()
            );
        });
    }

    private void loadShowtimeList() {
        List<Showtime> list = ShowtimeDAO.getAllShowtimes(); // You must implement this
        showtimeList.setAll(list);
        tbfShowtimes.setItems(showtimeList);
    }

    @FXML
    void handleAddNewShowtime(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Manager/Showtime/AddShowtime.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Add New Showtime");
            stage.showAndWait();

            loadShowtimeList(); // Refresh
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleEditShowtime(ActionEvent event) {
        Showtime selected = tbfShowtimes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No showtime selected", "Please select a showtime to edit.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Manager/Showtime/EditShowtime.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));

            // Pass data to edit controller
            EditShowtimeController controller = loader.getController();
            controller.setShowtime(selected);

            stage.setTitle("Edit Showtime");
            stage.showAndWait();

            loadShowtimeList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleFindShowtime(ActionEvent event) {
        String keyword = tfFind.getText().trim();
        if (keyword.isEmpty()) {
            loadShowtimeList();
            return;
        }

        List<Showtime> result = ShowtimeDAO.findShowtimes(keyword); // Implement this
        showtimeList.setAll(result);
        tbfShowtimes.setItems(showtimeList);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
