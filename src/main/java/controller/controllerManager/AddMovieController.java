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

    private File selectedFile;  // Stores the chosen image file
    private MovieListController movieListController;  // Reference to main movie list controller (for updating list)

    @FXML
    public void initialize() {
        // Initialize combo boxes with preset options
        // Movie genres
        cbGenre.getItems().addAll(
                "Action", "Drama", "Comedy", "Horror", "Romance",
                "Thriller", "Sci-Fi", "Fantasy", "Animation", "Documentary",
                "Adventure", "Crime", "Mystery", "Family", "Musical"
        );

        // Languages
        cbLanguage.getItems().addAll(
                "English", "Vietnamese", "Korean", "Japanese",
                "Chinese", "French", "Thai", "Hindi"
        );

        // Age ratings
        cbAgeRating.getItems().addAll(
                "G",        // General audiences
                "PG",       // Parental guidance suggested
                "PG-13",    // Parents strongly cautioned
                "R",        // Restricted
                "NC-17",    // Adults only
                "C18"       // 18+ (common in CGV Vietnam)
        );

    }

    /**
     * Opens a file chooser dialog to select an image file for the movie poster.
     * Shows a preview of the selected image.
     */
    @FXML
    private void handleChooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose image file");

        // Filter to only show image file types
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

    /**
     * Handles the insert button click event.
     * Validates inputs, copies the poster image to the app directory,
     * creates a Movie object, inserts it into the database,
     * shows success/failure alerts, and closes the form.
     */
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

        // Check required fields are not empty or null
        if (title.isEmpty() || genre == null || durationStr.isEmpty() || language == null ||
                ageRating == null || releaseDate == null || description.isEmpty() ||
                directedBy == null || directedBy.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Please fill in all required fields.");
            return;
        }

        try {
            int duration = Integer.parseInt(durationStr);

            // Validate duration is positive
            if (duration <= 0) {
                showAlert(Alert.AlertType.ERROR, "Invalid Duration", "Duration must be a number greater than 0.");
                return;
            }

            // Copy selected image file to app's images folder if a file was chosen
            if (selectedFile != null) {
                File destDir = new File("images");
                if (!destDir.exists()) destDir.mkdirs();

                File destFile = new File(destDir, selectedFile.getName());
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // Create new Movie object with collected data
            Movie movie = new Movie(
                    0, title, duration, genre, description,
                    directedBy, language, poster, ageRating,
                    LocalDateTime.now(), releaseDate
            );

            // Insert the movie into the database via DAO
            boolean inserted = MovieDAO.insertMovie(movie);
            if (inserted) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Movie added successfully.");

                // Optionally refresh the movie list if the controller reference is set
                if (movieListController != null) {
                    // e.g. movieListController.loadMovies();
                }

                clearForm();

                // Close the current form window
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

    /**
     * Clears all input fields and resets image preview.
     */
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

    /**
     * Handles the cancel button event, closes the form window.
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    /**
     * Utility method to show an alert dialog.
     * @param type AlertType of the alert (e.g. INFORMATION, ERROR)
     * @param title Title of the alert dialog
     * @param message Message content of the alert
     */
    public void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Returns the filename of the selected image file or null if none selected.
     * @return selected image file name or null
     */
    public String getSelectedImageFileName() {
        return selectedFile != null ? selectedFile.getName() : null;
    }

    /**
     * Sets a reference to the MovieListController to allow interaction (e.g. refreshing list).
     * @param movieListController the movie list controller instance
     */
    public void setMovieListController(MovieListController movieListController) {
        this.movieListController = movieListController;
    }
}
