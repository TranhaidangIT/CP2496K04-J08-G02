package controller.controllerManager;

import dao.ShowtimeDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import models.Showtime;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ShowtimeItemController {

    @FXML private GridPane gridShowtime;
    @FXML private Label lblDuration;
    @FXML private Label lblMovieTitle;
    @FXML private Label lblReleaseDate;
    @FXML private Label lblRoomNumber;
    @FXML private ImageView posterImage;
    @FXML private Pane showTimeList;
    @FXML private Button btnEdit;


    private ShowtimeDAO showtimeDAO = new ShowtimeDAO();

    private Showtime mainShowtime;
    private List<Showtime> showtimeGroup;

    // Gán thông tin từng block item showtime
    public void setShowtimeItem(Showtime firstShowtime, List<Showtime> timeSlots) {
        this.mainShowtime = firstShowtime;
        this.showtimeGroup = timeSlots;

        lblMovieTitle.setText(firstShowtime.getMovieTitle());
        lblRoomNumber.setText(firstShowtime.getRoomName());
        lblReleaseDate.setText(firstShowtime.getShowDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        lblDuration.setText(firstShowtime.getShowTime() + " ~ " + firstShowtime.getEndTime());

        try {
            String imageFileName = firstShowtime.getMovieTitle().replaceAll(" ", "_") + ".jpg";
            File imageFile = new File("images/" + imageFileName);

            if (imageFile.exists()) {
                Image poster = new Image(imageFile.toURI().toString());
                posterImage.setImage(poster);
            } else {
                posterImage.setImage(new Image(getClass().getResourceAsStream("/images/default-poster.png")));
            }
        } catch (Exception e) {
            System.out.println("Failed to load poster for: " + firstShowtime.getMovieTitle());
        }


        showTimeList.getChildren().clear();
        GridPane timeGrid = new GridPane();
        timeGrid.setHgap(10);
        timeGrid.setVgap(10);
        timeGrid.setPadding(new Insets(10));

        int col = 0;
        int row = 0;
        int maxCols = 5;

        for (Showtime s : timeSlots) {
            Label timeLabel = new Label(s.getShowTime().toString());

            // Correct way to add a CSS class
            timeLabel.getStyleClass().add("time-ticket");

            timeGrid.add(timeLabel, col, row);
            col++;
            if (col >= maxCols) {
                col = 0;
                row++;
            }
        }

        showTimeList.getChildren().add(timeGrid);
    }


    @FXML
    private void handleEditClickShowtime() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Manager/Showtime/EditShowtime.fxml"));
            Pane editPane = loader.load();

            EditShowtimeController controller = loader.getController();
            controller.setShowtimeToEdit(mainShowtime, showtimeGroup); // Gửi dữ liệu sang controller edit

            Stage stage = new Stage();
            stage.setScene(new Scene(editPane));
            stage.setTitle("Edit Showtime");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void handleClickShowtimeItem(MouseEvent mouseEvent) {
    }

    @FXML
    public void handleDeleteClickShowtime(ActionEvent event) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Delete showtime for " + mainShowtime.getMovieTitle());
        alert.setContentText("Are you sure you want to delete all the selected showtimes?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {

                for (Showtime s : showtimeGroup) {
                    showtimeDAO.deleteShowtimeById(s.getShowtimeId());
                }
            }
        });
    }

}
