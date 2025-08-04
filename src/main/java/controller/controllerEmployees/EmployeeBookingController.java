package controller.controllerEmployees;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.paint.Color; // <-- Thêm import này

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EmployeeBookingController {

    @FXML private Button dashboardBtn;
    @FXML private Button bookingBtn;
    @FXML private Button sellAddonServicesBtn;
    @FXML private Button searchCancelTicketBtn;
    @FXML private Button logoutBtn;

    @FXML private TextField dateField;
    @FXML private TextField nameField;
    @FXML private TextField timeField;
    @FXML private TextField phoneField;
    @FXML private Button viewHistoryBtn;
    @FXML private Button bookSeatBtn;

    // Các ghế trong FXML
    @FXML private Button seatA1, seatA2, seatA3, seatA4, seatA5, seatA6, seatA7, seatA8;
    @FXML private Button seatB1, seatB2, seatB3, seatB4, seatB5;
    @FXML private Button seatC1, seatC2, seatC3, seatC4, seatC5;
    // Khai báo thêm các ghế khác nếu có (ví dụ: A4, A5, A6)
    // List để dễ quản lý các ghế
    private List<Button> allSeats;
    private List<Button> selectedSeats = new ArrayList<>();

    @FXML
    public void initialize() {
        // Đặt màu nền cho nút Booking khi khởi tạo (được chọn)
        bookingBtn.setStyle("-fx-background-color: #A52A2A;");

        // Khởi tạo danh sách tất cả các ghế
        allSeats = new ArrayList<>();
        // Thêm tất cả các nút ghế vào danh sách
        allSeats.add(seatA1); allSeats.add(seatA2); allSeats.add(seatA3);
        allSeats.add(seatA4); allSeats.add(seatA5); allSeats.add(seatA6); allSeats.add(seatA7); allSeats.add(seatA8);
        allSeats.add(seatB1); allSeats.add(seatB2); allSeats.add(seatB3); allSeats.add(seatB4); allSeats.add(seatB5);
        allSeats.add(seatC1); allSeats.add(seatC2); allSeats.add(seatC3); allSeats.add(seatC4); allSeats.add(seatC5);

        // Đặt trạng thái ban đầu cho một số ghế (ví dụ: đã đặt)
        // Đây chỉ là ví dụ, bạn cần tải trạng thái ghế từ database
        setSeatStatus(seatA1, "booked"); // Màu đỏ (đã đặt)
        setSeatStatus(seatA2, "booked"); // Màu đỏ (đã đặt)
        setSeatStatus(seatA3, "selected"); // Màu xanh (đang chọn), theo ảnh
    }

    @FXML
    private void handleSeatClick(ActionEvent event) {
        Button clickedSeat = (Button) event.getSource();
        if (isSeatBooked(clickedSeat)) {
            showAlert(Alert.AlertType.INFORMATION, "Ghế đã được đặt", "Ghế này đã có người đặt, vui lòng chọn ghế khác.");
            return;
        }

        if (selectedSeats.contains(clickedSeat)) {
            // Bỏ chọn ghế
            selectedSeats.remove(clickedSeat);
            setSeatStatus(clickedSeat, "available"); // Chuyển về trạng thái trống
        } else {
            // Chọn ghế
            selectedSeats.add(clickedSeat);
            setSeatStatus(clickedSeat, "selected"); // Chuyển sang trạng thái đang chọn
        }
        updateSelectedSeatsDisplay();
    }

    private void setSeatStatus(Button seat, String status) {
        // Xóa tất cả style class liên quan đến trạng thái ghế
        seat.getStyleClass().remove("seat-available");
        seat.getStyleClass().remove("seat-selected");
        seat.getStyleClass().remove("seat-booked");

        // Thêm style class mới
        seat.getStyleClass().add("seat-" + status);

        // Cập nhật màu của FontIcon bên trong Button
        if (seat.getGraphic() instanceof FontIcon) {
            FontIcon icon = (FontIcon) seat.getGraphic();
            switch (status) {
                case "available":
                    icon.setIconColor(Color.BLACK); // Sử dụng Color.BLACK
                    break;
                case "selected":
                    icon.setIconColor(Color.web("#6A5ACD")); // Sử dụng Color.web()
                    break;
                case "booked":
                    icon.setIconColor(Color.web("#DC143C")); // Sử dụng Color.web()
                    break;
            }
        }
    }

    private boolean isSeatBooked(Button seat) {
        // Đây là ví dụ, trong thực tế bạn sẽ kiểm tra trạng thái từ database
        return seat.getStyleClass().contains("seat-booked");
    }

    private void updateSelectedSeatsDisplay() {
        // Có thể cập nhật một Label hoặc TextArea để hiển thị danh sách ghế đã chọn
        StringBuilder sb = new StringBuilder("Ghế đã chọn: ");
        for (Button seat : selectedSeats) {
            sb.append(seat.getId()).append(" ");
        }
        System.out.println(sb.toString());
        // Ví dụ: someLabel.setText(sb.toString());
    }

    @FXML
    private void handleViewHistoryClick(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Lịch sử đặt vé", "Chức năng xem lịch sử đặt vé sẽ được phát triển.");
        // TODO: Implement logic to view booking history
    }

    @FXML
    private void handleBookSeatClick(ActionEvent event) {
        if (selectedSeats.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn ghế", "Vui lòng chọn ít nhất một ghế để đặt.");
            return;
        }
        if (dateField.getText().isEmpty() || timeField.getText().isEmpty() || nameField.getText().isEmpty() || phoneField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng điền đầy đủ thông tin ngày, giờ, tên và số điện thoại.");
            return;
        }

        // Logic đặt ghế
        StringBuilder bookedSeats = new StringBuilder();
        for (Button seat : selectedSeats) {
            bookedSeats.append(seat.getId()).append(" ");
            setSeatStatus(seat, "booked"); // Chuyển trạng thái các ghế đã chọn thành "đã đặt"
        }
        showAlert(Alert.AlertType.INFORMATION, "Đặt ghế thành công!",
                "Bạn đã đặt các ghế: " + bookedSeats.toString() + "\n" +
                        "Ngày: " + dateField.getText() + ", Giờ: " + timeField.getText() + "\n" +
                        "Tên: " + nameField.getText() + ", Điện thoại: " + phoneField.getText());

        selectedSeats.clear(); // Xóa các ghế đã chọn sau khi đặt thành công
        // TODO: Lưu thông tin đặt vé vào database
    }

    // Các phương thức xử lý sự kiện cho Sidebar
    @FXML private void handleDashboardClick(ActionEvent event) { navigateToPage(event, "/views/fxml_Employees/EmployeeDashboard.fxml", "Dashboard Nhân Viên"); }
    @FXML private void handleBookingClick(ActionEvent event) { navigateToPage(event, "/views/fxml_Employees/Booking.fxml", "Đặt Vé"); } // Reload trang nếu cần
    @FXML private void handleSellAddonServicesClick(ActionEvent event) { navigateToPage(event, "/views/fxml_Manager/fxml_Employees/SellAddonServices.fxml", "Bán Dịch Vụ Thêm"); }
    @FXML private void handleSearchCancelTicketClick(ActionEvent event) { navigateToPage(event, "/views/fxml_Manager/fxml_Employees/SearchCancelTicket.fxml", "Tìm/Hủy Vé"); }
    @FXML private void handleLogoutClick(ActionEvent event) { navigateToPage(event, "/views/fxml_Manager/fxml_Employees/Login.fxml", "Đăng Nhập"); }

    private void navigateToPage(ActionEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            System.err.println("Lỗi khi tải trang FXML " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Không thể tải trang");
            alert.setContentText("Đã xảy ra lỗi khi tải trang " + title + ". Vui lòng thử lại.");
            alert.showAndWait();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}