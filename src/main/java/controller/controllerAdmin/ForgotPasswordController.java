package controller.controllerAdmin;

import dao.UserDAO; // Lớp dùng để thao tác với dữ liệu người dùng
import javafx.fxml.FXML; // Annotation cho các thành phần FXML
import javafx.scene.control.*; // Các control UI như TextField, PasswordField, Alert
import utils.PasswordUtil; // Lớp tiện ích để mã hóa mật khẩu

/**
 * Controller xử lý giao diện Quên mật khẩu bằng email.
 */
public class ForgotPasswordController {

    @FXML
    private TextField emailField; // Trường nhập email

    @FXML
    private PasswordField newPasswordField; // Trường nhập mật khẩu mới

    @FXML
    private PasswordField confirmPasswordField; // Trường xác nhận mật khẩu mới

    /**
     * Xử lý khi người dùng bấm nút "Reset Password".
     */
    @FXML
    private void handleResetPassword() {
        // Lấy dữ liệu từ các trường nhập liệu
        String email = emailField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        // Kiểm tra các trường có rỗng không
        if (email.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        // Kiểm tra mật khẩu mới và mật khẩu xác nhận có khớp nhau không
        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu xác nhận không khớp.");
            return;
        }

        // Kiểm tra độ dài tối thiểu của mật khẩu
        if (newPassword.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu phải từ 6 ký tự trở lên.");
            return;
        }

        // Mã hóa mật khẩu và gọi phương thức DAO để cập nhật mật khẩu theo email
        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        boolean success = UserDAO.updatePasswordByEmail(email, hashedPassword);

        // Hiển thị kết quả cho người dùng
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Mật khẩu đã được cập nhật.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Không thành công", "Không tìm thấy email đăng ký.");
        }
    }

    /**
     * Phương thức tiện ích để hiển thị các hộp thoại cảnh báo/thông báo.
     * @param type Kiểu cảnh báo (INFORMATION, ERROR, WARNING)
     * @param title Tiêu đề của hộp thoại
     * @param message Nội dung thông báo
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}