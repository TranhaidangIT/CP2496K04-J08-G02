package controller.controllerAdmin;

import dao.UserDAO;
import models.User;
import utils.Session;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;

/**
 * Controller xử lý đăng nhập cho 3 vai trò: admin, manage, employee.
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    /**
     * Xử lý khi nhấn nút Đăng nhập.
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Gọi DAO để kiểm tra username & password
        User user = UserDAO.login(username, password);

        if (user != null) {
            // Lưu thông tin user vào session
            Session.setCurrentUser(user);

            // Hiển thị thông báo
            showAlert(AlertType.INFORMATION, "Thành công", "Chào mừng " + user.getFullName());

            // Chuyển theo vai trò
            switch (user.getRole().toLowerCase()) {
                case "admin":
                    loadScene("/views/AdminDashboard.fxml", event);
                    break;
                case "manager":
                    loadScene("/views/Dashboard.fxml", event);
                    break;
                case "employee":
                    loadScene("/views/EmployeeSidebar.fxml", event);
                    break;
                default:
                    showAlert(AlertType.ERROR, "Lỗi", "Vai trò không xác định.");
            }

        } else {
            showAlert(AlertType.ERROR, "Đăng nhập thất bại", "Sai tài khoản hoặc mật khẩu.");
        }
    }

    /**
     * Hiển thị cảnh báo dạng popup.
     */
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Mở giao diện mới.
     */
    private void loadScene(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải giao diện: " + fxmlPath);
        }
    }

    /**
     * Mở giao diện quên mật khẩu khi nhấn link "Forgot Password?".
     */
    @FXML
    private void handleForgotPassword(ActionEvent event) {
        loadScene("/views/fxml_Admin/ForgotPassword_Step1.fxml", event);
    }
}