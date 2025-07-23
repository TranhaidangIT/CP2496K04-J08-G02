package controllerAdmin;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.event.ActionEvent; // Quan trọng: Đảm bảo đã import ActionEvent
import javafx.scene.Node;        // Quan trọng: Đảm bảo đã import Node
import javafx.stage.Stage;       // Quan trọng: Đảm bảo đã import Stage


public class ForgotPasswordController {

    @FXML
    private TextField emailField; // Liên kết với ô nhập email trong FXML

    // Phương thức xử lý khi người dùng nhấn nút "Submit"
    @FXML // Phải có @FXML để FXML có thể gọi phương thức này
    private void handleSubmit() {
        String email = emailField.getText();

        // Đây là nơi bạn sẽ thêm logic xử lý quên mật khẩu.
        // Ví dụ: gửi email đặt lại mật khẩu đến địa chỉ email này.
        if (email.isEmpty()) {
            showAlert(AlertType.WARNING, "Lỗi", "Vui lòng nhập địa chỉ email.");
        } else {
            // Giả lập việc gửi email
            showAlert(AlertType.INFORMATION, "Gửi Yêu Cầu", "Yêu cầu đặt lại mật khẩu đã được gửi đến: " + email + "\nVui lòng kiểm tra email của bạn.");
            // Sau khi gửi, có thể đóng cửa sổ hoặc chuyển về màn hình Login
            // Ví dụ: Đóng cửa sổ hiện tại (nếu bạn muốn)
            // Stage stage = (Stage) emailField.getScene().getWindow();
            // stage.close();
        }
    }

    // Phương thức trợ giúp để hiển thị hộp thoại thông báo
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Phương thức xử lý khi nhấn nút "X" (đóng ứng dụng/cửa sổ)
    @FXML // Phải có @FXML để FXML có thể gọi phương thức này
    public void closeApplication(ActionEvent event) {
        // Đảm bảo bạn đã import javafx.event.ActionEvent, javafx.scene.Node, javafx.stage.Stage
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
        // Hoặc Platform.exit(); nếu bạn muốn thoát toàn bộ ứng dụng
    }
}