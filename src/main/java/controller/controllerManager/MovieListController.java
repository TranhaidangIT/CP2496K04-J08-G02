package controller.controllerManager;

import dao.MovieDAO;
import dao.RoomTypeDAO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Movie;
import models.ScreeningRoom;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MovieListController {

    public TextField txtDirectedby;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private TextField tfFind;
    @FXML private AnchorPane tfFindMovie;

    @FXML private ComboBox<String> cbAgeRating;
    @FXML private ComboBox<String> cbGenre;
    @FXML private ComboBox<String> cbLanguage;
    @FXML private DatePicker datePicker;

    @FXML private TableView<Movie> tblMovies;
    @FXML private TableColumn<Movie, Integer> colId;
    @FXML private TableColumn<Movie, String> colTitle;
    @FXML private TableColumn<Movie, String> colGenre;
    @FXML private TableColumn<Movie, Integer> colDuration;
    @FXML private TableColumn<Movie, Object> colReleaseDate;

    @FXML private TextField txtTitle;
    @FXML private TextField txtDuration;
    @FXML private TextArea txtDescription;
    @FXML private ImageView posterImage;


    private ObservableList<Movie> movieList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Initialize ComboBoxes
        cbGenre.setItems(FXCollections.observableArrayList(
                "Action", "Adventure", "Comedy", "Drama", "Horror", "Sci-Fi"
        ));
        cbLanguage.setItems(FXCollections.observableArrayList(
                "English", "Vietnamese", "Japanese", "Korean", "French"
        ));
        cbAgeRating.setItems(FXCollections.observableArrayList(
                "G", "PG", "PG-13", "R", "NC-17"
        ));


        colId.setCellValueFactory(new PropertyValueFactory<>("movieId"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colReleaseDate.setCellValueFactory(new PropertyValueFactory<>("releasedDate"));

        // 3. Load movie data
        loadMovieList();
    }

    public void loadMovieList() {
        List<Movie> movies = MovieDAO.getAllMovies();
        System.out.println("DEBUG: Loaded movies count = " + movies.size());
        movieList.setAll(movies);
        tblMovies.setItems(movieList);
    }

    @FXML
    void handleAddNewMovie(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/fxml_Manager/Movie/AddMovie.fxml")
            );
            Parent root = loader.load();

            AddMovieController addCtrl = loader.getController();
            addCtrl.setMovieListController(this);

            Stage stage = new Stage();
            stage.setTitle("Add New Movie");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadMovieList();

        } catch (IOException e) {
            System.out.println();
            showErrorAlert("Error", "Failed to load the Add Movie form.");
        }
    }

    @FXML
    void openEditMovieForm(ActionEvent event) {
        Movie selected = tblMovies.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("No Movie Selected", "Please select a movie to edit.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/fxml_Manager/Movie/EditMovie.fxml")
            );
            Parent root = loader.load();

            EditMovieController editCtrl = loader.getController();
            editCtrl.setMovieData(selected);
            editCtrl.setMovieListController(this);

            Stage stage = new Stage();
            stage.setTitle("Edit Movie");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadMovieList();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to open Edit Movie form.");
        }
    }

    @FXML
    void handleTableClick(MouseEvent event) {
        Movie selected = tblMovies.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showMovieDetails(selected);
        }
    }

    private void showMovieDetails(Movie movie) {
        txtTitle.setText(movie.getTitle());
        txtDuration.setText(String.valueOf(movie.getDuration()));
        cbGenre.setValue(movie.getGenre());
        cbLanguage.setValue(movie.getLanguage());
        cbAgeRating.setValue(movie.getAgeRating());
        datePicker.setValue(movie.getReleasedDate());
        txtDescription.setText(movie.getDescription());

        String defaultImagePath = getClass().getResource("/images/default-poster.png").toExternalForm();

        if (movie.getPoster() != null && !movie.getPoster().isEmpty()) {
            File file = new File("images/" + movie.getPoster());
            if (file.exists()) {
                posterImage.setImage(new Image(file.toURI().toString()));
            } else {
                posterImage.setImage(new Image(defaultImagePath));
            }
        } else {
            posterImage.setImage(new Image(defaultImagePath));
        }

    }

    // ALERT HELPERS
    private void showWarningAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    @FXML
    void handleFindClick(MouseEvent event) {
        String keyword = tfFind.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadMovieList();
            return;
        }

        ObservableList<Movie> filteredList = FXCollections.observableArrayList();

        for (Movie movie : movieList) {
            // Kiểm tra các trường có chứa keyword không
            if (movie.getTitle().toLowerCase().contains(keyword)
                    || movie.getGenre().toLowerCase().contains(keyword)
                    || movie.getLanguage().toLowerCase().contains(keyword)
                    || movie.getAgeRating().toLowerCase().contains(keyword)
                    || movie.getDirectedBy().toLowerCase().contains(keyword)) {
                filteredList.add(movie);
            }
        }

        tblMovies.setItems(filteredList);
    }

}
