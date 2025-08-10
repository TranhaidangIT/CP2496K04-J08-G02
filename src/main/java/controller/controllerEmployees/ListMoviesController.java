package controller.controllerEmployees;

import dao.MovieDAO;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import models.Movie;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

public class ListMoviesController implements Initializable {

    @FXML
    private AnchorPane contentArea; // Thêm biến để ánh xạ contentArea từ EmployeeSidebar.fxml

    @FXML private Button genreButton;
    @FXML private ListView<String> genreListView;
    @FXML private Button durationButton;
    @FXML private ListView<String> durationListView;
    @FXML private Button ratingButton;
    @FXML private ListView<String> ratingListView;
    @FXML private Button languageButton;
    @FXML private ListView<String> languageListView;
    @FXML private FlowPane nowShowingPane;
    @FXML private FlowPane comingSoonPane;
    @FXML private TextField searchField;

    private final List<ListView<?>> allListViews = new ArrayList<>();
    private final Map<String, BooleanProperty> selectedMap = new HashMap<>();

    public void setContentArea(AnchorPane contentArea) {
        this.contentArea = contentArea;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Giữ nguyên mã initialize hiện tại
        searchField.setOnAction(e -> searchMoviesByTitle());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterMovies());
        displayMovies();

        genreButton.setOnAction(e -> {
            hideAllListViewsExcept(genreListView);
            genreListView.setVisible(!genreListView.isVisible());
        });

        durationButton.setOnAction(e -> {
            hideAllListViewsExcept(durationListView);
            durationListView.setVisible(!durationListView.isVisible());
        });

        ratingButton.setOnAction(e -> {
            hideAllListViewsExcept(ratingListView);
            ratingListView.setVisible(!ratingListView.isVisible());
        });

        languageButton.setOnAction(e -> {
            hideAllListViewsExcept(languageListView);
            languageListView.setVisible(!languageListView.isVisible());
        });

        allListViews.add(genreListView);
        allListViews.add(durationListView);
        allListViews.add(ratingListView);
        allListViews.add(languageListView);

        ObservableList<String> genres = FXCollections.observableArrayList("Action", "Drama", "Comedy", "Horror", "Romance");
        genreListView.setItems(genres);
        genreListView.setVisible(false);
        for (String genre : genres) {
            selectedMap.put(genre, new SimpleBooleanProperty(false));
        }
        genreListView.setCellFactory(CheckBoxListCell.forListView(item -> selectedMap.get(item)));
        genres.forEach(item -> selectedMap.get(item).addListener((obs, wasSelected, isSelected) -> filterMovies()));

        ObservableList<String> durations = FXCollections.observableArrayList("Under 90 mins", "90–120 mins", "120–150 mins", "Over 150 mins");
        durationListView.setItems(durations);
        durationListView.setVisible(false);
        for (String duration : durations) {
            selectedMap.put(duration, new SimpleBooleanProperty(false));
        }
        durationListView.setCellFactory(CheckBoxListCell.forListView(item -> selectedMap.get(item)));
        durations.forEach(item -> selectedMap.get(item).addListener((obs, wasSelected, isSelected) -> filterMovies()));

        ObservableList<String> rate = FXCollections.observableArrayList("P", "K", "T13", "T16", "T18");
        ratingListView.setItems(rate);
        ratingListView.setVisible(false);
        for (String rating : rate) {
            selectedMap.put(rating, new SimpleBooleanProperty(false));
        }
        ratingListView.setCellFactory(CheckBoxListCell.forListView(item -> selectedMap.get(item)));
        rate.forEach(item -> selectedMap.get(item).addListener((obs, wasSelected, isSelected) -> filterMovies()));

        ObservableList<String> languages = FXCollections.observableArrayList("English", "Vietnamese", "Korean", "Japanese");
        languageListView.setItems(languages);
        languageListView.setVisible(false);
        for (String language : languages) {
            selectedMap.put(language, new SimpleBooleanProperty(false));
        }
        languageListView.setCellFactory(CheckBoxListCell.forListView(item -> selectedMap.get(item)));
        languages.forEach(item -> selectedMap.get(item).addListener((obs, wasSelected, isSelected) -> filterMovies()));
    }

    private void hideAllListViewsExcept(ListView<?> showThis) {
        for (ListView<?> listView : allListViews) {
            if (listView != showThis) {
                listView.setVisible(false);
            }
        }
    }

    private void displayMovies() {
        List<Movie> movies = MovieDAO.getAllMovies();
        LocalDate today = LocalDate.now();
        List<Movie> nowShowing = new ArrayList<>();
        List<Movie> comingSoon = new ArrayList<>();

        for (Movie movie : movies) {
            if (movie.getReleasedDate().isAfter(today)) {
                comingSoon.add(movie);
            } else {
                nowShowing.add(movie);
            }
        }

        nowShowingPane.getChildren().clear();
        comingSoonPane.getChildren().clear();

        for (Movie movie : nowShowing) {
            nowShowingPane.getChildren().add(createMovieCard(movie));
        }

        for (Movie movie : comingSoon) {
            comingSoonPane.getChildren().add(createMovieCard(movie));
        }
    }

    private VBox createMovieCard(Movie movie) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 10; -fx-background-radius: 10;");
        card.setPrefWidth(180);

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(130, 180);
        imageContainer.setStyle("-fx-background-color: #f0f0f0; -fx-alignment: center;");

        ImageView posterView = new ImageView();
        String posterFileName = movie.getPoster();
        InputStream imgStream = getClass().getResourceAsStream("/images/" + posterFileName);

        if (imgStream != null) {
            posterView.setImage(new Image(imgStream));
        } else {
            InputStream defaultStream = getClass().getResourceAsStream("/images/default-poster.png");
            if (defaultStream != null) {
                posterView.setImage(new Image(defaultStream));
            } else {
                System.err.println("not found poster");
            }
        }

        posterView.setFitWidth(130);
        posterView.setFitHeight(180);
        posterView.setPreserveRatio(false);

        imageContainer.getChildren().add(posterView);

        Label titleLabel = new Label(movie.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label genreLabel = new Label("Genre: " + movie.getGenre());
        genreLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");
        Label durationLabel = new Label("Duration: " + movie.getDuration() + " min");
        durationLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");

        card.getChildren().addAll(imageContainer, titleLabel, genreLabel, durationLabel);
        card.setOnMouseClicked(e -> showMovieDetails(movie));
        return card;
    }

    private void showMovieDetails(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/MovieDetail.fxml"));
            Parent root = loader.load();
            MovieDetailController detailController = loader.getController();
            detailController.setMovie(movie);
            contentArea.getChildren().setAll(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
        } catch (IOException e) {
            System.err.println("Error load MovieDetail.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void searchMoviesByTitle() {
        String keyword = searchField.getText().trim().toLowerCase();
        List<Movie> allMovies = MovieDAO.getAllMovies();
        List<Movie> filteredMovies = new ArrayList<>();

        for (Movie movie : allMovies) {
            if (movie.getTitle().toLowerCase().contains(keyword)) {
                filteredMovies.add(movie);
            }
        }

        nowShowingPane.getChildren().clear();
        comingSoonPane.getChildren().clear();

        if (filteredMovies.isEmpty()) {
            Label noResult = new Label("No movies found for: \"" + keyword + "\"");
            noResult.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            nowShowingPane.getChildren().add(noResult);
        } else {
            LocalDate today = LocalDate.now();
            for (Movie movie : filteredMovies) {
                VBox card = createMovieCard(movie);
                if (movie.getReleasedDate().isAfter(today)) {
                    comingSoonPane.getChildren().add(card);
                } else {
                    nowShowingPane.getChildren().add(card);
                }
            }
        }
    }

    private void filterMovies() {
        List<Movie> allMovies = MovieDAO.getAllMovies();
        List<Movie> filtered = new ArrayList<>();
        String keyword = searchField.getText().toLowerCase().trim();

        for (Movie movie : allMovies) {
            boolean match = true;

            if (!keyword.isEmpty()) {
                boolean contains = movie.getTitle().toLowerCase().contains(keyword)
                        || movie.getGenre().toLowerCase().contains(keyword)
                        || movie.getDirectedBy().toLowerCase().contains(keyword);
                if (!contains) {
                    match = false;
                }
            }

            List<String> selectedGenres = new ArrayList<>();
            for (String genre : Arrays.asList("Action", "Drama", "Comedy", "Horror", "Romance")) {
                BooleanProperty prop = selectedMap.get(genre);
                if (prop != null && prop.get()) selectedGenres.add(genre);
            }
            if (!selectedGenres.isEmpty()) {
                boolean genreMatch = false;
                for (String selected : selectedGenres) {
                    if (movie.getGenre().toLowerCase().contains(selected.toLowerCase())) {
                        genreMatch = true;
                        break;
                    }
                }
                if (!genreMatch) {
                    match = false;
                }
            }

            List<String> selectedDurations = new ArrayList<>();
            for (String d : Arrays.asList("Under 90 mins", "90–120 mins", "120–150 mins", "Over 150 mins")) {
                BooleanProperty prop = selectedMap.get(d);
                if (prop != null && prop.get()) selectedDurations.add(d);
            }
            if (!selectedDurations.isEmpty()) {
                boolean durationMatch = false;
                int duration = movie.getDuration();
                for (String d : selectedDurations) {
                    switch (d) {
                        case "Under 90 mins":
                            if (duration < 90) durationMatch = true;
                            break;
                        case "90–120 mins":
                            if (duration >= 90 && duration <= 120) durationMatch = true;
                            break;
                        case "120–150 mins":
                            if (duration > 120 && duration <= 150) durationMatch = true;
                            break;
                        case "Over 150 mins":
                            if (duration > 150) durationMatch = true;
                            break;
                    }
                }
                if (!durationMatch) {
                    match = false;
                }
            }

            List<String> selectedRatings = new ArrayList<>();
            for (String rating : Arrays.asList("P", "K", "T13", "T16", "T18")) {
                BooleanProperty prop = selectedMap.get(rating);
                if (prop != null && prop.get()) selectedRatings.add(rating);
            }
            if (!selectedRatings.isEmpty() && !selectedRatings.contains(movie.getAgeRating())) {
                match = false;
            }

            List<String> selectedLangs = new ArrayList<>();
            for (String lang : Arrays.asList("English", "Tiếng Việt")) {
                BooleanProperty prop = selectedMap.get(lang);
                if (prop != null && prop.get()) selectedLangs.add(lang);
            }
            if (!selectedLangs.isEmpty() && !selectedLangs.contains(movie.getLanguage())) {
                match = false;
            }

            if (match) filtered.add(movie);
        }

        nowShowingPane.getChildren().clear();
        comingSoonPane.getChildren().clear();

        if (filtered.isEmpty()) {
            Label empty = new Label("No movies match selected filters.");
            empty.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");
            nowShowingPane.getChildren().add(empty);
        } else {
            LocalDate today = LocalDate.now();
            for (Movie movie : filtered) {
                VBox card = createMovieCard(movie);
                if (movie.getReleasedDate().isAfter(today)) {
                    comingSoonPane.getChildren().add(card);
                } else {
                    nowShowingPane.getChildren().add(card);
                }
            }
        }
    }

    private String getSelected(String... options) {
        for (String option : options) {
            BooleanProperty prop = selectedMap.get(option);
            if (prop != null && prop.get()) return option;
        }
        return null;
    }
}