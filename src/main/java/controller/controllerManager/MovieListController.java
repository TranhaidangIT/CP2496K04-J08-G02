package controller.controllerManager;

import dao.MovieDAO;
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

import java.io.File;
import java.io.IOException;
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

        // Table column mapping
        colId.setCellValueFactory(new PropertyValueFactory<>("movieId"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colReleaseDate.setCellValueFactory(new PropertyValueFactory<>("releasedDate"));

        // Load movie data
        loadMovieList();
    }

    public void loadMovieList() {
        List<Movie> movies = MovieDAO.getAllMovies();
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
        // Text fields - chỉ đọc
        txtTitle.setText(movie.getTitle());
        txtTitle.setEditable(false);

        txtDuration.setText(String.valueOf(movie.getDuration()));
        txtDuration.setEditable(false);

        txtDirectedby.setText(movie.getDirectedBy());
        txtDirectedby.setEditable(false);

        txtDescription.setText(movie.getDescription());
        txtDescription.setEditable(false);

        // ComboBox - chỉ hiển thị, không cho chọn
        cbGenre.setValue(movie.getGenre());
        cbGenre.setMouseTransparent(true);
        cbGenre.setFocusTraversable(false);

        cbLanguage.setValue(movie.getLanguage());
        cbLanguage.setMouseTransparent(true);
        cbLanguage.setFocusTraversable(false);

        cbAgeRating.setValue(movie.getAgeRating());
        cbAgeRating.setMouseTransparent(true);
        cbAgeRating.setFocusTraversable(false);

        // DatePicker - chỉ đọc
        datePicker.setValue(movie.getReleasedDate());
        datePicker.setMouseTransparent(true);
        datePicker.setFocusTraversable(false);

        // Poster
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

    @FXML
    void handleFindClick(MouseEvent event) {
        String keyword = tfFind.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadMovieList();
            return;
        }

        ObservableList<Movie> filteredList = FXCollections.observableArrayList();
        for (Movie movie : movieList) {
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
