package controller.controllerManager;

import dao.MovieDAO;
import dao.ScreeningRoomDAO;
import dao.ShowtimeDAO;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import models.Movie;
import models.ScreeningRoom;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddShowtimeController {

    @FXML
    private Button addTimeButton;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnInsert;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Spinner<Integer> hourSpinner;

    @FXML
    private Spinner<Integer> minuteSpinner;

    @FXML
    private ComboBox<String> movieComboBox;

    @FXML
    private ComboBox<String> roomComboBox;

    @FXML
    private GridPane timeSlotGrid;

    private List<LocalTime> addedTimeSlots = new ArrayList<>();

    private final int maxColumns = 12;

    // Map tên -> object để truy xuất ID khi insert
    private Map<String, Movie> movieMap = new HashMap<>();
    private Map<String, ScreeningRoom> roomMap = new HashMap<>();

    @FXML
    public void initialize() {
        // Spinner giờ/phút
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 10));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 5));

        hourSpinner.getEditor().setTextFormatter(createTwoDigitFormatter());
        minuteSpinner.getEditor().setTextFormatter(createTwoDigitFormatter());

        // Load dữ liệu từ DB
        loadMovieData();
        loadRoomData();
    }

    private void loadMovieData() {
        try {
            List<Movie> movies = MovieDAO.getAllMovies(); // nên gọi version đầy đủ
            for (Movie movie : movies) {
                String display = movie.getTitle() + " (" + movie.getReleasedDate().getYear() + ")";
                movieComboBox.getItems().add(display);
                movieMap.put(display, movie);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load movie data.");
        }
    }


    private void loadRoomData() {
        try {
            List<ScreeningRoom> rooms = ScreeningRoomDAO.getAllRooms();
            for (ScreeningRoom room : rooms) {
                String display = "Room " + room.getRoomNumber();
                roomComboBox.getItems().add(display);
                roomMap.put(display, room);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load room data.");
        }
    }

    private TextFormatter<String> createTwoDigitFormatter() {
        return new TextFormatter<>(change -> {
            if (!change.getControlNewText().matches("\\d{0,2}")) return null;
            return change;
        });
    }

    @FXML
    void handleAddTimeSlot(ActionEvent event) {
        int hour = hourSpinner.getValue();
        int minute = minuteSpinner.getValue();
        LocalTime time = LocalTime.of(hour, minute);

        if (addedTimeSlots.contains(time)) {
            showAlert("Duplicate Time", "This time slot has already been added.");
            return;
        }

        addedTimeSlots.add(time);
        updateTimeSlotGrid();
    }

    private void updateTimeSlotGrid() {
        timeSlotGrid.getChildren().clear();

        for (int i = 0; i < addedTimeSlots.size(); i++) {
            LocalTime time = addedTimeSlots.get(i);

            Label timeLabel = new Label(time.toString());
            Button removeButton = new Button("X");
            removeButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
            removeButton.setOnAction(e -> {
                addedTimeSlots.remove(time);
                updateTimeSlotGrid();
            });

            HBox hBox = new HBox(5, timeLabel, removeButton);
            hBox.setStyle("-fx-background-color: #f1f1f1; -fx-padding: 5; -fx-border-color: #ccc; -fx-border-radius: 4;");

            int col = i % maxColumns;
            int row = i / maxColumns;
            timeSlotGrid.add(hBox, col, row);
        }
    }

    @FXML
    void handleInsert(ActionEvent event) {
        String selectedMovie = movieComboBox.getValue();
        String selectedRoom = roomComboBox.getValue();

        if (selectedMovie == null || selectedRoom == null || datePicker.getValue() == null || addedTimeSlots.isEmpty()) {
            showAlert("Incomplete Input", "Please fill in all fields and add at least one time slot.");
            return;
        }

        Movie movie = movieMap.get(selectedMovie);
        ScreeningRoom room = roomMap.get(selectedRoom);

        if (movie == null || room == null) {
            showAlert("Invalid Selection", "Invalid movie or room selection.");
            return;
        }

        for (LocalTime time : addedTimeSlots) {
            LocalTime endTime = time.plusMinutes(movie.getDuration());

            boolean success = ShowtimeDAO.insertShowtime(
                    movie.getMovieId(),
                    room.getRoomId(),
                    datePicker.getValue(),
                    time,
                    endTime
            );

            if (!success) {
                showAlert("Error", "Failed to insert showtime at " + time);
                return;
            }
        }

        showAlert("Success", "Showtimes inserted successfully.");
        handleCancel(null); // Đóng cửa sổ
    }


    @FXML
    void handleCancel(ActionEvent event) {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
