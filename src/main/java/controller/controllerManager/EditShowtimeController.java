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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Movie;
import models.ScreeningRoom;
import models.Showtime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class EditShowtimeController {

    @FXML private Button addTimeButton;
    @FXML private Button btnCancel;
    @FXML private Button btnDelete;
    @FXML private Button btnRefresh;
    @FXML private Button btnSaveSlotTime;
    @FXML private Button btnUpdate;

    @FXML private ComboBox<Movie> cbMovieList;
    @FXML private TextField tfRoom;
    @FXML private TextField tfDate;
    @FXML private Spinner<String> hourSpinner;
    @FXML private Spinner<String> minuteSpinner;
    @FXML private TextField tfBreakTime;
    @FXML private TextField tfDuration;
    @FXML private GridPane timeSlotGrid;
    @FXML private VBox vboxShowtimeExistingList;

    private final MovieDAO movieDAO = new MovieDAO();
    private final ScreeningRoomDAO roomDAO = new ScreeningRoomDAO();
    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();

    private final List<LocalTime> newTimeSlots = new ArrayList<>();
    private Showtime selectedShowtime = null;
    private int breakTime = 30;
    private List<ScreeningRoom> roomList;

    private boolean isTimeSlotsSaved = false;

    @FXML
    public void initialize() {
        List<String> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d", i));
        }
        SpinnerValueFactory<String> hourFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(FXCollections.observableArrayList(hours));
        hourFactory.setValue("09");
        hourSpinner.setValueFactory(hourFactory);

        List<String> minutes = new ArrayList<>();
        for (int i = 0; i < 60; i += 5) {
            minutes.add(String.format("%02d", i));
        }
        SpinnerValueFactory<String> minuteFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(FXCollections.observableArrayList(minutes));
        minuteFactory.setValue("00");
        minuteSpinner.setValueFactory(minuteFactory);

        loadRooms();
        loadMovies();

        tfDate.setText(LocalDate.now().toString());
        tfDate.setEditable(false);

        tfBreakTime.setText(String.valueOf(breakTime));
        tfBreakTime.setEditable(false);

        tfRoom.setEditable(false);

        loadShowtimes();

        cbMovieList.setOnAction(e -> {
            Movie movie = cbMovieList.getSelectionModel().getSelectedItem();
            if (movie != null) {
                tfDuration.setText(String.valueOf(movie.getDuration()));
                isTimeSlotsSaved = false;
            } else {
                tfDuration.clear();
            }
        });
    }

    private void loadRooms() {
        roomList = roomDAO.getAllRooms();
        if (!roomList.isEmpty()) {
            String firstRoomName = roomList.get(0).getRoomNumber();
            tfRoom.setText(firstRoomName);
        }
    }

    private void loadMovies() {
        List<Movie> movies = movieDAO.getAllMovies();
        cbMovieList.setItems(FXCollections.observableArrayList(movies));
        if (!movies.isEmpty()) {
            cbMovieList.getSelectionModel().selectFirst();
            tfDuration.setText(String.valueOf(movies.get(0).getDuration()));
        }

        cbMovieList.setCellFactory(lv -> new ListCell<Movie>() {
            @Override
            protected void updateItem(Movie item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle());
            }
        });
        cbMovieList.setButtonCell(new ListCell<Movie>() {
            @Override
            protected void updateItem(Movie item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle());
            }
        });
    }

    private void loadShowtimes() {
        vboxShowtimeExistingList.getChildren().clear();
        newTimeSlots.clear();
        timeSlotGrid.getChildren().clear();
        tfDuration.clear();
        selectedShowtime = null;
        isTimeSlotsSaved = false;

        ScreeningRoom selectedRoom = findRoomByName(tfRoom.getText());
        if (selectedRoom == null) return;

        LocalDate selectedDate;
        try {
            selectedDate = LocalDate.parse(tfDate.getText());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Date", "Date format must be YYYY-MM-DD");
            return;
        }

        List<Showtime> showtimes = showtimeDAO.getShowtimesByRoomAndDate(selectedRoom.getRoomId(), selectedDate);

        for (Showtime st : showtimes) {
            HBox hbox = new HBox(10);
            Label lblTime = new Label(st.getShowTime().toString());
            Label lblMovie = new Label(st.getMovieTitle());

            Button btnSelect = new Button("Select");
            btnSelect.setOnAction(e -> {
                selectedShowtime = st;
                cbMovieList.getSelectionModel().select(findMovieById(st.getMovieId()));

                newTimeSlots.clear();
                newTimeSlots.add(st.getShowTime());
                renderTimeSlots();

                tfDuration.setText(String.valueOf(findMovieById(st.getMovieId()).getDuration()));

                isTimeSlotsSaved = false;
            });

            hbox.getChildren().addAll(lblTime, lblMovie, btnSelect);
            vboxShowtimeExistingList.getChildren().add(hbox);
        }
    }

    private ScreeningRoom findRoomByName(String name) {
        if (name == null) return null;
        for (ScreeningRoom room : roomList) {
            if (room.getRoomNumber().equals(name)) {
                return room;
            }
        }
        return null;
    }

    private Movie findMovieById(int movieId) {
        for (Movie m : cbMovieList.getItems()) {
            if (m.getMovieId() == movieId) return m;
        }
        return null;
    }

    private void renderTimeSlots() {
        timeSlotGrid.getChildren().clear();
        int row = 0;
        for (LocalTime time : newTimeSlots) {
            Label timeLabel = new Label(time.toString());
            Button btnRemove = new Button("X");
            btnRemove.setOnAction(e -> {
                newTimeSlots.remove(time);
                renderTimeSlots();
                isTimeSlotsSaved = false;
            });
            HBox hbox = new HBox(5, timeLabel, btnRemove);
            timeSlotGrid.add(hbox, 0, row++);
        }
    }

    @FXML
    void handleAddTimeSlot(ActionEvent event) {
        try {
            int hour = Integer.parseInt(hourSpinner.getValue());
            int minute = Integer.parseInt(minuteSpinner.getValue());
            LocalTime time = LocalTime.of(hour, minute);

            LocalTime openTime = LocalTime.of(8, 30);
            LocalTime closeTime = LocalTime.of(22, 0);

            if (time.isBefore(openTime) || time.isAfter(closeTime)) {
                showAlert(Alert.AlertType.WARNING, "Invalid Time", "Showtime must be between 08:30 and 22:00.");
                return;
            }

            if (!newTimeSlots.contains(time)) {
                newTimeSlots.add(time);
                Collections.sort(newTimeSlots);
                renderTimeSlots();
                isTimeSlotsSaved = false;
            } else {
                showAlert(Alert.AlertType.WARNING, "Duplicate Time Slot", "This time slot already exists.");
            }
        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Time", "Hour or minute is invalid.");
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        clearForm();
    }

    @FXML
    void handleDelete(ActionEvent event) {
        if (selectedShowtime == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a showtime to delete.");
            return;
        }

        boolean confirmed = showConfirm("Delete Showtime", "Are you sure you want to delete this showtime?");
        if (confirmed) {
            boolean deleted = showtimeDAO.deleteShowtime(selectedShowtime.getShowtimeId());
            if (deleted) {
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Showtime deleted successfully.");
                loadShowtimes();
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete showtime.");
            }
        }
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        loadShowtimes();
        clearForm();
    }

    @FXML
    void handleSaveTimeSlot(ActionEvent event) {
        if (newTimeSlots.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Time Slots", "Please add at least one time slot.");
            return;
        }
        Movie selectedMovie = cbMovieList.getSelectionModel().getSelectedItem();
        String durationStr = tfDuration.getText();
        if (selectedMovie == null || durationStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Data", "Please select a movie and enter duration.");
            return;
        }
        try {
            Integer.parseInt(durationStr);
            isTimeSlotsSaved = true;
            showAlert(Alert.AlertType.INFORMATION, "Saved", "Time slots saved. You can now update showtimes.");
        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Duration", "Duration must be a number.");
            isTimeSlotsSaved = false;
        }
    }

    @FXML
    void handleSelectMovie(ActionEvent event) {
        Movie movie = cbMovieList.getSelectionModel().getSelectedItem();
        if (movie != null) {
            tfDuration.setText(String.valueOf(movie.getDuration()));
            isTimeSlotsSaved = false;
        } else {
            tfDuration.clear();
        }
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        if (!isTimeSlotsSaved) {
            showAlert(Alert.AlertType.WARNING, "Not Saved", "Please save time slots before updating.");
            return;
        }

        if (selectedShowtime == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a showtime to update.");
            return;
        }

        ScreeningRoom selectedRoom = findRoomByName(tfRoom.getText());
        LocalDate selectedDate;
        try {
            selectedDate = LocalDate.parse(tfDate.getText());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Date", "Date format must be YYYY-MM-DD");
            return;
        }
        Movie selectedMovie = cbMovieList.getSelectionModel().getSelectedItem();
        String durationStr = tfDuration.getText();

        if (selectedRoom == null || selectedDate == null || selectedMovie == null || durationStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Data", "Please fill all required fields.");
            return;
        }
        if (newTimeSlots.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Time Slots", "Please add time slots before updating.");
            return;
        }

        try {
            int duration = Integer.parseInt(durationStr);

            // Delete old showtimes
            showtimeDAO.deleteShowtimesByRoomDateMovie(selectedRoom.getRoomId(), selectedDate, selectedMovie.getMovieId());

            LocalTime closeTime = LocalTime.of(22, 0);

            for (LocalTime startTime : newTimeSlots) {
                LocalTime endTime = startTime.plusMinutes(duration + breakTime);

                if (endTime.isAfter(closeTime)) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Showtime", "Showtime starting at " + startTime + " ends after 22:00. Please adjust.");
                    return;
                }

                Showtime newShowtime = new Showtime();
                newShowtime.setRoomId(selectedRoom.getRoomId());
                newShowtime.setMovieId(selectedMovie.getMovieId());
                newShowtime.setShowDate(selectedDate);
                newShowtime.setShowTime(startTime);
                newShowtime.setEndTime(endTime);

                showtimeDAO.insertShowtime(newShowtime);
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Showtimes updated successfully.");
            loadShowtimes();
            clearForm();

            isTimeSlotsSaved = false;

            Stage stage = (Stage) btnUpdate.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Duration", "Duration must be a number.");
        }
    }

    private void clearForm() {
        tfDuration.clear();
        newTimeSlots.clear();
        renderTimeSlots();
        selectedShowtime = null;
        isTimeSlotsSaved = false;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public void loadShowtimeToEdit(int roomId, LocalDate currentDate) {
        ScreeningRoom room = findRoomById(roomId);
        if (room != null) {
            tfRoom.setText(room.getRoomNumber());
        }
        tfDate.setText(currentDate.toString());
        loadShowtimes();
    }

    private ScreeningRoom findRoomById(int roomId) {
        for (ScreeningRoom room : roomList) {
            if (room.getRoomId() == roomId) {
                return room;
            }
        }
        return null;
    }
}
