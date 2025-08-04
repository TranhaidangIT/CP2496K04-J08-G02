package controller.controllerEmployees;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import models.Movie;
import models.Showtime;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

public class MovieDetailController {

    @FXML private Label titleLabel, genreLabel, durationLabel, languageLabel, ageRatingLabel, releaseDateLabel, directedByLabel;
    @FXML private TextArea descriptionArea;
    @FXML private ImageView posterImage;
    @FXML private FlowPane showtimePane;
    @FXML private Button confirmButton;
    @FXML private Button exitButton;
    @FXML private Label roomLabel;
    private Showtime selectedShowtime;

    @FXML
    public void initialize() {
        confirmButton.setOnAction(e -> handleConfirm());
        exitButton.setOnAction(e -> handleExit());
    }

    @FXML
    private void handleConfirm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/SeatSelection.fxml"));
            Parent seatSelectionRoot = loader.load();
            SeatSelectionController controller = loader.getController();
            if (selectedShowtime == null) {
                System.err.println("Lỗi: selectedShowtime là null!");
                return;
            }
            controller.setData(selectedShowtime);
            AnchorPane parent = (AnchorPane) confirmButton.getScene().lookup("#contentArea");
            if (parent != null) {
                parent.getChildren().setAll(seatSelectionRoot);
                AnchorPane.setTopAnchor(seatSelectionRoot, 0.0);
                AnchorPane.setBottomAnchor(seatSelectionRoot, 0.0);
                AnchorPane.setLeftAnchor(seatSelectionRoot, 0.0);
                AnchorPane.setRightAnchor(seatSelectionRoot, 0.0);
            } else {
                System.err.println("Không tìm thấy #contentArea trong scene!");
            }
        } catch (IOException ex) {
            System.err.println("Lỗi khi tải SeatSelection.fxml: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleExit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/ListMovies.fxml"));
            Parent listMoviesRoot = loader.load();
            ListMoviesController controller = loader.getController();
            controller.setContentArea((AnchorPane) exitButton.getScene().lookup("#contentArea"));
            AnchorPane parent = (AnchorPane) exitButton.getScene().lookup("#contentArea");
            if (parent != null) {
                parent.getChildren().setAll(listMoviesRoot);
                AnchorPane.setTopAnchor(listMoviesRoot, 0.0);
                AnchorPane.setBottomAnchor(listMoviesRoot, 0.0);
                AnchorPane.setLeftAnchor(listMoviesRoot, 0.0);
                AnchorPane.setRightAnchor(listMoviesRoot, 0.0);
            } else {
                System.err.println("Không tìm thấy #contentArea trong scene!");
            }
        } catch (IOException ex) {
            System.err.println("Lỗi khi tải ListMovies.fxml: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void setMovie(Movie movie) {
        titleLabel.setText(movie.getTitle());
        genreLabel.setText("Genre: " + movie.getGenre());
        durationLabel.setText("Duration: " + movie.getDuration() + " min");
        languageLabel.setText("Language: " + movie.getLanguage());
        ageRatingLabel.setText("Age Rating: " + movie.getAgeRating());
        releaseDateLabel.setText("Release: " + movie.getReleasedDate().toString());
        directedByLabel.setText("Director: " + movie.getDirectedBy());
        descriptionArea.setText(movie.getDescription());

        String posterFile = movie.getPoster();
        var stream = getClass().getResourceAsStream("/images/" + posterFile);

        if (stream != null) {
            Image img = new Image(stream);
            posterImage.setImage(img);
        } else {
            System.err.println("Không tìm thấy ảnh: " + posterFile);
        }

        generateMockShowtimes(movie);
    }

    private void generateMockShowtimes(Movie movie) {
        showtimePane.getChildren().clear();
        confirmButton.setDisable(true);

        LocalTime firstSlot = LocalTime.of(8, 0);
        LocalTime lastSlot = LocalTime.of(22, 0);
        Duration duration = Duration.ofMinutes(movie.getDuration());

        int room = movie.getMovieId() % 10 + 1;
        roomLabel.setText("Room " + room);

        LocalTime current = firstSlot;
        while (true) {
            LocalTime rawEnd = current.plus(duration);
            LocalTime roundedEnd = roundUpToNearestQuarterHour(rawEnd);
            if (roundedEnd.isAfter(lastSlot)) break;

            String label = current + " - " + roundedEnd;

            LocalTime finalCurrent = current;
            LocalTime finalEnd = roundedEnd;
            int finalRoom = room;

            Button btn = new Button(label);
            btn.setOnAction(e -> {
                showtimePane.getChildren().forEach(node -> node.setStyle(""));
                btn.setStyle("-fx-background-color: #66ccff; -fx-text-fill: white;");

                selectedShowtime = new Showtime();
                selectedShowtime.setMovieId(movie.getMovieId());
                selectedShowtime.setMovieTitle(movie.getTitle());
                selectedShowtime.setShowDate(LocalDate.now().toString());
                selectedShowtime.setShowTime(finalCurrent.toString());
                selectedShowtime.setEndTime(finalEnd.toString());
                selectedShowtime.setRoomId(finalRoom);
                selectedShowtime.setRoomName("Room " + finalRoom);

                confirmButton.setDisable(false);
            });

            btn.setPrefWidth(140);
            showtimePane.getChildren().add(btn);
            current = roundedEnd.plusMinutes(15);
        }
    }

    private LocalTime roundUpToNearestQuarterHour(LocalTime time) {
        int minute = time.getMinute();
        int add = 0;

        if (minute > 0 && minute <= 15) {
            add = 15 - minute;
        } else if (minute > 15 && minute <= 30) {
            add = 30 - minute;
        } else if (minute > 30 && minute <= 45) {
            add = 45 - minute;
        } else if (minute > 45) {
            add = 60 - minute;
        }
        return time.plusMinutes(add);
    }
}