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
import configs.DBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

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

        loadShowtimesFromDatabase(movie);
    }

    private void loadShowtimesFromDatabase(Movie movie) {
        showtimePane.getChildren().clear();
        confirmButton.setDisable(true);

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT showtimeId, roomId, showDate, showTime, endTime FROM showtimes WHERE movieId = ? AND showDate = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, movie.getMovieId());
            stmt.setString(2, LocalDate.now().toString()); // Lấy showtime của ngày hiện tại
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int showtimeId = rs.getInt("showtimeId");
                int roomId = rs.getInt("roomId");
                String showDate = rs.getString("showDate");
                String showTime = rs.getString("showTime").substring(0, 8);
                String endTime = rs.getString("endTime").substring(0, 8);

                String label = showTime + " - " + endTime;

                Button btn = new Button(label);
                btn.setOnAction(e -> {
                    showtimePane.getChildren().forEach(node -> node.setStyle(""));
                    btn.setStyle("-fx-background-color: #66ccff; -fx-text-fill: white;");

                    selectedShowtime = new Showtime();
                    selectedShowtime.setShowtimeId(showtimeId);
                    selectedShowtime.setMovieId(movie.getMovieId());
                    selectedShowtime.setMovieTitle(movie.getTitle());
                    selectedShowtime.setShowDate(showDate);
                    selectedShowtime.setShowTime(showTime);
                    selectedShowtime.setEndTime(endTime);
                    selectedShowtime.setRoomId(roomId);
                    selectedShowtime.setRoomName("Room " + roomId);

                    confirmButton.setDisable(false);
                    System.out.println("Đã chọn showtimeId: " + showtimeId);
                });

                btn.setPrefWidth(140);
                showtimePane.getChildren().add(btn);
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tải showtimes từ CSDL: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}