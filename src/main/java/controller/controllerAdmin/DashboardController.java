package controller.controllerAdmin;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button; // Nếu bạn muốn xử lý các button ở sidebar

public class DashboardController {

    // Labels để hiển thị dữ liệu
    @FXML
    private Label revenueLabel;
    @FXML
    private Label moviesLabel;
    @FXML
    private Label ticketsLabel;

    // Các nút sidebar (tùy chọn, nếu bạn muốn xử lý chúng ngay)
    @FXML
    private Button dashboardBtn;
    @FXML
    private Button movieShowtimeBtn;
    @FXML
    private Button ticketsForSaleBtn;
    @FXML
    private Button projectionRoomBtn;
    @FXML
    private Button userManageBtn;
    @FXML
    private Button logoutBtn;


    /**
     * Phương thức này được gọi tự động sau khi FXML được tải hoàn chỉnh.
     * Đây là nơi tốt nhất để khởi tạo dữ liệu cho dashboard.
     */
    @FXML
    public void initialize() {
        // Tải và hiển thị dữ liệu khi dashboard được khởi tạo
        loadDashboardData();

        // Cài đặt sự kiện cho các nút sidebar (Tùy chọn)
        // Ví dụ:
        // movieShowtimeBtn.setOnAction(event -> navigateToMovieShowtime());
        // logoutBtn.setOnAction(event -> handleLogout());
    }

    private void loadDashboardData() {
        // --- GIẢ LẬP DỮ LIỆU ---
        // Trong ứng dụng thực tế, bạn sẽ gọi các phương thức từ DAO để lấy dữ liệu từ DB.
        double totalRevenue = 250123456.78; // Ví dụ: Doanh thu
        int totalMovies = 65;               // Ví dụ: Tổng số phim
        int totalTicketsSold = 35421;       // Ví dụ: Tổng số vé đã bán
        // -----------------------

        // Cập nhật các Label trên giao diện
        revenueLabel.setText(String.format("%,.2f VND", totalRevenue)); // Định dạng tiền tệ
        moviesLabel.setText(String.valueOf(totalMovies));
        ticketsLabel.setText(String.valueOf(totalTicketsSold));
    }


}