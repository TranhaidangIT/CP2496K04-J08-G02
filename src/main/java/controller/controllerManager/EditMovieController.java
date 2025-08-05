package controller.controllerManager;

import dao.MovieDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Movie;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;

public class EditMovieController {

    @FXML private Button btnCancel, btnDelete, btnUpdate, btnChooseFile;
    @FXML private ImageView imgPreview;
    @FXML private TextField txtTitle, txtDirectedby, txtDuration;
    @FXML private ComboBox<String> cbGenre, cbLanguage, cbAgeRating;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descriptionTextArea;

    private Movie movie;
    private File selectedFile;
    private MovieListController movieListController;

    public void setMovieListController(MovieListController controller) {
        this.movieListController = controller;
    }

    public void setMovieData(Movie movie) {
        this.movie = movie;

        txtTitle.setText(movie.getTitle());
        txtDirectedby.setText(movie.getDirectedBy());
        txtDuration.setText(String.valueOf(movie.getDuration()));
        cbGenre.setValue(movie.getGenre());
        cbLanguage.setValue(movie.getLanguage());
        cbAgeRating.setValue(movie.getAgeRating());
        datePicker.setValue(movie.getReleasedDate());
        descriptionTextArea.setText(movie.getDescription());

        if (movie.getPoster() != null && !movie.getPoster().isEmpty()) {
            imgPreview.setImage(new Image("file:images/" + movie.getPoster()));
        }
    }

    @FXML
    public void initialize() {
        cbGenre.getItems().addAll("Action", "Drama", "Comedy", "Horror", "Romance");
        cbLanguage.getItems().addAll("English", "Vietnamese", "Korean", "Japanese");
        cbAgeRating.getItems().addAll("G", "PG", "PG-13", "R", "NC-17");
    }

    @FXML
    void handleChooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Movie Poster");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = fileChooser.showOpenDialog(btnChooseFile.getScene().getWindow());

        if (file != null) {
            selectedFile = file;
            imgPreview.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        String title = txtTitle.getText();
        String directedBy = txtDirectedby.getText();
        String genre = cbGenre.getValue();
        String durationStr = txtDuration.getText();
        String language = cbLanguage.getValue();
        String ageRating = cbAgeRating.getValue();
        LocalDate releaseDate = datePicker.getValue();
        String description = descriptionTextArea.getText();
        String poster = selectedFile != null ? selectedFile.getName() : movie.getPoster();

        if (title.isEmpty() || genre == null || durationStr.isEmpty() || language == null ||
                ageRating == null || releaseDate == null || description.isEmpty() || directedBy.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all fields.");
            return;
        }

        try {
            int duration = Integer.parseInt(durationStr);

            if (!MovieDAO.isMovieIdExists(movie.getMovieId())) {
                showAlert(Alert.AlertType.ERROR, "Error", "Movie ID không tồn tại trong database.");
                return;
            }

            if (selectedFile != null) {
                savePosterImage(selectedFile);
            }

            movie.setTitle(title);
            movie.setDirectedBy(directedBy);
            movie.setGenre(genre);
            movie.setDuration(duration);
            movie.setLanguage(language);
            movie.setAgeRating(ageRating);
            movie.setReleasedDate(releaseDate);
            movie.setDescription(description);
            movie.setPoster(poster);

            if (MovieDAO.updateMovie(movie)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Movie updated successfully.");
                movieListController.loadMovieList();
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update movie.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Duration must be a number.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "File Error", "Error saving the poster image.");
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeWindow();
    }

    @FXML
    void handleDelete(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Are you sure you want to delete this movie?");
        confirmAlert.setContentText("This action cannot be undone.");

        ButtonType btnYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType btnNo = new ButtonType("No", ButtonBar.ButtonData.NO);

        confirmAlert.getButtonTypes().setAll(btnYes, btnNo);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == btnYes) {
                boolean success = MovieDAO.deleteMovieById(movie.getMovieId());
                if (success) {
                    movieListController.loadMovieList();
                    showAlert(Alert.AlertType.INFORMATION, "Deleted", "Movie deleted successfully.");
                    closeWindow();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Failed", "Failed to delete movie.");
                }
            }
        });
    }

    private void savePosterImage(File sourceFile) throws IOException {
        File destDir = new File("images");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        File destFile = new File(destDir, sourceFile.getName());
        Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}
