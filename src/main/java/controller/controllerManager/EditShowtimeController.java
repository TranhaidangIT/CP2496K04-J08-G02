package controller.controllerManager;

import dao.MovieDAO;
import dao.ScreeningRoomDAO;
import dao.ShowtimeDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import models.Movie;
import models.ScreeningRoom;
import models.Showtime;

import javafx.event.ActionEvent;
import java.time.LocalTime;
import java.util.*;

public class EditShowtimeController {

    @FXML private Button addTimeButton, btnCancel, btnDelete, btnUpdate;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> hourSpinner, minuteSpinner;
    @FXML private ComboBox<String> movieComboBox, roomComboBox;
    @FXML private GridPane timeSlotGrid;

    private List<LocalTime> addedTimeSlots = new ArrayList<>();
    private Map<String, Movie> movieMap = new HashMap<>();
    private Map<String, ScreeningRoom> roomMap = new HashMap<>();

    private Showtime mainShowtime;
    private List<Showtime> showtimeGroup;
    private final int maxColumns = 10;

    private Runnable refreshCallback;

    @FXML
    public void initialize() {
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 10));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 5));

        hourSpinner.getEditor().setTextFormatter(createTwoDigitFormatter());
        minuteSpinner.getEditor().setTextFormatter(createTwoDigitFormatter());

        loadMovieData();
        loadRoomData();
    }

    private TextFormatter<String> createTwoDigitFormatter() {
        return new TextFormatter<>(change -> {
            if (!change.getControlNewText().matches("\\d{0,2}")) return null;
            return change;
        });
    }

    private void loadMovieData() {
        try {
            List<Movie> movies = MovieDAO.getAllMovies();
            for (Movie movie : movies) {
                String display = movie.getTitle() + " (" + movie.getReleasedDate().getYear() + ")";
                movieComboBox.getItems().add(display);
                movieMap.put(display, movie);
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to load movie data.");
        }
    }

    private void loadRoomData() {
        try {
            List<ScreeningRoom> rooms = ScreeningRoomDAO.getAllRooms();
            for (ScreeningRoom room : rooms) {
                String display = room.getRoomNumber();
                roomComboBox.getItems().add(display);
                roomMap.put(display, room);
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to load room data.");
        }
    }

    public void setShowtimeToEdit(Showtime mainShowtime, List<Showtime> showtimeGroup) {
        this.mainShowtime = mainShowtime;
        this.showtimeGroup = showtimeGroup;

        datePicker.setValue(mainShowtime.getShowDate());

        for (Map.Entry<String, Movie> entry : movieMap.entrySet()) {
            if (entry.getValue().getMovieId() == mainShowtime.getMovieId()) {
                movieComboBox.setValue(entry.getKey());
                break;
            }
        }

        for (Map.Entry<String, ScreeningRoom> entry : roomMap.entrySet()) {
            if (entry.getValue().getRoomId() == mainShowtime.getRoomId()) {
                roomComboBox.setValue(entry.getKey());
                break;
            }
        }

        for (Showtime st : showtimeGroup) {
            addedTimeSlots.add(st.getShowTime());
        }

        updateTimeSlotGrid();
    }

    private void updateTimeSlotGrid() {
        timeSlotGrid.getChildren().clear();

        for (int i = 0; i < addedTimeSlots.size(); i++) {
            LocalTime time = addedTimeSlots.get(i);
            Label timeLabel = new Label(time.toString());

            Button removeBtn = new Button("X");
            removeBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
            removeBtn.setOnAction(e -> {
                // Xác nhận khi xóa 1 khung giờ
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Xác nhận xóa");
                confirm.setHeaderText("Bạn có chắc muốn xóa suất chiếu này?");
                confirm.setContentText(time.toString());
                Optional<ButtonType> result = confirm.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // Xóa trong DB nếu suất này nằm trong nhóm hiện tại
                    Showtime target = showtimeGroup.stream()
                            .filter(st -> st.getShowTime().equals(time))
                            .findFirst()
                            .orElse(null);
                    if (target != null) {
                        ShowtimeDAO.deleteShowtimeById(target.getShowtimeId());
                        showtimeGroup.remove(target);
                    }

                    addedTimeSlots.remove(time);
                    updateTimeSlotGrid();
                }
            });

            HBox hBox = new HBox(5, timeLabel, removeBtn);
            hBox.setStyle("-fx-background-color: #f1f1f1; -fx-padding: 5; -fx-border-color: #ccc;");
            int col = i % maxColumns;
            int row = i / maxColumns;
            timeSlotGrid.add(hBox, col, row);
        }
    }

    @FXML
    void handleAddTimeSlot(ActionEvent event) {
        int hour = hourSpinner.getValue();
        int minute = minuteSpinner.getValue();
        LocalTime time = LocalTime.of(hour, minute);

        if (addedTimeSlots.contains(time)) {
            showAlert("Duplicate", "This time slot already exists.");
            return;
        }

        addedTimeSlots.add(time);
        updateTimeSlotGrid();
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        if (movieComboBox.getValue() == null || roomComboBox.getValue() == null || datePicker.getValue() == null || addedTimeSlots.isEmpty()) {
            showAlert("Error", "Please fill in all required fields.");
            return;
        }

        Movie movie = movieMap.get(movieComboBox.getValue());
        ScreeningRoom room = roomMap.get(roomComboBox.getValue());

        if (movie == null || room == null) {
            showAlert("Invalid", "Movie or room selection is invalid.");
            return;
        }

        for (Showtime st : showtimeGroup) {
            ShowtimeDAO.deleteShowtimeById(st.getShowtimeId());
        }

        for (LocalTime time : addedTimeSlots) {
            LocalTime end = time.plusMinutes(movie.getDuration());
            boolean success = ShowtimeDAO.insertShowtime(
                    movie.getMovieId(),
                    room.getRoomId(),
                    datePicker.getValue(),
                    time,
                    end
            );
            if (!success) {
                showAlert("Error", "Failed to insert showtime: " + time);
                return;
            }
        }

        showAlert("Success", "Showtimes updated.");
        if (refreshCallback != null) refreshCallback.run();
        handleCancel(null);
    }

    @FXML
    void handleDelete(ActionEvent event) {
        // Xác nhận khi xóa cả nhóm
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Bạn có chắc muốn xóa toàn bộ suất chiếu này?");
        confirm.setContentText("Thao tác này không thể hoàn tác.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            for (Showtime st : showtimeGroup) {
                ShowtimeDAO.deleteShowtimeById(st.getShowtimeId());
            }
            showAlert("Deleted", "Showtimes deleted.");
            if (refreshCallback != null) refreshCallback.run();
            handleCancel(null);
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    public void handleLoadShowtime(ActionEvent actionEvent) {
    }
}
