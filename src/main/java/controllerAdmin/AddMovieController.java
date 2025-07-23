package controllerAdmin;

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
import javafx.stage.Stage;
import models.Movie; // Đảm bảo lớp Movie của bạn được import
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Lớp điều khiển cho màn hình Thêm/Sửa Phim (AddMovie).
 * Quản lý việc thêm, cập nhật, xóa và hiển thị danh sách phim.
 * Không có chức năng import hình ảnh trực tiếp trên màn hình này.
 */
public class AddMovieController {

    // --- Sidebar Navigation Buttons ---
    @FXML private Button dashboardBtn;
    @FXML private Button movieShowtimeBtn;
    @FXML private Button ticketsForSaleBtn;
    @FXML private Button projectionRoomBtn;
    @FXML private Button userManageBtn;
    @FXML private Button logoutBtn;

    // --- UI elements for Movie Details Input ---
    @FXML private ImageView moviePosterImageView;
    @FXML private TextField movieTitleField;
    @FXML private TextField genreField;
    @FXML private TextField durationField;
    @FXML private DatePicker showingDateField;
    @FXML private TextField searchField;

    // --- UI elements for Actions ---
    @FXML private Button insertButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    // --- Table View for Movies ---
    @FXML private TableView<Movie> movieTableView;
    @FXML private TableColumn<Movie, String> movieTitleCol;
    @FXML private TableColumn<Movie, String> genreCol;
    @FXML private TableColumn<Movie, String> durationCol;
    @FXML private TableColumn<Movie, String> showingDateCol;

    private ObservableList<Movie> movieList = FXCollections.observableArrayList();
    private String currentImagePath; // Lưu đường dẫn ảnh tạm thời cho phim đang được chỉnh sửa/xem

    /**
     * Khởi tạo bộ điều khiển. Phương thức này được FXMLLoader tự động gọi.
     */
    @FXML
    public void initialize() {
        // Khởi tạo các cột của TableView
        movieTitleCol.setCellValueFactory(new PropertyValueFactory<>("movieName"));
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));
        showingDateCol.setCellValueFactory(new PropertyValueFactory<>("releaseDate"));

        // Tải dữ liệu mẫu hoặc từ CSDL
        loadSampleMovieData();
        movieTableView.setItems(movieList);

        // Lắng nghe sự kiện chọn hàng trong TableView
        movieTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayMovieDetails(newSelection);
            } else {
                clearFields();
            }
        });

        // Vô hiệu hóa Update/Delete ban đầu cho đến khi có phim được chọn
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    /**
     * Tải dữ liệu phim mẫu vào TableView. Trong ứng dụng thực tế sẽ tải từ CSDL.
     */
    private void loadSampleMovieData() {
        // Đảm bảo constructor của Movie nhận đủ 9 đối số
        movieList.add(new Movie(1, "Avengers: Infinity War", "Action/Sci-fi", "2:29:00", "2022-08-17", "/images/avengers.jpg", "Synopsis for Avengers", "Robert Downey Jr., Chris Evans", "10:00, 14:00"));
        movieList.add(new Movie(2, "Spider-Man: No Way Home", "Action/Adventure", "2:28:00", "2022-08-19", "/images/spiderman.jpg", "Synopsis for Spider-Man", "Tom Holland, Zendaya", "12:00, 16:00"));
        movieList.add(new Movie(3, "Black Panther", "Action/Adventure", "2:15:01", "2022-08-16", "/images/blackpanther.jpg", "Synopsis for Black Panther", "Chadwick Boseman, Michael B. Jordan", "18:00, 20:00"));
    }

    /**
     * Hiển thị chi tiết phim được chọn từ TableView vào các trường nhập liệu.
     * @param movie Đối tượng Movie cần hiển thị.
     */
    public void displayMovieDetails(Movie movie) {
        if (movie != null) {
            movieTitleField.setText(movie.getMovieName());
            genreField.setText(movie.getGenre());
            durationField.setText(movie.getDuration());

            if (movie.getReleaseDate() != null && !movie.getReleaseDate().isEmpty()) {
                try {
                    showingDateField.setValue(LocalDate.parse(movie.getReleaseDate()));
                } catch (Exception e) {
                    showingDateField.setValue(null);
                    System.err.println("Lỗi parse ngày releaseDate: " + e.getMessage());
                }
            } else {
                showingDateField.setValue(null);
            }

            currentImagePath = movie.getImagePath();
            if (currentImagePath != null && !currentImagePath.isEmpty()) {
                try {
                    Image image;
                    if (currentImagePath.startsWith("/")) { // Từ resources
                        image = new Image(getClass().getResourceAsStream(currentImagePath));
                    } else { // Từ đường dẫn file tuyệt đối
                        image = new Image("file:" + currentImagePath);
                    }

                    if (image.isError()) { // Nếu lỗi khi tải từ resource, thử lại với đường dẫn file
                        System.err.println("Ảnh lỗi hoặc không tìm thấy trong resource, thử tải từ đường dẫn file: " + currentImagePath);
                        image = new Image("file:" + currentImagePath);
                    }
                    moviePosterImageView.setImage(image);
                } catch (Exception e) {
                    System.err.println("Không thể tải ảnh cho phim " + movie.getMovieName() + ": " + currentImagePath + " - " + e.getMessage());
                    moviePosterImageView.setImage(null);
                }
            } else {
                moviePosterImageView.setImage(null);
            }

            insertButton.setDisable(true);
            updateButton.setDisable(false);
            deleteButton.setDisable(false);
        } else {
            clearFields();
        }
    }

    /**
     * Xử lý sự kiện khi nút "Insert" được nhấp.
     */
    @FXML
    private void handleInsertButton() {
        String title = movieTitleField.getText();
        String genre = genreField.getText();
        String duration = durationField.getText();
        LocalDate showingDate = showingDateField.getValue();

        // Khi thêm mới, nếu không có import ảnh, chúng ta cần một ảnh mặc định hoặc yêu cầu user cung cấp sau
        // Tạm thời, gán một ảnh mặc định nếu không có đường dẫn ảnh nào được set
        if (currentImagePath == null || currentImagePath.isEmpty()) {
            currentImagePath = "/images/default_poster.jpg"; // Đảm bảo bạn có ảnh này trong resources
        }

        if (title.isEmpty() || genre.isEmpty() || duration.isEmpty() || showingDate == null) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Vui lòng điền đầy đủ thông tin phim.");
            return;
        }

        String formattedShowingDate = showingDate.format(DateTimeFormatter.ISO_DATE);

        // Tạo đối tượng Movie mới với 9 tham số
        Movie newMovie = new Movie(generateNewMovieId(), title, genre, duration, formattedShowingDate, currentImagePath, "", "", "");
        movieList.add(newMovie);
        movieTableView.getSelectionModel().select(newMovie);

        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Phim '" + title + "' đã được thêm.");
        clearFields();
    }

    /**
     * Xử lý sự kiện khi nút "Update" được nhấp.
     */
    @FXML
    private void handleUpdateButton() {
        Movie selectedMovie = movieTableView.getSelectionModel().getSelectedItem();
        if (selectedMovie == null) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Vui lòng chọn một phim từ bảng để cập nhật.");
            return;
        }

        String title = movieTitleField.getText();
        String genre = genreField.getText();
        String duration = durationField.getText();
        LocalDate showingDate = showingDateField.getValue();

        if (title.isEmpty() || genre.isEmpty() || duration.isEmpty() || showingDate == null) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Vui lòng điền đầy đủ thông tin phim.");
            return;
        }

        String formattedShowingDate = showingDate.format(DateTimeFormatter.ISO_DATE);

        selectedMovie.setMovieName(title);
        selectedMovie.setGenre(genre);
        selectedMovie.setDuration(duration);
        selectedMovie.setReleaseDate(formattedShowingDate);
        selectedMovie.setImagePath(currentImagePath); // Giữ nguyên ảnh hiện tại hoặc sử dụng ảnh mới nếu có thay đổi từ màn hình edit chi tiết

        movieTableView.refresh();
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Phim '" + title + "' đã được cập nhật.");
        clearFields();
    }

    /**
     * Xử lý sự kiện khi nút "Delete" được nhấp.
     */
    @FXML
    private void handleDeleteButton() {
        Movie selectedMovie = movieTableView.getSelectionModel().getSelectedItem();
        if (selectedMovie == null) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Vui lòng chọn một phim từ bảng để xóa.");
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Xác nhận xóa");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText("Bạn có chắc chắn muốn xóa phim '" + selectedMovie.getMovieName() + "' không?");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            movieList.remove(selectedMovie);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Phim '" + selectedMovie.getMovieName() + "' đã bị xóa.");
            clearFields();
        }
    }

    /**
     * Xử lý sự kiện khi nút "Clear" được nhấp.
     */
    @FXML
    private void handleClearButton() {
        clearFields();
    }

    /**
     * Xóa nội dung của tất cả các TextField và ImageView, và đặt lại trạng thái nút.
     */
    private void clearFields() {
        movieTitleField.clear();
        genreField.clear();
        durationField.clear();
        showingDateField.setValue(null);
        moviePosterImageView.setImage(null);
        currentImagePath = null;
        movieTableView.getSelectionModel().clearSelection();

        insertButton.setDisable(false);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    /**
     * Tạo một ID phim mới (chỉ để giả lập).
     */
    private int generateNewMovieId() {
        if (movieList.isEmpty()) {
            return 1;
        }
        return movieList.stream().mapToInt(Movie::getId).max().orElse(0) + 1;
    }

    /**
     * Hiển thị một hộp thoại thông báo.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- Phương thức điều hướng thanh bên ---
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
        // Chuyển sang màn hình quản lý phim dạng card (MovieManagement)
        switchScreen(event, "/fxml_Admin/MovieManagement.fxml", "Quản lý Phim", 1229, 768);
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
}