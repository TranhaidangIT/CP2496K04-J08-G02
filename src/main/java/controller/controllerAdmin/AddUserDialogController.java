package controller.controllerAdmin;

import models.User;
import dao.UserDAO;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Random;
import java.util.UUID;

public class AddUserDialogController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private ComboBox<String> roleComboBox;

    private Stage dialogStage;
    private boolean saveClicked = false;
    private User newUser;

    // Phương thức này được gọi tự động sau khi FXML đã được tải và các thành phần đã được tiêm
    @FXML
    private void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("Admin", "Manager", "Employee"));
        roleComboBox.getSelectionModel().select("Employee");
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    public User getNewUser() {
        return newUser;
    }

    // Phương thức mới để tạo EmployeeId
    private String generateEmployeeId(String role) {
        String prefix = "";
        switch (role) {
            case "Admin":
                prefix = "ADM";
                break;
            case "Manager":
                prefix = "MNE";
                break;
            case "Employee":
                prefix = "EMP";
                break;
            default:
                String uuid = UUID.randomUUID().toString().replace("-", "");
                return uuid.substring(0, 10).toUpperCase();
        }

        // Tạo phần số ngẫu nhiên không trùng lặp bằng cách gọi UserDAO
        String newId;
        Random random = new Random();
        do {
            // Tạo số ngẫu nhiên
            int number = 100 + random.nextInt(900);
            newId = prefix + number;
        } while (UserDAO.isEmployeeIdExists(newId));

        return newId;
    }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            newUser = new User();

            // Lấy vai trò trước
            String role = roleComboBox.getValue();
            // Tạo mã ID dựa trên vai trò đã chọn
            String employeeId = generateEmployeeId(role);

            newUser.setEmployeeId(employeeId);
            newUser.setUsername(usernameField.getText());
            newUser.setPassword("123");
            newUser.setFullName(nameField.getText());
            newUser.setEmail(emailField.getText());
            newUser.setPhone(phoneField.getText());
            newUser.setRole(role);



            saveClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (usernameField.getText() == null || usernameField.getText().isEmpty()) {
            errorMessage += "Không có tên đăng nhập!\n";
        }
        if (nameField.getText() == null || nameField.getText().isEmpty()) {
            errorMessage += "Không có họ và tên!\n";
        }
        if (emailField.getText() == null || emailField.getText().isEmpty()) {
            errorMessage += "Không có email!\n";
        }
        if (phoneField.getText() == null || phoneField.getText().isEmpty()) {
            errorMessage += "Không có số điện thoại!\n";
        }
        if (roleComboBox.getValue() == null) {
            errorMessage += "Không có vai trò!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Thông tin không hợp lệ");
            alert.setHeaderText("Vui lòng điền đầy đủ các trường");
            alert.setContentText(errorMessage);

            alert.showAndWait();
            return false;
        }
    }
}