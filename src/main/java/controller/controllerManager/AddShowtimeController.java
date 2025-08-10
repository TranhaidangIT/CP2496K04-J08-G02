package controller.controllerManager;

import dao.MovieDAO;
import dao.ScreeningRoomDAO;
import dao.ShowtimeDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Movie;
import models.ScreeningRoom;
import models.Showtime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AddShowtimeController {

    @FXML
    public HBox hboxShowtimeExisting;

    @FXML
    public ScrollPane scrTimeSlot;

    @FXML
    public AnchorPane paneTimeSlots;

    @FXML
    public VBox vboxShowtimeExistingList;

    @FXML
    private Button addTimeButton;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnInsert;

    @FXML
    private Button btnRefresh;

    @FXML
    private ComboBox<String> cbMovieList;

    @FXML
    private ComboBox<String> cbRoomList;

    @FXML
    private GridPane timeSlotGrid;

    @FXML
    private Spinner<Integer> hourSpinner;

    @FXML
    private Spinner<Integer> minuteSpinner;

    @FXML
    private DatePicker pickerDate;

    @FXML
    private TextField tfBreakTime;

    @FXML
    private TextField tfDuration;

    @FXML
    private Label txtMovieExisting;

    @FXML
    private Label txtTimeExisting;

    private final List<LocalTime> timeSlots = new ArrayList<>();

    private final List<Showtime> existingShowtimesCache = new ArrayList<>();

    private final MovieDAO movieDAO = new MovieDAO();
    private final ScreeningRoomDAO roomDAO = new ScreeningRoomDAO();
    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();

    @FXML
    public void initialize() {
        initializeComboBoxes();
        initializeSpinners();
        initializeDatePicker();

        tfBreakTime.setText("30");
        tfBreakTime.setDisable(true);

        tfDuration.setDisable(true);

        cbMovieList.setOnAction(this::handleSelectMovie);
        cbRoomList.setOnAction(this::handleSelectRoom);
        pickerDate.setOnAction(this::handleDatePicker);

        btnRefresh.setOnAction(this::handleRefresh);
    }

    private void initializeComboBoxes() {
        List<Movie> movies = movieDAO.getAllMovies();
        List<ScreeningRoom> rooms = roomDAO.getAllRooms();

        ObservableList<String> movieNames = FXCollections.observableArrayList();
        for (Movie m : movies) movieNames.add(m.getTitle());
        cbMovieList.setItems(movieNames);

        ObservableList<String> roomNames = FXCollections.observableArrayList();
        for (ScreeningRoom r : rooms) roomNames.add(r.getRoomNumber());
        cbRoomList.setItems(roomNames);
    }

    public void initializeSpinners() {
        SpinnerValueFactory<Integer> hourFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 10);
        hourSpinner.setValueFactory(hourFactory);

        SpinnerValueFactory<Integer> minuteFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 55, 0, 5);
        minuteSpinner.setValueFactory(minuteFactory);
    }

    private void initializeDatePicker() {
        pickerDate.setValue(LocalDate.now());
    }

    @FXML
    void handleAddTimeSlot(ActionEvent event) {
        String selectedRoom = cbRoomList.getValue();
        String selectedMovie = cbMovieList.getValue();

        if (selectedRoom == null) {
            showAlert(Alert.AlertType.WARNING, "Please select a room before adding a time slot.");
            return;
        }

        if (selectedMovie == null) {
            showAlert(Alert.AlertType.WARNING, "Please select a movie before adding a time slot.");
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(tfDuration.getText());
            if (duration <= 0) {
                showAlert(Alert.AlertType.ERROR, "Duration must be a positive integer.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Duration must be a number.");
            return;
        }

        int breakTime;
        try {
            breakTime = Integer.parseInt(tfBreakTime.getText());
        } catch (NumberFormatException e) {
            breakTime = 30;
        }

        LocalTime newTime = LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue());
        LocalDate selectedDate = pickerDate.getValue();

        ScreeningRoom room = roomDAO.getRoomByName(selectedRoom);
        if (room == null) {
            showAlert(Alert.AlertType.ERROR, "Selected room not found.");
            return;
        }
        List<Showtime> existingShowtimes = showtimeDAO.getShowtimesByRoomDate(room.getRoomId(), selectedDate);

        LocalTime newEndTime = newTime.plusMinutes(duration);
        LocalTime newEndWithBreak = newEndTime.plusMinutes(breakTime);

        // Check vượt qua nửa đêm (nếu endTime < startTime nghĩa là kết thúc ngày tiếp theo)
        if (newEndTime.isBefore(newTime)) {
            showAlert(Alert.AlertType.WARNING, "Showtime ends after midnight, which is not allowed.");
            return;
        }
        if (newEndWithBreak.isBefore(newTime)) {
            showAlert(Alert.AlertType.WARNING, "Showtime plus break ends after midnight, which is not allowed.");
            return;
        }

        LocalTime openTime = LocalTime.of(8, 0);
        LocalTime closeTime = LocalTime.of(22, 0);

        if (newTime.isBefore(openTime)) {
            showAlert(Alert.AlertType.WARNING, "The cinema opens at 08:00. Cannot add showtime before opening.");
            return;
        }

        if (newEndWithBreak.isAfter(closeTime)) {
            showAlert(Alert.AlertType.WARNING, "Showtime and break must finish before 22:00.");
            return;
        }

        // Kiểm tra xung đột với showtime trong DB
        for (Showtime st : existingShowtimes) {
            LocalTime existingStart = st.getShowTime();
            Movie existingMovie = movieDAO.getMovieById(st.getMovieId());
            if (existingMovie == null) continue;
            LocalTime existingEnd = existingStart.plusMinutes(existingMovie.getDuration());
            existingEnd = existingEnd.plusMinutes(breakTime);

            if (timesOverlap(newTime, newEndTime, existingStart, existingEnd)) {
                showAlert(Alert.AlertType.ERROR,
                        "This time slot " + newTime + " - " + newEndTime + " overlaps with existing showtime " +
                                existingStart + " - " + existingEnd + " (Movie: " + existingMovie.getTitle() + ").");
                return;
            }
        }

        // Kiểm tra xung đột với các slot đã thêm trong phiên làm việc này
        if (!timeSlots.isEmpty()) {
            LocalTime lastStart = timeSlots.get(timeSlots.size() - 1);
            LocalTime earliestNext = lastStart.plusMinutes(duration + breakTime);
            if (newTime.isBefore(earliestNext)) {
                showAlert(Alert.AlertType.WARNING, "Start time must be at or after " + earliestNext + ".");
                return;
            }
        } else {
            LocalTime firstShowtimeAllowed = LocalTime.of(8, 30);
            if (newTime.isBefore(firstShowtimeAllowed)) {
                showAlert(Alert.AlertType.WARNING, "The first showtime must start at 08:30 or later.");
                return;
            }
        }

        if (timeSlots.contains(newTime)) {
            showAlert(Alert.AlertType.WARNING, "This time slot already exists!");
            return;
        }

        // Thêm slot mới
        timeSlots.add(newTime);

        // Xóa toàn bộ slot hiện có trên GridPane trước khi thêm lại
        timeSlotGrid.getChildren().clear();
        int maxPerRow = 5;
        for (int i = 0; i < timeSlots.size(); i++) {
            LocalTime time = timeSlots.get(i);

            Label lbl = new Label(time.toString());
            Button btnRemove = new Button("X");
            btnRemove.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-weight: bold;");
            btnRemove.setOnAction(e -> {
                timeSlots.remove(time);
                refreshGrid();
            });

            HBox slotBox = new HBox(5, lbl, btnRemove);
            int row = i / maxPerRow;
            int col = i % maxPerRow;
            timeSlotGrid.add(slotBox, col, row);
        }

        // Cập nhật spinner cho lần chọn tiếp theo
        LocalTime nextStart = newTime.plusMinutes(duration + breakTime);
        hourSpinner.getValueFactory().setValue(nextStart.getHour());
        minuteSpinner.getValueFactory().setValue(nextStart.getMinute());
    }


    private void refreshGrid() {
        timeSlotGrid.getChildren().clear();
        int maxPerRow = 5;
        for (int i = 0; i < timeSlots.size(); i++) {
            LocalTime time = timeSlots.get(i);

            Label lbl = new Label(time.toString());
            Button btnRemove = new Button("X");
            btnRemove.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-weight: bold;");
            btnRemove.setOnAction(e -> {
                timeSlots.remove(time);
                refreshGrid();
            });

            HBox slotBox = new HBox(5, lbl, btnRemove);
            int row = i / maxPerRow;
            int col = i % maxPerRow;
            timeSlotGrid.add(slotBox, col, row);
        }
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        updateShowtimesByRoomAndDate();
    }

    @FXML
    void handleSelectMovie(ActionEvent event) {
        String selectedMovie = cbMovieList.getValue();
        if (selectedMovie == null) {
            tfDuration.clear();
            return;
        }

        Movie movie = movieDAO.getMovieByTitle(selectedMovie);
        if (movie != null) {
            tfDuration.setText(String.valueOf(movie.getDuration()));
        } else {
            tfDuration.clear();
        }
    }

    @FXML
    void handleSelectRoom(ActionEvent event) {
        updateShowtimesByRoomAndDate();
    }

    @FXML
    void handleDatePicker(ActionEvent event) {
        updateShowtimesByRoomAndDate();
    }

    private void updateShowtimesByRoomAndDate() {
        String selectedRoom = cbRoomList.getValue();
        LocalDate selectedDate = pickerDate.getValue();

        if (selectedRoom == null || selectedDate == null) {
            vboxShowtimeExistingList.getChildren().clear();
            txtMovieExisting.setText("");
            txtTimeExisting.setText("");
            existingShowtimesCache.clear();
            return;
        }

        ScreeningRoom room = roomDAO.getRoomByName(selectedRoom);
        if (room == null) return;

        List<Showtime> showtimes = showtimeDAO.getShowtimesByRoomDate(room.getRoomId(), selectedDate);

        existingShowtimesCache.clear();
        existingShowtimesCache.addAll(showtimes);

        vboxShowtimeExistingList.getChildren().clear();

        if (existingShowtimesCache.isEmpty()) {
            txtTimeExisting.setText("No showtimes available.");
        } else {
            for (Showtime st : existingShowtimesCache) {
                Movie movie = movieDAO.getMovieById(st.getMovieId());
                if (movie == null) continue;

                LocalTime startTime = st.getShowTime();
                LocalTime endTime = startTime.plusMinutes(movie.getDuration());

                Label lbl = new Label(movie.getTitle() + ": " + startTime + " - " + endTime);
                lbl.setPrefWidth(200);

                Button btnRemove = new Button("X");
                btnRemove.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-weight: bold;");
                btnRemove.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirm Delete");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Are you sure you want to delete this showtime?");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            boolean deleted = showtimeDAO.deleteShowtime(st.getShowtimeId());
                            if (deleted) {
                                showAlert(Alert.AlertType.INFORMATION, "Showtime deleted successfully.");
                                updateShowtimesByRoomAndDate();
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Failed to delete showtime.");
                            }
                        }
                    });
                });

                HBox row = new HBox(10, lbl, btnRemove);
                vboxShowtimeExistingList.getChildren().add(row);
            }
            txtTimeExisting.setText("Total showtimes: " + existingShowtimesCache.size());
        }

        txtMovieExisting.setText("Showtimes for room " + selectedRoom + " on " + selectedDate.toString());
    }

    @FXML
    public void handleSaveTimeSlot(ActionEvent event) {
        String selectedMovie = cbMovieList.getValue();
        String selectedRoom = cbRoomList.getValue();
        LocalDate selectedDate = pickerDate.getValue();

        if (selectedMovie == null || selectedRoom == null || selectedDate == null || timeSlots.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Please select movie, room, date and add at least one time slot.");
            return;
        }

        int breakTime;
        try {
            breakTime = Integer.parseInt(tfBreakTime.getText());
        } catch (NumberFormatException e) {
            breakTime = 30;
        }

        int duration;
        try {
            duration = Integer.parseInt(tfDuration.getText());
            if (duration <= 0) {
                showAlert(Alert.AlertType.ERROR, "Duration must be a positive integer.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Duration must be a number.");
            return;
        }

        Movie movie = movieDAO.getMovieByTitle(selectedMovie);
        ScreeningRoom room = roomDAO.getRoomByName(selectedRoom);

        if (movie == null || room == null) {
            showAlert(Alert.AlertType.ERROR, "Movie or room not found.");
            return;
        }

        List<Showtime> allShowtimesInRoom = showtimeDAO.getShowtimesByRoomDate(room.getRoomId(), selectedDate);

        LocalTime openTime = LocalTime.of(8, 0);
        LocalTime closeTime = LocalTime.of(22, 0);

        for (LocalTime newStart : timeSlots) {
            LocalTime newEnd = newStart.plusMinutes(duration);
            LocalTime endWithBreak = newEnd.plusMinutes(breakTime);

            // Kiểm tra giờ mở cửa và đóng cửa
            if (newStart.isBefore(openTime)) {
                showAlert(Alert.AlertType.WARNING, "Showtime cannot start before 08:00.");
                return;
            }

            if (endWithBreak.isAfter(closeTime)) {
                showAlert(Alert.AlertType.WARNING,
                        "Showtime starting at " + newStart + " must finish before 22:00 including break.");
                return;
            }

            // Kiểm tra xung đột với showtime đã có
            for (Showtime existing : allShowtimesInRoom) {
                LocalTime existingStart = existing.getShowTime();
                LocalTime existingEnd = existing.getEndTime();  // thường chưa cộng break time

                if (timesOverlap(newStart, newEnd, existingStart, existingEnd)) {
                    showAlert(Alert.AlertType.ERROR,
                            "Time slot " + newStart + " - " + newEnd + " overlaps with existing showtime " +
                                    existingStart + " - " + existingEnd + " (Movie: " +
                                    movieDAO.getMovieById(existing.getMovieId()).getTitle() + ").");
                    return;
                }
            }
        }

        // Nếu không trùng và hợp lệ thì lưu vào DB
        List<Showtime> showtimesToAdd = new ArrayList<>();
        for (LocalTime startTime : timeSlots) {
            LocalTime endTime = startTime.plusMinutes(duration);
            Showtime st = new Showtime();
            st.setMovieId(movie.getMovieId());
            st.setRoomId(room.getRoomId());
            st.setShowDate(selectedDate);
            st.setShowTime(startTime);
            st.setEndTime(endTime);
            showtimesToAdd.add(st);
        }

        boolean success = showtimeDAO.insertShowtimes(showtimesToAdd);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Showtimes added successfully.");
            timeSlots.clear();
            updateShowtimesByRoomAndDate();
        } else {
            showAlert(Alert.AlertType.ERROR, "Failed to add showtimes.");
        }
    }

    private boolean timesOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return !(end1.compareTo(start2) <= 0 || end2.compareTo(start1) <= 0);
    }

    @FXML
    void handleCancel(ActionEvent event) {
        boolean hasInput = !timeSlots.isEmpty()
                || cbMovieList.getValue() != null
                || cbRoomList.getValue() != null
                || (pickerDate.getValue() != null && !pickerDate.getValue().equals(LocalDate.now()))
                || !tfDuration.getText().isEmpty();

        if (hasInput) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Exit");
            confirm.setHeaderText(null);
            confirm.setContentText("You have unsaved data. Do you want to exit?");
            ButtonType yesBtn = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType noBtn = new ButtonType("No", ButtonBar.ButtonData.NO);
            confirm.getButtonTypes().setAll(yesBtn, noBtn);

            confirm.showAndWait().ifPresent(response -> {
                if (response == yesBtn) {
                    closeStage();
                }
            });
        } else {
            closeStage();
        }
    }

    @FXML
    public void handleInsert(ActionEvent event) {
        if (!timeSlots.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Please save the slots before inserting.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Insert");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to insert showtimes with current data?");

        ButtonType yesBtn = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noBtn = new ButtonType("No", ButtonBar.ButtonData.NO);
        confirm.getButtonTypes().setAll(yesBtn, noBtn);

        confirm.showAndWait().ifPresent(response -> {
            if (response == yesBtn) {
                closeStage();
            }
        });
    }

    private void closeStage() {
        Stage stage = (Stage) btnInsert.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
