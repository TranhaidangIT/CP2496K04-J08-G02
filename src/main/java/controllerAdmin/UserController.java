package controllerAdmin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import models.User; // Đảm bảo lớp User đã được định nghĩa đúng

import java.io.IOException;

/**
 * Lớp điều khiển cho màn hình Quản lý người dùng.
 * Xử lý các tương tác UI và điều hướng.
 */
public class UserController {

    // --- Các thành phần UI được inject từ FXML ---

    // Sidebar navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button movieShowtimeBtn;
    @FXML private Button ticketsForSaleBtn;
    @FXML private Button projectionRoomBtn;
    @FXML private Button userManageBtn;
    @FXML private Button logoutBtn;

    // Search input field
    @FXML private TextField searchField;

    // User data table and its columns
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idCol;
    @FXML private TableColumn<User, String> fullNameCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private TableColumn<User, Void> deleteCol; // Cột cho các nút xóa
    @FXML private TableColumn<User, Void> editCol;   // Cột cho các nút sửa

    // Input fields for user details (for adding new or editing existing users)
    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private TextField nameField;
    @FXML private TextField roleField;

    // Action buttons for managing user data
    @FXML private Button addNewButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    // Danh sách quan sát (ObservableList) cho dữ liệu người dùng.
    // Sẽ được khai báo nhưng KHÔNG điền dữ liệu mẫu vào lúc này.
    private ObservableList<User> userData = FXCollections.observableArrayList();

    /**
     * Khởi tạo bộ điều khiển sau khi phần tử gốc của nó đã được xử lý hoàn toàn.
     * Phương thức này được FXMLLoader tự động gọi.
     */
    @FXML
    public void initialize() {
        // Cấu hình PropertyValueFactory cho các cột dữ liệu.
        // Điều này giúp TableView biết cách liên kết thuộc tính từ đối tượng User với từng cột.
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Cấu hình cột nút "Xóa"
        // Tạo một CellFactory để tùy chỉnh cách hiển thị mỗi ô trong cột này.
        Callback<TableColumn<User, Void>, TableCell<User, Void>> deleteCellFactory = new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                final TableCell<User, Void> cell = new TableCell<User, Void>() {
                    private final Button btn = new Button("Xóa");
                    {
                        // Áp dụng kiểu dáng và kích thước cho nút.
                        btn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                        btn.setPrefWidth(60);
                        // Đặt hành động khi nút được nhấp.
                        btn.setOnAction((ActionEvent event) -> {
                            // Lấy đối tượng User tương ứng với hàng này.
                            // Lưu ý: Hiện tại sẽ không có dữ liệu để lấy, nhưng cấu trúc này là đúng.
                            User user = getTableView().getItems().get(getIndex());
                            System.out.println("Nút Xóa được nhấp cho người dùng: " + user.getFullName());
                            // Phát triển trong tương lai: Thêm logic xóa người dùng (ví dụ: khỏi CSDL và khỏi userData).
                            // userData.remove(user);
                        });
                    }

                    // Cập nhật giao diện của ô.
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null); // Không hiển thị gì nếu ô trống.
                        } else {
                            setGraphic(btn); // Hiển thị nút nếu ô có dữ liệu.
                        }
                    }
                };
                return cell;
            }
        };
        deleteCol.setCellFactory(deleteCellFactory); // Gán CellFactory cho cột "Xóa".

        // Cấu hình cột nút "Sửa" (tương tự cột "Xóa")
        Callback<TableColumn<User, Void>, TableCell<User, Void>> editCellFactory = new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                final TableCell<User, Void> cell = new TableCell<User, Void>() {
                    private final Button btn = new Button("Sửa");
                    {
                        btn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black;");
                        btn.setPrefWidth(60);
                        btn.setOnAction((ActionEvent event) -> {
                            User user = getTableView().getItems().get(getIndex());
                            System.out.println("Nút Sửa được nhấp cho người dùng: " + user.getFullName());
                            // Phát triển trong tương lai: Tải chi tiết người dùng vào các trường nhập liệu để chỉnh sửa.
                            // displayUserDetails(user);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
                return cell;
            }
        };
        editCol.setCellFactory(editCellFactory); // Gán CellFactory cho cột "Sửa".

        // Gán danh sách trống cho TableView.
        // Điều này đảm bảo các cột hiển thị, nhưng không có hàng dữ liệu.
        userTable.setItems(userData); // userData là ObservableList rỗng.

        // Khởi tạo các trường nhập liệu để được xóa khi khởi động.
        clearUserDetails();

        // Gắn các trình xử lý sự kiện vào các nút hành động.
        if (addNewButton != null) {
            addNewButton.setOnAction(event -> handleAddNewButton());
        }
        if (saveButton != null) {
            saveButton.setOnAction(event -> handleSaveButton());
        }
        if (cancelButton != null) {
            cancelButton.setOnAction(event -> handleCancelButton());
        }
    }

    /**
     * Xóa tất cả các trường nhập liệu văn bản trong phần chi tiết người dùng.
     */
    private void clearUserDetails() {
        usernameField.clear();
        passwordField.clear();
        nameField.clear();
        roleField.clear();
    }

    /**
     * Xử lý sự kiện nhấp chuột vào nút 'Thêm mới'.
     * Xóa các trường nhập liệu, chuẩn bị cho việc nhập dữ liệu người dùng mới.
     */
    @FXML
    private void handleAddNewButton() {
        clearUserDetails();
        System.out.println("Nút 'Thêm mới người dùng' đã được nhấp. Các trường nhập liệu đã được xóa.");
        // Phát triển trong tương lai: Bật/tắt các nút Lưu/Hủy, đặt trạng thái biểu mẫu thành 'thêm mới'.
    }

    /**
     * Xử lý sự kiện nhấp chuột vào nút 'Lưu'.
     * Thu thập dữ liệu từ các trường nhập liệu.
     */
    @FXML
    private void handleSaveButton() {
        System.out.println("Nút 'Lưu' đã được nhấp.");
        // Phát triển trong tương lai:
        // 1. Xác thực dữ liệu đầu vào.
        // 2. Thu thập dữ liệu: usernameField.getText(), passwordField.getText(), v.v.
        // 3. Gọi một lớp dịch vụ để lưu/cập nhật người dùng trong cơ sở dữ liệu.
        // 4. Làm mới dữ liệu bảng sau khi lưu.
        // 5. Cung cấp phản hồi cho người dùng (ví dụ: thông báo thành công/lỗi).

        // Ví dụ: In dữ liệu đã thu thập ra console
        String username = usernameField.getText();
        String password = passwordField.getText(); // Lưu ý: Xử lý mật khẩu an toàn (hashing) trong các ứng dụng thực tế
        String name = nameField.getText();
        String role = roleField.getText();
        System.out.println("Dữ liệu đã thu thập: Tên người dùng=" + username + ", Mật khẩu=" + password + ", Tên=" + name + ", Vai trò=" + role);
    }

    /**
     * Xử lý sự kiện nhấp chuột vào nút 'Hủy'.
     * Xóa các trường nhập liệu và loại bỏ mọi thay đổi chưa lưu.
     */
    @FXML
    private void handleCancelButton() {
        clearUserDetails();
        System.out.println("Nút 'Hủy' đã được nhấp. Các trường nhập liệu đã được xóa.");
        // Phát triển trong tương lai: Hoàn tác trạng thái biểu mẫu nếu đang chỉnh sửa người dùng hiện có.
    }

    // --- Phương thức điều hướng thanh bên ---

    /**
     * Phương thức chung để chuyển đổi giữa các màn hình FXML khác nhau.
     * @param event ActionEvent từ việc nhấp nút.
     * @param fxmlPath Đường dẫn đến tệp FXML đích (ví dụ: "/fxml/Dashboard.fxml").
     * @param title Tiêu đề cho cửa sổ mới.
     * @param width Chiều rộng mong muốn của cảnh mới.
     * @param height Chiều cao mong muốn của cảnh mới.
     */
    private void switchScreen(ActionEvent event, String fxmlPath, String title, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Lỗi khi chuyển màn hình sang " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace(); // In toàn bộ stack trace để gỡ lỗi
        }
    }

    /**
     * Điều hướng đến màn hình Dashboard.
     * @param event ActionEvent từ nút Dashboard.
     */
    @FXML
    private void handleDashboardButton(ActionEvent event) {
        switchScreen(event, "/fxml_Admin/Dashboard.fxml", "Bảng điều khiển Admin", 1229, 768);
    }

    /**
     * Xử lý điều hướng đến màn hình Movie/Showtime (placeholder).
     * @param event ActionEvent từ nút Movie/Showtime.
     */
    @FXML
    private void handleMovieShowtimeButton(ActionEvent event) {
        System.out.println("Điều hướng đến màn hình Movie / Showtime (Chưa triển khai).");
        // Phát triển trong tương lai: switchScreen(event, "/fxml/MovieShowtime.fxml", "Quản lý suất chiếu phim", 1229, 768);
    }

    /**
     * Điều hướng đến màn hình Tickets for Sale.
     * @param event ActionEvent từ nút Tickets for Sale.
     */
    @FXML
    private void handleTicketsForSaleButton(ActionEvent event) {
        switchScreen(event, "/fxml_Admin/TicketsForSale.fxml", "Quản lý vé bán", 1229, 768);
    }

    /**
     * Xử lý điều hướng đến màn hình Projection Room (placeholder).
     * @param event ActionEvent từ nút Projection Room.
     */
    @FXML
    private void handleProjectionRoomButton(ActionEvent event) {
        System.out.println("Điều hướng đến màn hình Projection Room (Chưa triển khai).");
        // Phát triển trong tương lai: switchScreen(event, "/fxml/ProjectionRoom.fxml", "Quản lý phòng chiếu", 1229, 768);
    }

    /**
     * Xử lý điều hướng đến màn hình Quản lý người dùng.
     * Phương thức này được kích hoạt khi đang ở trên màn hình Quản lý người dùng.
     * @param event ActionEvent từ nút Quản lý người dùng.
     */
    @FXML
    private void handleUserManageButton(ActionEvent event) {
        System.out.println("Đã ở màn hình Quản lý người dùng.");
    }

    /**
     * Điều hướng đến màn hình Đăng nhập.
     * @param event ActionEvent từ nút Đăng xuất.
     */
    @FXML
    private void handleLogoutButton(ActionEvent event) {
        switchScreen(event, "/fxml_Admin/Login.fxml", "Đăng nhập", 600, 400);
    }
}