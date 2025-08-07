package controller.controllerManager;

import dao.ShowtimeDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Showtime;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShowtimeListController {

    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnFind;
    @FXML private AnchorPane paneFind;
    @FXML private AnchorPane paneShowtimeList;
    @FXML private ScrollPane scrShowtimeItemList;
    @FXML private VBox showtimeItemList;
    @FXML private TextField tfFind;
    @FXML private AnchorPane tfFindShowtime;

    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();

    @FXML
    public void initialize() {
        loadShowtimeItems();
    }

    private void loadShowtimeItems() {
        showtimeItemList.getChildren().clear();

        List<Showtime> allShowtimes = ShowtimeDAO.getAllShowtimes();

        Map<String, List<Showtime>> grouped = allShowtimes.stream()
                .collect(Collectors.groupingBy(s -> s.getMovieId() + "-" + s.getRoomId() + "-" + s.getShowDate()));

        for (List<Showtime> group : grouped.values()) {
            if (group.isEmpty()) continue;

            Showtime first = group.getFirst();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Manager/Showtime/ShowtimeItem.fxml"));
                Node itemNode = loader.load();

                ShowtimeItemController itemController = loader.getController();
                itemController.setShowtimeItem(first, group);

                showtimeItemList.getChildren().add(itemNode);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void handleAddNewShowtime(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Manager/Showtime/AddShowtime.fxml"));
            AnchorPane addShowtimePane = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add New Showtime");
            stage.setScene(new Scene(addShowtimePane));
            stage.show();

            // Auto refresh after add
            stage.setOnHidden(e -> loadShowtimeItems());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleFindShowtime(ActionEvent event) {
        String keyword = tfFind.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadShowtimeItems();
            return;
        }

        List<Showtime> filtered = ShowtimeDAO.getAllShowtimes().stream()
                .filter(s -> s.getMovieTitle().toLowerCase().contains(keyword) ||
                        s.getRoomName().toLowerCase().contains(keyword))
                .toList();

        showtimeItemList.getChildren().clear();

        Map<String, List<Showtime>> grouped = filtered.stream()
                .collect(Collectors.groupingBy(s -> s.getMovieId() + "-" + s.getRoomId() + "-" + s.getShowDate()));

        for (List<Showtime> group : grouped.values()) {
            if (group.isEmpty()) continue;

            Showtime first = group.getFirst();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Manager/Showtime/ShowtimeItem.fxml"));
                Node itemNode = loader.load();

                ShowtimeItemController itemController = loader.getController();
                itemController.setShowtimeItem(first, group);

                showtimeItemList.getChildren().add(itemNode);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleFindClick(javafx.scene.input.MouseEvent mouseEvent) {
        String keyword = tfFind.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadShowtimeItems();
            return;
        }

        List<Showtime> allShowtimes = ShowtimeDAO.getAllShowtimes().stream()
                .filter(s -> s.getMovieTitle().toLowerCase().contains(keyword) ||
                        s.getRoomName().toLowerCase().contains(keyword))
                .toList();

        showtimeItemList.getChildren().clear();

        Map<String, List<Showtime>> grouped = allShowtimes.stream()
                .collect(Collectors.groupingBy(s -> s.getMovieId() + "-" + s.getRoomId() + "-" + s.getShowDate()));

        for (List<Showtime> group : grouped.values()) {
            if (group.isEmpty()) continue;

            Showtime first = group.getFirst();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Manager/Showtime/ShowtimeItem.fxml"));
                Node itemNode = loader.load();

                ShowtimeItemController itemController = loader.getController();
                itemController.setShowtimeItem(first, group);

                showtimeItemList.getChildren().add(itemNode);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
