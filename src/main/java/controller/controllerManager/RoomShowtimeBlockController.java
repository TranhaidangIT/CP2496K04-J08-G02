package controller.controllerManager;

import dao.ShowtimeDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
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

public class RoomShowtimeBlockController {

    @FXML
    private Label lblRoomNumber;

    @FXML
    private VBox vboxListShowtime;

    @FXML
    private Button btnEditShowtime;

    @FXML
    private Button btnDeleteShowtime;

    private ScreeningRoom currentRoom;
    private LocalDate currentDate;

    /**
     * Sets the screening room and its showtimes for this block.
     * Grouped by movie title and date to create multiple MovieShowtimeItem instances.
     */
    public void setRoomAndShowtimes(ScreeningRoom room, List<Showtime> showtimes) {
        this.currentRoom = room;

        lblRoomNumber.setText(room.getRoomNumber());
        vboxListShowtime.getChildren().clear();

        if (showtimes.isEmpty()) return;

        // Group showtimes first by movie, then by date
        Map<String, Map<LocalDate, List<Showtime>>> groupedByMovieAndDate = showtimes.stream()
                .collect(Collectors.groupingBy(
                        Showtime::getMovieTitle,
                        Collectors.groupingBy(Showtime::getShowDate)
                ));

        // Store the earliest date found
        currentDate = showtimes.stream()
                .map(Showtime::getShowDate)
                .sorted()
                .findFirst()
                .orElse(null);

        // Create UI blocks for each (movie + date) group
        groupedByMovieAndDate.forEach((movieTitle, dateMap) -> {
            dateMap.forEach((date, stList) -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Manager/Showtime/MovieShowtimeItem.fxml"));
                    Parent node = loader.load();

                    MovieShowtimeItemController controller = loader.getController();
                    // Pass the first showtime as reference plus the full list for that movie-date group
                    controller.setData(stList.get(0), stList);

                    vboxListShowtime.getChildren().add(node);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    /**
     * Opens the edit dialog for the current room's showtimes on currentDate.
     */
    @FXML
    private void handleEditClickShowtime() {
        if (currentRoom == null || currentDate == null) {
            showAlert(Alert.AlertType.WARNING, "No room or date data available to edit.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Manager/Showtime/EditShowtime.fxml"));
            Parent root = loader.load();

            EditShowtimeController controller = loader.getController();
            // Pass room ID and date; the controller will load all showtimes for that room on that date
            controller.loadShowtimeToEdit(currentRoom.getRoomId(), currentDate);

            Stage stage = new Stage();
            stage.setTitle("Edit showtimes for room " + currentRoom.getRoomNumber() + " on " + currentDate);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh showtime list after editing
            reloadShowtimes();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error opening edit showtime window.");
        }
    }

    /**
     * Deletes all showtimes for this room on the currentDate.
     */
    @FXML
    public void handleDeleteClickShowtime(ActionEvent actionEvent) {
        if (currentRoom == null || currentDate == null) {
            showAlert(Alert.AlertType.WARNING, "No room or date data available to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm deletion");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete all showtimes for room "
                + currentRoom.getRoomNumber() + " on " + currentDate + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ShowtimeDAO showtimeDAO = new ShowtimeDAO();
                boolean success = showtimeDAO.deleteShowtimesByRoomAndDate(currentRoom.getRoomId(), currentDate);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "All showtimes deleted successfully.");
                    vboxListShowtime.getChildren().clear();
                    currentDate = null;
                } else {
                    showAlert(Alert.AlertType.ERROR, "Failed to delete showtimes.");
                }
            }
        });
    }

    /**
     * Reloads the showtimes list for the current room and date.
     * Can be called after editing or adding showtimes.
     */
    private void reloadShowtimes() {
        if (currentRoom == null || currentDate == null) return;

        ShowtimeDAO showtimeDAO = new ShowtimeDAO();
        List<Showtime> showtimes = showtimeDAO.getShowtimesByRoomDate(currentRoom.getRoomId(), currentDate);

        setRoomAndShowtimes(currentRoom, showtimes);
    }

    /**
     * Shows a simple alert dialog with the given message.
     */
    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
