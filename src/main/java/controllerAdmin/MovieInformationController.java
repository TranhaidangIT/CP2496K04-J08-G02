package controllerAdmin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Movie;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class MovieInformationController {

    @FXML private Button dashboardBtn;
    @FXML private Button movieShowtimeBtn;
    @FXML private Button ticketsForSaleBtn;
    @FXML private Button projectionRoomBtn;
    @FXML private Button userManageBtn;
    @FXML private Button logoutBtn;

    @FXML private ImageView moviePosterImageView;
    @FXML private TextField movieTitleField;
    @FXML private TextField genreField;
    @FXML private TextField durationField;
    @FXML private TextField showtimeField;
    @FXML private DatePicker releaseDateField; // Đây là ngày phát hành/chiếu
    @FXML private TextArea castTextArea;
    @FXML private TextArea synopsisTextArea;

    @FXML private Button uploadImageBtn;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;
    @FXML private Button backButton;

    private Movie currentMovie; // Đối tượng phim hiện tại đang được hiển thị/chỉnh sửa
    private String newImagePath; // Lưu đường dẫn ảnh mới nếu người dùng upload

    /**
     * Phương thức này được gọi từ MovieManagementController để truyền đối tượng Movie.
     * @param movie Đối tượng Movie cần hiển thị chi tiết.
     */
    public void setMovie(Movie movie) {
        this.currentMovie = movie;
        displayMovieDetails();
    }

    private void displayMovieDetails() {
        if (currentMovie != null) {
            movieTitleField.setText(currentMovie.getMovieName());
            genreField.setText(currentMovie.getGenre());
            durationField.setText(currentMovie.getDuration());
            showtimeField.setText(currentMovie.getShowtime());
            castTextArea.setText(currentMovie.getCast());
            synopsisTextArea.setText(currentMovie.getSynopsis());

            // Chuyển đổi String ngày sang LocalDate cho DatePicker
            if (currentMovie.getReleaseDate() != null && !currentMovie.getReleaseDate().isEmpty()) {
                try {
                    releaseDateField.setValue(LocalDate.parse(currentMovie.getReleaseDate()));
                } catch (Exception e) {
                    releaseDateField.setValue(null);
                    System.err.println("Lỗi parse ngày releaseDate: " + e.getMessage());
                }
            } else {
                releaseDateField.setValue(null);
            }

            // Tải và hiển thị ảnh poster
            newImagePath = currentMovie.getImagePath(); // Khởi tạo newImagePath bằng ảnh hiện tại
            if (newImagePath != null && !newImagePath.isEmpty()) {
                try {
                    Image image;
                    if (newImagePath.startsWith("/")) { // Từ resources
                        image = new Image(getClass().getResourceAsStream(newImagePath));
                    } else { // Từ đường dẫn file tuyệt đối
                        image = new Image("file:" + newImagePath);
                    }
                    if (image.isError()) {
                        System.err.println("Ảnh lỗi hoặc không tìm thấy trong resource, thử tải từ đường dẫn file: " + newImagePath);
                        image = new Image("file:" + newImagePath); // Thử lại với đường dẫn file
                    }
                    moviePosterImageView.setImage(image);
                } catch (Exception e) {
                    System.err.println("Không thể tải ảnh cho phim " + currentMovie.getMovieName() + ": " + newImagePath + " - " + e.getMessage());
                    moviePosterImageView.setImage(null);
                }
            } else {
                moviePosterImageView.setImage(null);
            }
        }
    }

    @FXML
    private void handleUploadImageButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn Poster Phim");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());
                moviePosterImageView.setImage(image);
                newImagePath = selectedFile.getAbsolutePath(); // Lưu đường dẫn tuyệt đối mới
                showAlert(Alert.AlertType.INFORMATION, "Upload Ảnh", "Ảnh đã được chọn: " + selectedFile.getName());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải ảnh: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSaveChangesButton() {
        if (currentMovie == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không có phim nào được chọn để lưu.");
            return;
        }

        String title = movieTitleField.getText();
        String genre = genreField.getText();
        String duration = durationField.getText();
        String showtime = showtimeField.getText();
        LocalDate releaseDate = releaseDateField.getValue();
        String cast = castTextArea.getText();
        String synopsis = synopsisTextArea.getText();

        if (title.isEmpty() || genre.isEmpty() || duration.isEmpty() || showtime.isEmpty() || releaseDate == null || cast.isEmpty() || synopsis.isEmpty() || newImagePath == null || newImagePath.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Vui lòng điền đầy đủ thông tin và đảm bảo poster đã được tải lên.");
            return;
        }

        String formattedReleaseDate = releaseDate.format(DateTimeFormatter.ISO_DATE);

        // Cập nhật đối tượng Movie hiện tại
        currentMovie.setMovieName(title);
        currentMovie.setGenre(genre);
        currentMovie.setDuration(duration);
        currentMovie.setShowtime(showtime);
        currentMovie.setReleaseDate(formattedReleaseDate);
        currentMovie.setCast(cast);
        currentMovie.setSynopsis(synopsis);
        currentMovie.setImagePath(newImagePath); // Cập nhật đường dẫn ảnh

        // Trong ứng dụng thực tế: Gọi DAO để lưu các thay đổi vào CSDL
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thông tin phim '" + title + "' đã được lưu.");
        // Bạn có thể quay lại màn hình danh sách phim hoặc làm mới nó ở đây
    }

    @FXML
    private void handleDeleteMovieButton() {
        if (currentMovie == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không có phim nào được chọn để xóa.");
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Xác nhận xóa");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText("Bạn có chắc chắn muốn xóa phim '" + currentMovie.getMovieName() + "' không?");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Trong ứng dụng thực tế: Gọi DAO để xóa phim khỏi CSDL
            // Sau khi xóa, quay lại màn hình danh sách phim
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Phim '" + currentMovie.getMovieName() + "' đã bị xóa.");
            handleBackButton(new ActionEvent()); // Quay lại màn hình danh sách
        }
    }

    @FXML
    private void handleBackButton(ActionEvent event) {
        // Quay lại màn hình MovieManagement
        switchScreen(event, "/fxml_Admin/MovieManagement.fxml", "Quản lý Phim", 1229, 768);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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