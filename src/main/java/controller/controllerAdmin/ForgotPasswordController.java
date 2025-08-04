package controller.controllerAdmin;

import dao.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ForgotPasswordController {

    @FXML
    private AnchorPane rootPane;

    // Các trường cho trang xác minh (Trang 2)
    @FXML
    private TextField employeeIdField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;

    // Các trường cho trang đặt lại mật khẩu (Trang 3)
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;

    private String userEmail; // Biến tạm để lưu email đã xác minh

    // Phương thức xử lý nút "Xác minh" ở Trang 2
    @FXML
    private void handleVerifyUser(ActionEvent event) {
        String employeeId = employeeIdField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();

        // Kiểm tra các trường có rỗng không
        if (employeeId.isEmpty() || username.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        // Gọi DAO để kiểm tra sự tồn tại của người dùng
        if (UserDAO.isUserExists(employeeId, username, email)) {
            // Nếu xác minh thành công, lưu email và chuyển sang Trang 3
            this.userEmail = email;
            loadResetPasswordPage(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Thông tin không khớp. Vui lòng kiểm tra lại.");
        }
    }

    // Phương thức xử lý nút "Xác nhận" ở Trang 3
    @FXML
    private void handleResetPassword() {
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập mật khẩu mới và xác nhận.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu xác nhận không khớp.");
            return;
        }

        if (newPassword.length() < 3) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu phải từ 3 ký tự trở lên.");
            return;
        }

        // Cập nhật mật khẩu trong cơ sở dữ liệu
        if (UserDAO.updatePasswordByEmail(this.userEmail, newPassword)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Mật khẩu đã được cập nhật.");
            // Quay lại màn hình đăng nhập
            Stage stage = (Stage) rootPane.getScene().getWindow();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Admin/Login.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Đăng nhập");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Không thành công", "Đã xảy ra lỗi khi cập nhật mật khẩu.");
        }
    }

    // Phương thức giúp chuyển từ Trang 2 sang Trang 3
    private void loadResetPasswordPage(ActionEvent event) {
        try {
            // Lấy Stage từ sự kiện của nút bấm
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Admin/ForgotPassword_Step3.fxml"));
            Parent page = loader.load();

            // Lấy controller của trang mới và truyền dữ liệu
            ForgotPasswordController controller = loader.getController();
            controller.userEmail = this.userEmail;

            // Hiển thị email đã được xác minh trên trang mới
            controller.emailField.setText(this.userEmail);
            controller.emailField.setDisable(true); // Ngăn không cho người dùng sửa email

            // Chuyển màn hình
            Scene scene = new Scene(page);
            stage.setScene(scene);
            stage.setTitle("Đặt lại mật khẩu");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải trang đặt lại mật khẩu.");
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