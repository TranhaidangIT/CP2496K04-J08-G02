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

    @FXML private ComboBox<String> cbMovieList;
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
    private Map<String, Movie> movieMap = new HashMap<>(); // To map movie titles to Movie objects

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
            String selectedTitle = cbMovieList.getSelectionModel().getSelectedItem();
            if (selectedTitle != null) {
                Movie movie = movieMap.get(selectedTitle);
                if (movie != null) {
                    tfDuration.setText(String.valueOf(movie.getDuration()));
                    isTimeSlotsSaved = false;
                }
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
        List<String> movieTitles = new ArrayList<>();
        movieMap.clear(); // Clear the map before populating
        for (Movie movie : movies) {
            movieTitles.add(movie.getTitle());
            movieMap.put(movie.getTitle(), movie);
        }
        cbMovieList.setItems(FXCollections.observableArrayList(movieTitles));
        if (!movieTitles.isEmpty()) {
            cbMovieList.getSelectionModel().selectFirst();
            Movie firstMovie = movieMap.get(movieTitles.get(0));
            if (firstMovie != null) {
                tfDuration.setText(String.valueOf(firstMovie.getDuration()));
            }
        }
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
                Movie movie = movieMap.get(st.getMovieTitle());
                if (movie != null) {
                    cbMovieList.getSelectionModel().select(st.getMovieTitle());

                    newTimeSlots.clear();
                    List<Showtime> movieShowtimes = showtimeDAO.getShowtimesByRoomDateMovie(selectedRoom.getRoomId(), selectedDate, movieMap.get(st.getMovieTitle()).getMovieId());
                    for (Showtime s : movieShowtimes) {
                        newTimeSlots.add(s.getShowTime());
                    }
                    Collections.sort(newTimeSlots);
                    renderTimeSlots();

                    tfDuration.setText(String.valueOf(movie.getDuration()));
                }
                isTimeSlotsSaved = false;
            });

            Button btnDeleteIndividual = new Button("Delete");
            btnDeleteIndividual.setOnAction(e -> {
                boolean confirmed = showConfirm("Delete Showtime", "Are you sure you want to delete this showtime?");
                if (confirmed) {
                    boolean deleted = showtimeDAO.deleteShowtime(st.getShowtimeId());
                    if (deleted) {
                        showAlert(Alert.AlertType.INFORMATION, "Deleted", "Showtime deleted successfully.");
                        loadShowtimes();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete showtime.");
                    }
                }
            });

            hbox.getChildren().addAll(lblTime, lblMovie, btnSelect, btnDeleteIndividual);
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
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    @FXML
    void handleDelete(ActionEvent event) {
        if (selectedShowtime == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a showtime to delete all for this movie.");
            return;
        }

        boolean confirmed = showConfirm("Delete All for Movie", "Are you sure you want to delete all showtimes for this movie in the room on this date?");
        if (confirmed) {
            ScreeningRoom selectedRoom = findRoomByName(tfRoom.getText());
            LocalDate selectedDate;
            try {
                selectedDate = LocalDate.parse(tfDate.getText());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Date", "Date format must be YYYY-MM-DD");
                return;
            }
            Movie movie = movieMap.get(selectedShowtime.getMovieTitle());
            if (movie != null) {
                boolean deleted = showtimeDAO.deleteShowtimesByRoomDateMovie(selectedRoom.getRoomId(), selectedDate, movie.getMovieId());
                if (deleted) {
                    showAlert(Alert.AlertType.INFORMATION, "Deleted", "All showtimes for the movie deleted successfully.");
                    loadShowtimes();
                    clearForm();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete showtimes.");
                }
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
        String selectedTitle = cbMovieList.getSelectionModel().getSelectedItem();
        String durationStr = tfDuration.getText();
        if (selectedTitle == null || durationStr.isEmpty()) {
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
        String selectedTitle = cbMovieList.getSelectionModel().getSelectedItem();
        if (selectedTitle != null) {
            Movie movie = movieMap.get(selectedTitle);
            if (movie != null) {
                tfDuration.setText(String.valueOf(movie.getDuration()));
                isTimeSlotsSaved = false;
            }
        } else {
            tfDuration.clear();
        }
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        ScreeningRoom selectedRoom = findRoomByName(tfRoom.getText());
        LocalDate selectedDate;
        try {
            selectedDate = LocalDate.parse(tfDate.getText());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Date", "Date format must be YYYY-MM-DD");
            return;
        }
        String selectedTitle = cbMovieList.getSelectionModel().getSelectedItem();
        String durationStr = tfDuration.getText();

        if (selectedRoom == null || selectedDate == null || selectedTitle == null || durationStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Data", "Please fill all required fields.");
            return;
        }
        if (newTimeSlots.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Time Slots", "Please add time slots before updating.");
            return;
        }

        try {
            int duration = Integer.parseInt(durationStr);
            Movie movie = movieMap.get(selectedTitle);
            if (movie != null) {
                showtimeDAO.deleteShowtimesByRoomDateMovie(selectedRoom.getRoomId(), selectedDate, movie.getMovieId());

                LocalTime closeTime = LocalTime.of(22, 0);

                for (LocalTime startTime : newTimeSlots) {
                    LocalTime endTime = startTime.plusMinutes(duration + breakTime);

                    if (endTime.isAfter(closeTime)) {
                        showAlert(Alert.AlertType.WARNING, "Invalid Showtime", "Showtime starting at " + startTime + " ends after 22:00. Please adjust.");
                        return;
                    }

                    Showtime newShowtime = new Showtime();
                    newShowtime.setRoomId(selectedRoom.getRoomId());
                    newShowtime.setMovieId(movie.getMovieId());
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
            }
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