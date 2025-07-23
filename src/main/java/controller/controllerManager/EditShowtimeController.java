package controller.controllerManager;

import dao.ShowtimeDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Showtime;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditShowtimeController {

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnLoadSt;

    @FXML
    private Button btnUpdate;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> movieComboBox;

    @FXML
    private ComboBox<String> roomComboBox;

    @FXML
    private VBox timeSlotList;

    private List<Showtime> loadedShowtimes = new ArrayList<>();
    private Showtime selectedShowtime = null;

    @FXML
    public void initialize() {
        // Mock movie/room list
        movieComboBox.getItems().addAll("Oppenheimer", "Barbie");
        roomComboBox.getItems().addAll("Room A", "Room B");

        movieComboBox.setValue("Oppenheimer");
        roomComboBox.setValue("Room A");
    }

    @FXML
    void handleLoadShowtime(ActionEvent event) {
        String selectedMovie = movieComboBox.getValue();
        String selectedRoom = roomComboBox.getValue();
        LocalDate selectedDate = datePicker.getValue();

        if (selectedMovie == null || selectedRoom == null || selectedDate == null) {
            showAlert("Missing Input", "Please choose movie, room and date.");
            return;
        }

        String dateStr = selectedDate.toString();

        // Filter by movieTitle, roomName and date
        loadedShowtimes = ShowtimeDAO.getAllShowtimes().stream()
                .filter(s -> s.getMovieTitle().equals(selectedMovie) &&
                        s.getRoomName().equals(selectedRoom) &&
                        s.getShowDate().equals(dateStr))
                .toList();

        displayTimeSlots();
    }

    private void displayTimeSlots() {
        timeSlotList.getChildren().clear();

        for (Showtime st : loadedShowtimes) {
            Label timeLabel = new Label(st.getShowTime());
            Button selectBtn = new Button("Select");
            selectBtn.setOnAction(e -> {
                selectedShowtime = st;
                highlightSelection(st);
            });

            HBox box = new HBox(10, timeLabel, selectBtn);
            timeSlotList.getChildren().add(box);
        }
    }

    private void highlightSelection(Showtime st) {
        for (var node : timeSlotList.getChildren()) {
            if (node instanceof HBox hbox && hbox.getChildren().get(0) instanceof Label lbl) {
                if (lbl.getText().equals(st.getShowTime())) {
                    lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #AA0000;");
                } else {
                    lbl.setStyle("");
                }
            }
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        if (selectedShowtime == null) {
            showAlert("No selection", "Please select a showtime to delete.");
            return;
        }

        boolean deleted = ShowtimeDAO.deleteShowtime(selectedShowtime.getShowtimeId());

        if (deleted) {
            showAlert("Deleted", "Showtime deleted successfully.");
            loadedShowtimes.remove(selectedShowtime);
            selectedShowtime = null;
            displayTimeSlots();
        } else {
            showAlert("Error", "Failed to delete showtime.");
        }
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        if (selectedShowtime == null) {
            showAlert("No selection", "Please select a showtime to update.");
            return;
        }

        // Here you could open a new dialog or inline edit
        selectedShowtime.setShowTime("21:00"); // example update
        boolean updated = ShowtimeDAO.updateShowtime(selectedShowtime);

        if (updated) {
            showAlert("Updated", "Showtime updated to 21:00.");
            displayTimeSlots();
        } else {
            showAlert("Error", "Failed to update showtime.");
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setShowtime(Showtime selected) {
    }

    public void handleAddTimeSlot(ActionEvent actionEvent) {
    }

    @FXML
    private VBox showtimeContainer;

    public void loadShowtimes(Map<LocalDate, List<String>> showtimesByDate) {
        showtimeContainer.getChildren().clear();

        for (Map.Entry<LocalDate, List<String>> entry : showtimesByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<String> times = entry.getValue();

            Label dateLabel = new Label();
            if (date.equals(LocalDate.now())) {
                dateLabel.setText("Today - " + date.format(DateTimeFormatter.ofPattern("MMMM dd")));
            } else if (date.equals(LocalDate.now().plusDays(1))) {
                dateLabel.setText("Tomorrow - " + date.format(DateTimeFormatter.ofPattern("MMMM dd")));
            } else {
                dateLabel.setText(date.format(DateTimeFormatter.ofPattern("EEEE - MMM dd")));
            }
            dateLabel.getStyleClass().add("showtime-day-label");

            FlowPane timePane = new FlowPane();
            timePane.setHgap(10);
            timePane.setVgap(10);

            for (String time : times) {
                Button btn = new Button(time);
                btn.getStyleClass().add("showtime-button");
                btn.setOnAction(e -> System.out.println("Clicked time: " + time));
                timePane.getChildren().add(btn);
            }

            VBox section = new VBox(5, dateLabel, timePane);
            showtimeContainer.getChildren().add(section);
        }
    }

}
