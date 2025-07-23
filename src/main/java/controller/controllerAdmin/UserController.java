package controller.controllerAdmin;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
// Không cần các imports liên quan đến Scene, Stage, BorderPane nếu controller này chỉ quản lý nội dung

public class UserController {

    // Các thành phần của User Management (giữ nguyên)
    @FXML private TextField searchField;
    @FXML private TableView<?> userTable; // Dùng ? nếu chưa biết kiểu dữ liệu cụ thể
    @FXML private TableColumn<?, ?> idCol;
    @FXML private TableColumn<?, ?> usernameCol;
    @FXML private TableColumn<?, ?> fullNameCol;
    @FXML private TableColumn<?, ?> roleCol;
    @FXML private TableColumn<?, ?> deleteCol;
    @FXML private TableColumn<?, ?> editCol;
    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private TextField nameField;
    @FXML private TextField roleField;
    @FXML private Button addNewButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    @FXML
    public void initialize() {
        // Logic khởi tạo cho trang User Management (ví dụ: load dữ liệu vào bảng)
        // Đây là nơi bạn sẽ gọi các phương thức để hiển thị dữ liệu ban đầu
        System.out.println("UserManagementContent.fxml initialized.");
    }

    // --- Các phương thức xử lý sự kiện cho nội dung quản lý người dùng ---

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        System.out.println("Searching for: " + keyword);
        // Logic tìm kiếm người dùng và cập nhật TableView ở đây
    }

    @FXML
    private void handleAddNew() {
        System.out.println("Add New User clicked");
        // Logic để chuẩn bị form cho việc thêm người dùng mới ở đây
    }

    @FXML
    private void handleSave() {
        System.out.println("Save User clicked");
        // Logic để lưu thông tin người dùng (thêm mới hoặc cập nhật) ở đây
    }

    @FXML
    private void handleCancel() {
        System.out.println("Cancel clicked");
        // Logic để hủy bỏ thao tác hiện tại và xóa các trường form ở đây
    }

    // Các phương thức cho việc xử lý nút Edit/Delete trong TableView
    // (sẽ được implement thông qua CellFactory)
}