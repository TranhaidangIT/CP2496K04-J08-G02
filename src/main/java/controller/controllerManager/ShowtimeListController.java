package controller.controllerManager;

import dao.ScreeningRoomDAO;
import dao.ShowtimeDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.ScreeningRoom;
import models.Showtime;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShowtimeListController {

    @FXML
    private ComboBox<String> cbRoomFilter;

    @FXML
    private DatePicker pickerDateFilter;

    @FXML
    private ScrollPane scrShowtimeItemList;

    @FXML
    private VBox showtimeItemList;

    private final ScreeningRoomDAO roomDAO = new ScreeningRoomDAO();
    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();

    @FXML
    public void initialize() {
        List<ScreeningRoom> rooms = roomDAO.getAllRooms();
        ObservableList<String> roomNames = FXCollections.observableArrayList();
        for (ScreeningRoom r : rooms) {
            roomNames.add(r.getRoomNumber());
        }
        cbRoomFilter.setItems(roomNames);

        // Bỏ chọn phòng mặc định để hiển thị tất cả
        cbRoomFilter.getSelectionModel().clearSelection();

        pickerDateFilter.setValue(LocalDate.now());

        // Load toàn bộ suất chiếu tất cả phòng ngày hôm nay
        loadAllShowtimesByDate(pickerDateFilter.getValue());

        // Khi thay đổi ngày, load lại toàn bộ suất chiếu, reset phòng chọn
        pickerDateFilter.setOnAction(e -> {
            cbRoomFilter.getSelectionModel().clearSelection();
            loadAllShowtimesByDate(pickerDateFilter.getValue());
        });

        // Khi chọn phòng, lọc showtime theo phòng, ngày
        cbRoomFilter.setOnAction(e -> {
            String selectedRoom = cbRoomFilter.getSelectionModel().getSelectedItem();
            LocalDate selectedDate = pickerDateFilter.getValue();
            if (selectedRoom == null) {
                loadAllShowtimesByDate(selectedDate);
            } else {
                loadShowtimesByRoomAndDate(selectedRoom, selectedDate);
            }
        });
    }

    private void loadAllShowtimesByDate(LocalDate date) {
        showtimeItemList.getChildren().clear();

        if (date == null) return;

        // Lấy tất cả phòng
        List<ScreeningRoom> rooms = roomDAO.getAllRooms();

        try {
            for (ScreeningRoom room : rooms) {
                // Lấy showtime phòng và ngày
                List<Showtime> showtimes = showtimeDAO.getShowtimesByRoomDate(room.getRoomId(), date);
                if (showtimes.isEmpty()) continue;

                // Nhóm theo movieId
                Map<Integer, List<Showtime>> grouped = showtimes.stream()
                        .collect(Collectors.groupingBy(Showtime::getMovieId));

                for (List<Showtime> group : grouped.values()) {
                    if (group.isEmpty()) continue;

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Manager/Showtime/RoomShowtimeBlock.fxml"));
                    Node node = loader.load();

                    RoomShowtimeBlockController controller = loader.getController();
                    controller.setRoomAndShowtimes(room, group);

                    showtimeItemList.getChildren().add(node);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadShowtimesByRoomAndDate(String roomNumber, LocalDate date) {
        showtimeItemList.getChildren().clear();

        if (roomNumber == null || date == null) return;

        ScreeningRoom room = roomDAO.getRoomByName(roomNumber);
        if (room == null) return;

        List<Showtime> showtimes = showtimeDAO.getShowtimesByRoomDate(room.getRoomId(), date);

        Map<Integer, List<Showtime>> grouped = showtimes.stream()
                .collect(Collectors.groupingBy(Showtime::getMovieId));

        try {
            for (List<Showtime> group : grouped.values()) {
                if (group.isEmpty()) continue;

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Manager/Showtime/RoomShowtimeBlock.fxml"));
                Node node = loader.load();

                RoomShowtimeBlockController controller = loader.getController();
                controller.setRoomAndShowtimes(room, group);

                showtimeItemList.getChildren().add(node);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleAddNewShowtime(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Manager/Showtime/AddShowtime.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Showtime");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Reload dữ liệu sau khi đóng add showtime
            String selectedRoom = cbRoomFilter.getSelectionModel().getSelectedItem();
            LocalDate selectedDate = pickerDateFilter.getValue();
            if (selectedRoom == null) {
                loadAllShowtimesByDate(selectedDate);
            } else {
                loadShowtimesByRoomAndDate(selectedRoom, selectedDate);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
