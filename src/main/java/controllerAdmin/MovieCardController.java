package controllerAdmin;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import models.Movie;

import java.io.File;
import java.io.InputStream; // Thêm import này

public class MovieCardController {

    @FXML
    private Label movieNameLabel;
    @FXML
    private Label genreLabel;
    @FXML
    private Label durationLabel;
    @FXML
    private ImageView movieImageView;

    private Movie movie;

    /**
     * Đặt đối tượng Movie cho card và cập nhật giao diện.
     * Đây là phương thức được gọi từ MovieManagementController.
     * @param movie Đối tượng Movie cần hiển thị.
     */
    public void setMovie(Movie movie) {
        this.movie = movie;
        if (movie != null) { // Luôn kiểm tra null trước khi truy cập movie
            movieNameLabel.setText(movie.getMovieName());
            genreLabel.setText(movie.getGenre());
            durationLabel.setText(movie.getDuration());

            // Tải ảnh hoặc hiển thị ảnh mặc định
            String imagePath = movie.getImagePath();
            Image imageToSet = null; // Khởi tạo với null

            // Cố gắng tải ảnh từ đường dẫn đã cung cấp
            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    if (imagePath.startsWith("/")) { // Từ resources (ví dụ: /images/avengers.jpg)
                        InputStream is = getClass().getResourceAsStream(imagePath);
                        if (is != null) { // Kiểm tra null trước khi tạo Image
                            imageToSet = new Image(is);
                        } else {
                            System.err.println("Không tìm thấy resource: " + imagePath);
                        }
                    } else { // Từ đường dẫn file tuyệt đối (ví dụ: C:/path/to/image.jpg)
                        File file = new File(imagePath);
                        if (file.exists()) { // Kiểm tra file tồn tại
                            imageToSet = new Image(file.toURI().toString());
                        } else {
                            System.err.println("Không tìm thấy file: " + imagePath);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi cố gắng tải ảnh từ đường dẫn " + imagePath + ": " + e.getMessage());
                }
            }

            // Nếu không tải được ảnh từ đường dẫn hoặc đường dẫn null/rỗng, sử dụng ảnh mặc định
            if (imageToSet == null || imageToSet.isError()) { // Kiểm tra nếu imageToSet vẫn null hoặc có lỗi
                try {
                    InputStream defaultIs = getClass().getResourceAsStream("/images/default_poster.jpg");
                    if (defaultIs != null) {
                        imageToSet = new Image(defaultIs);
                    } else {
                        System.err.println("Cảnh báo: Không tìm thấy ảnh mặc định tại /images/default_poster.jpg");
                        // Nếu cả ảnh mặc định cũng không có, đặt ImageView là null
                        imageToSet = null;
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi tải ảnh mặc định: " + e.getMessage());
                    imageToSet = null; // Đặt null nếu ảnh mặc định cũng lỗi
                }
            }
            movieImageView.setImage(imageToSet);
        } else {
            // Xử lý trường hợp movie là null (không mong muốn ở đây nhưng an toàn)
            movieNameLabel.setText("N/A");
            genreLabel.setText("N/A");
            durationLabel.setText("N/A");
            movieImageView.setImage(null); // Không có phim thì không có ảnh
        }
    }
}