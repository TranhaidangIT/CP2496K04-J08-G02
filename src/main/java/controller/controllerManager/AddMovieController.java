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
import java.time.LocalDateTime;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;


public class AddMovieController {

    @FXML private Button btnChooseFile;
    @FXML private ImageView imgPreview;
    @FXML private TextField txtTitle;
    @FXML private TextField txtDuration;
    @FXML private ComboBox<String> cbGenre;
    @FXML private ComboBox<String> cbLanguage;
    @FXML private ComboBox<String> cbAgeRating;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descriptionTextArea;
    @FXML private TextField txtDirectedby;
    @FXML private Button btnInsert;
    @FXML private Button btnCancel;

    private File selectedFile;
    private MovieListController movieListController;


    @FXML
    public void initialize() {
        cbGenre.getItems().addAll("Action", "Drama", "Comedy", "Horror", "Romance");
        cbLanguage.getItems().addAll("English", "Vietnamese", "Korean", "Japanese");
        cbAgeRating.getItems().addAll("G", "PG", "PG-13", "R", "NC-17");
    }

    @FXML
    private void handleChooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose image file");

        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"
        );
        fileChooser.getExtensionFilters().add(imageFilter);

        File file = fileChooser.showOpenDialog(btnChooseFile.getScene().getWindow());

        if (file != null) {
            selectedFile = file;
            Image image = new Image(file.toURI().toString());
            imgPreview.setImage(image);
            imgPreview.setFitWidth(150);
            imgPreview.setPreserveRatio(true);
        }
    }

    @FXML
    private void handleInsert(ActionEvent event) {
        String title = txtTitle.getText();
        String directedBy = txtDirectedby.getText();
        String genre = cbGenre.getValue();
        String durationStr = txtDuration.getText();
        String language = cbLanguage.getValue();
        String ageRating = cbAgeRating.getValue();
        LocalDate releaseDate = datePicker.getValue();
        String description = descriptionTextArea.getText();
        String poster = getSelectedImageFileName();

        if (title.isEmpty() || genre == null || durationStr.isEmpty() || language == null ||
                ageRating == null || releaseDate == null || description.isEmpty() ||
                directedBy == null || directedBy.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Please fill in all required fields.");
            return;
        }

        try {
            int duration = Integer.parseInt(durationStr);
            if (duration <= 0) {
                showAlert(Alert.AlertType.ERROR, "Invalid Duration", "Duration must be a number greater than 0.");
                return;
            }

            if (selectedFile != null) {
                File destDir = new File("images");
                if (!destDir.exists()) destDir.mkdirs();

                File destFile = new File(destDir, selectedFile.getName());
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            Movie movie = new Movie(
                    0, title, duration, genre, description,
                    directedBy, language, poster, ageRating,
                    LocalDateTime.now(), releaseDate
            );

            boolean inserted = MovieDAO.insertMovie(movie);
            if (inserted) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Movie added successfully.");

                if (movieListController != null) {

                }

                clearForm();
                Stage stage = (Stage) btnInsert.getScene().getWindow();
                stage.close();

            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add movie to the database.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Duration must be a valid number.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "File Error", "Could not copy image file.");
        }
    }


    private void clearForm() {
        txtTitle.clear();
        txtDirectedby.clear();
        cbGenre.setValue(null);
        txtDuration.clear();
        cbLanguage.setValue(null);
        cbAgeRating.setValue(null);
        datePicker.setValue(null);
        descriptionTextArea.clear();
        imgPreview.setImage(null);
        selectedFile = null;
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    public void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    public String getSelectedImageFileName() {
        return selectedFile != null ? selectedFile.getName() : null;
    }

    public void setMovieListController(MovieListController movieListController) {
        this.movieListController = movieListController;
    }
}
