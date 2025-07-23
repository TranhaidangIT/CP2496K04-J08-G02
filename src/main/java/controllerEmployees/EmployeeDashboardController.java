package controllerEmployees;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class EmployeeDashboardController {

    @FXML
    private Button dashboardBtn;
    @FXML
    private Button bookingBtn;
    @FXML
    private Button sellAddonServicesBtn;
    @FXML
    private Button searchCancelTicketBtn;
    @FXML
    private Button logoutBtn;

    @FXML
    private Label staffNameLabel;
    @FXML
    private Label ticketsSoldLabel;
    @FXML
    private Label totalRevenueLabel;

    @FXML
    public void initialize() {
        staffNameLabel.setText("Tran hai dang"); //
        ticketsSoldLabel.setText("70"); //
        totalRevenueLabel.setText("6,600,000 VND"); //

        dashboardBtn.setStyle("-fx-background-color: #A52A2A;");
    }

    @FXML
    private void handleDashboardClick(ActionEvent event) {
        // Cần thay đổi đường dẫn FXML
        navigateToPage(event, "/fxml_Employees/EmployeeDashboard.fxml", "Dashboard Nhân Viên"); // Đã sửa
    }

    @FXML
    private void handleBookingClick(ActionEvent event) {
        // Cần thay đổi đường dẫn FXML
        navigateToPage(event, "/fxml_Employees/Booking.fxml", "Đặt Vé"); // Đã sửa (giả định Booking.fxml nằm trong fxml_Employees)
    }

    @FXML
    private void handleSellAddonServicesClick(ActionEvent event) {
        // Cần thay đổi đường dẫn FXML
        navigateToPage(event, "/fxml_Employees/SellAddonServices.fxml", "Bán Dịch Vụ Thêm"); // Đã sửa (giả định SellAddonServices.fxml nằm trong fxml_Employees)
    }

    @FXML
    private void handleSearchCancelTicketClick(ActionEvent event) {
        // Cần thay đổi đường dẫn FXML
        navigateToPage(event, "/fxml_Employees/SearchCancelTicket.fxml", "Tìm/Hủy Vé"); // Đã sửa (giả định SearchCancelTicket.fxml nằm trong fxml_Employees)
    }

    @FXML
    private void handleLogoutClick(ActionEvent event) {
        // Cần thay đổi đường dẫn FXML
        // Giả định trang Login.fxml nằm trong thư mục fxml_Employees, hoặc nếu là trang đăng nhập chung, bạn có thể cân nhắc một thư mục FXML chung cho nó
        navigateToPage(event, "/fxml_Employees/Login.fxml", "Đăng Nhập"); // Đã sửa (giả định Login.fxml nằm trong fxml_Employees)
    }

    // Phương thức chung để chuyển đổi màn hình
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
            // Optional: Hiển thị Alert cho người dùng
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Không thể tải trang");
            alert.setContentText("Đã xảy ra lỗi khi tải trang " + title + ". Vui lòng thử lại.");
            alert.showAndWait();
        }
    }
}