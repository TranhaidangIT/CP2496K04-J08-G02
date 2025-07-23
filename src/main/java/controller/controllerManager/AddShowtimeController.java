package controller.controllerManager;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
    private VBox timeSlotList;

    private List<LocalTime> addedTimeSlots = new ArrayList<>();

    @FXML
    public void initialize() {
        // Initialize spinners
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 10));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 5));

        // Format spinner with 2-digit display
        hourSpinner.getEditor().setTextFormatter(createTwoDigitFormatter());
        minuteSpinner.getEditor().setTextFormatter(createTwoDigitFormatter());

        // Sample data - replace with DAO calls if needed
        movieComboBox.setItems(FXCollections.observableArrayList("Movie A", "Movie B", "Movie C"));
        roomComboBox.setItems(FXCollections.observableArrayList("Room 1", "Room 2", "Room 3"));
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

        Label timeLabel = new Label(time.toString());
        Button removeButton = new Button("X");
        removeButton.setOnAction(e -> {
            timeSlotList.getChildren().removeIf(node -> ((HBox) node).getChildren().contains(timeLabel));
            addedTimeSlots.remove(time);
        });

        HBox hBox = new HBox(10, timeLabel, removeButton);
        timeSlotList.getChildren().add(hBox);
    }

    @FXML
    void handleInsert(ActionEvent event) {
        String selectedMovie = movieComboBox.getValue();
        String selectedRoom = roomComboBox.getValue();

        if (selectedMovie == null || selectedRoom == null || datePicker.getValue() == null || addedTimeSlots.isEmpty()) {
            showAlert("Incomplete Input", "Please fill in all fields and add at least one time slot.");
            return;
        }

        // Sample printout - replace with actual insert logic
        System.out.println("Inserting showtimes for:");
        System.out.println("Movie: " + selectedMovie);
        System.out.println("Room: " + selectedRoom);
        System.out.println("Date: " + datePicker.getValue());
        for (LocalTime time : addedTimeSlots) {
            System.out.println(" - " + time);
        }

        // You can clear the form or close the window here
    }

    @FXML
    void handleCancel(ActionEvent event) {
        // Close current window
        Button source = (Button) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
