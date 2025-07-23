package controllerAdmin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import models.Movie;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.VBox;


public class MovieManagementController {

    @FXML private Button dashboardBtn;
    @FXML private Button movieShowtimeBtn;
    @FXML private Button ticketsForSaleBtn;
    @FXML private Button projectionRoomBtn;
    @FXML private Button userManageBtn;
    @FXML private Button logoutBtn;
    @FXML private Button addMovieBtn;
    @FXML private TextField searchField;
    @FXML private FlowPane movieFlowPane; // Để chứa các MovieCard

    private List<Movie> movies = new ArrayList<>(); // Danh sách phim chính

    @FXML
    public void initialize() {
        loadSampleMovieData(); // Tải dữ liệu phim mẫu
        displayMovieCards(movies); // Hiển thị các card phim
    }

    private void loadSampleMovieData() {
        // Đảm bảo constructor của Movie nhận đủ 9 đối số
        movies.add(new Movie(1, "Avengers: Infinity War", "Action/Sci-fi", "2:29:00", "2022-08-17", "/images/avengers.jpg", "Synopsis for Avengers", "Robert Downey Jr., Chris Evans", "10:00, 14:00"));
        movies.add(new Movie(2, "Spider-Man: No Way Home", "Action/Adventure", "2:28:00", "2022-08-19", "/images/spiderman.jpg", "Synopsis for Spider-Man", "Tom Holland, Zendaya", "12:00, 16:00"));
        movies.add(new Movie(3, "Black Panther", "Action/Adventure", "2:15:01", "2022-08-16", "/images/blackpanther.jpg", "Synopsis for Black Panther", "Chadwick Boseman, Michael B. Jordan", "18:00, 20:00"));
        movies.add(new Movie(4, "The Great Gatsby", "Drama", "2:23:00", "2022-08-20", "/images/default_poster.jpg", "Synopsis for Gatsby", "Leonardo DiCaprio", "19:00, 21:00"));
        // Thêm nhiều phim hơn nếu cần
    }

    private void displayMovieCards(List<Movie> movieList) {
        movieFlowPane.getChildren().clear(); // Xóa các card cũ
        for (Movie movie : movieList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_Admin/MovieCard.fxml"));
                VBox movieCard = loader.load();
                MovieCardController controller = loader.getController();
                controller.setMovie(movie); // Truyền đối tượng Movie vào controller của card

                // Thiết lập sự kiện click cho từng card
                movieCard.setOnMouseClicked(event -> handleMovieCardClick(movie));

                movieFlowPane.getChildren().add(movieCard);
            } catch (IOException e) {
                System.err.println("Lỗi khi tải MovieCard.fxml: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleMovieCardClick(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_Admin/MovieInformation.fxml"));
            Parent root = loader.load();
            MovieInformationController movieInfoController = loader.getController();
            movieInfoController.setMovie(movie); // Truyền đối tượng phim để hiển thị chi tiết và chỉnh sửa

            Stage stage = (Stage) movieFlowPane.getScene().getWindow(); // Lấy stage hiện tại
            Scene scene = new Scene(root, 1229, 768); // Kích thước màn hình
            stage.setScene(scene);
            stage.setTitle("Thông tin Phim");
            stage.show();
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Lỗi khi chuyển sang màn hình MovieInformation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Sidebar Navigation Methods ---
    private void switchScreen(ActionEvent event, String fxmlPath, String title, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Lỗi khi chuyển màn hình sang " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDashboardButton(ActionEvent event) {
        switchScreen(event, "/fxml_Admin/Dashboard.fxml", "Bảng điều khiển Admin", 1229, 768);
    }

    @FXML
    private void handleMovieShowtimeButton(ActionEvent event) {
        // Đã ở màn hình này, không làm gì cả hoặc làm mới
        displayMovieCards(movies); // Có thể làm mới danh sách
    }

    @FXML
    private void handleTicketsForSaleButton(ActionEvent event) {
        switchScreen(event, "/fxml_Admin/TicketsForSale.fxml", "Quản lý vé bán", 1229, 768);
    }

    @FXML
    private void handleProjectionRoomButton(ActionEvent event) {
        switchScreen(event, "/fxml_Admin/ProjectionRoom.fxml", "Quản lý phòng chiếu", 1229, 768);
    }

    @FXML
    private void handleUserManageButton(ActionEvent event) {
        switchScreen(event, "/fxml_Admin/ManageUser.fxml", "Quản lý người dùng", 1229, 768);
    }

    @FXML
    private void handleLogoutButton(ActionEvent event) {
        switchScreen(event, "/fxml_Admin/Login.fxml", "Đăng nhập", 600, 400);
    }

    @FXML
    private void handleAddMovieButton(ActionEvent event) {
        switchScreen(event, "/fxml_Admin/AddMovie.fxml", "Thêm Phim Mới", 1229, 768);
    }
}