package controller.controllerAdmin;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.event.ActionEvent; // Cần import ActionEvent
import javafx.scene.Parent;      // Cần import Parent
import javafx.scene.Scene;       // Cần import Scene
import javafx.stage.Stage;       // Cần import Stage
import javafx.fxml.FXMLLoader;   // Cần import FXMLLoader
import javafx.scene.Node;        // Cần import Node

import java.io.IOException; // Cần import IOException cho FXMLLoader.load

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if ("admin".equals(username) && "admin".equals(password)) {
            showAlert(AlertType.INFORMATION, "Login Successful", "Welcome, " + username + "!");
            // Ở đây bạn có thể thêm logic để chuyển sang màn hình chính của ứng dụng
        } else {
            showAlert(AlertType.ERROR, "Login Failed", "Invalid username or password.");
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // PHƯƠNG THỨC MỚI ĐỂ XỬ LÝ CHUYỂN SANG MÀN HÌNH QUÊN MẬT KHẨU
    @FXML
    private void handleForgotPassword(ActionEvent event) {
        try {
            // Tải FXML cho màn hình Forgot Password
            Parent forgotPasswordRoot = FXMLLoader.load(getClass().getResource("/views/fxml_Admin/forgotPassword.fxml"));

            // Lấy Stage hiện tại từ sự kiện (nút hoặc hyperlink đã được nhấp)
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Đặt Scene mới cho Stage hiện tại
            currentStage.setScene(new Scene(forgotPasswordRoot));
            currentStage.setTitle("Quên Mật Khẩu"); // Cập nhật tiêu đề cửa sổ
            currentStage.show(); // Hiển thị màn hình mới
            currentStage.setResizable(false); // Giữ không cho thay đổi kích thước
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Lỗi tải giao diện", "Không thể tải màn hình Quên Mật Khẩu.");
        }
    }
}