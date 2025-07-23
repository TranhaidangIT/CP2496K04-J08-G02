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
import models.Room; // Đảm bảo lớp Room đã được định nghĩa đúng

import java.io.IOException;

/**
 * Lớp điều khiển cho màn hình Quản lý phòng chiếu.
 * Xử lý các tương tác UI và điều hướng.
 */
public class ProjectionRoomController {

    // --- Các thành phần UI được inject từ FXML ---

    // Sidebar navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button movieShowtimeBtn;
    @FXML private Button ticketsForSaleBtn;
    @FXML private Button projectionRoomBtn;
    @FXML private Button userManageBtn;
    @FXML private Button logoutBtn;

    // Search input fields
    @FXML private TextField searchField;
    @FXML private TextField typeSearchField;

    // Room data table and its columns
    @FXML private TableView<Room> roomTable;
    @FXML private TableColumn<Room, String> idCol; // Giả sử ID phòng là String (ví dụ: P01)
    @FXML private TableColumn<Room, String> roomNameCol;
    @FXML private TableColumn<Room, String> roomTypeCol;
    @FXML private TableColumn<Room, Integer> numSeatsCol;
    @FXML private TableColumn<Room, String> statusCol;
    @FXML private TableColumn<Room, Void> editCol;   // Cột cho các nút sửa
    @FXML private TableColumn<Room, Void> deleteCol; // Cột cho các nút xóa

    // Input fields for room details (for adding new or editing existing rooms)
    @FXML private TextField roomNameField;
    @FXML private TextField roomTypeField;
    @FXML private TextField totalSeatsField;
    @FXML private TextField statusField;

    // Action buttons for managing room data
    @FXML private Button addButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    // Danh sách quan sát (ObservableList) cho dữ liệu phòng chiếu.
    private ObservableList<Room> roomData = FXCollections.observableArrayList();

    /**
     * Khởi tạo bộ điều khiển sau khi phần tử gốc của nó đã được xử lý hoàn toàn.
     * Phương thức này được FXMLLoader tự động gọi.
     */
    @FXML
    public void initialize() {
        // Cấu hình PropertyValueFactory cho các cột dữ liệu.
        // Điều này giúp TableView biết cách liên kết thuộc tính từ đối tượng Room với từng cột.
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        roomNameCol.setCellValueFactory(new PropertyValueFactory<>("roomName"));
        roomTypeCol.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        numSeatsCol.setCellValueFactory(new PropertyValueFactory<>("numSeats"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Cấu hình cột nút "Sửa"
        Callback<TableColumn<Room, Void>, TableCell<Room, Void>> editCellFactory = new Callback<TableColumn<Room, Void>, TableCell<Room, Void>>() {
            @Override
            public TableCell<Room, Void> call(final TableColumn<Room, Void> param) {
                final TableCell<Room, Void> cell = new TableCell<Room, Void>() {
                    private final Button btn = new Button("Edit");
                    {
                        btn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black;");
                        btn.setPrefWidth(60);
                        btn.setOnAction((ActionEvent event) -> {
                            Room room = getTableView().getItems().get(getIndex());
                            System.out.println("Nút Sửa được nhấp cho phòng: " + room.getRoomName());
                            displayRoomDetails(room);
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

        // Cấu hình cột nút "Xóa"
        Callback<TableColumn<Room, Void>, TableCell<Room, Void>> deleteCellFactory = new Callback<TableColumn<Room, Void>, TableCell<Room, Void>>() {
            @Override
            public TableCell<Room, Void> call(final TableColumn<Room, Void> param) {
                final TableCell<Room, Void> cell = new TableCell<Room, Void>() {
                    private final Button btn = new Button("Delete");
                    {
                        btn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                        btn.setPrefWidth(60);
                        btn.setOnAction((ActionEvent event) -> {
                            Room room = getTableView().getItems().get(getIndex());
                            System.out.println("Nút Xóa được nhấp cho phòng: " + room.getRoomName());
                            // Phát triển trong tương lai: Thêm logic xóa phòng (ví dụ: khỏi CSDL và khỏi roomData).
                            // roomData.remove(room);
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
        deleteCol.setCellFactory(deleteCellFactory); // Gán CellFactory cho cột "Xóa".

        // Thêm dữ liệu mẫu vào bảng (dựa trên hình ảnh bạn cung cấp)
        roomData.add(new Room("P01", "Room 01", "2D", 20, "Active"));
        // Thêm các hàng trống để làm cho bảng trông đầy đủ hơn như trong ảnh
        for (int i = 0; i < 10; i++) { // Thêm 10 hàng trống để mô phỏng
            // roomData.add(new Room("", "", "", 0, "")); // Hoặc bạn có thể tạo đối tượng Room rỗng
        }

        roomTable.setItems(roomData);

        // Khởi tạo các trường nhập liệu để được xóa khi khởi động.
        clearRoomDetails();
    }

    /**
     * Xóa tất cả các trường nhập liệu văn bản trong phần chi tiết phòng chiếu.
     */
    private void clearRoomDetails() {
        roomNameField.clear();
        roomTypeField.clear();
        totalSeatsField.clear();
        statusField.clear();
    }

    /**
     * Hiển thị chi tiết của một phòng chiếu vào các trường nhập liệu.
     * @param room Đối tượng Room để hiển thị.
     */
    private void displayRoomDetails(Room room) {
        roomNameField.setText(room.getRoomName());
        roomTypeField.setText(room.getRoomType());
        totalSeatsField.setText(String.valueOf(room.getNumSeats()));
        statusField.setText(room.getStatus());
    }

    /**
     * Xử lý sự kiện nhấp chuột vào nút 'Add'.
     * Xóa các trường nhập liệu, chuẩn bị cho việc nhập dữ liệu phòng mới.
     */
    @FXML
    private void handleAddButton() {
        clearRoomDetails();
        System.out.println("Nút 'Add' đã được nhấp. Các trường nhập liệu đã được xóa.");
        // Phát triển trong tương lai: Có thể đặt trạng thái để biết đang thêm mới.
    }

    /**
     * Xử lý sự kiện nhấp chuột vào nút 'Save'.
     * Thu thập dữ liệu từ các trường nhập liệu.
     */
    @FXML
    private void handleSaveButton() {
        System.out.println("Nút 'Save' đã được nhấp.");
        // Phát triển trong tương lai:
        // 1. Validate input data.
        // 2. Collect data: roomNameField.getText(), roomTypeField.getText(), etc.
        // 3. Call a service layer to save/update room in a database.
        // 4. Refresh table data after save.
        // 5. Provide feedback to the user (e.g., success/error message).

        // Ví dụ: In dữ liệu đã thu thập ra console
        String roomName = roomNameField.getText();
        String roomType = roomTypeField.getText();
        int totalSeats = 0;
        try {
            totalSeats = Integer.parseInt(totalSeatsField.getText());
        } catch (NumberFormatException e) {
            System.err.println("Lỗi: Số lượng ghế không hợp lệ.");
            // Hiển thị thông báo lỗi cho người dùng.
            return;
        }
        String status = statusField.getText();
        System.out.println("Dữ liệu đã thu thập: Room Name=" + roomName + ", Room Type=" + roomType + ", Total Seats=" + totalSeats + ", Status=" + status);

        // Trong trường hợp này, chúng ta sẽ thêm một phòng mới vào ObservableList (để thấy trên UI)
        // Trong ứng dụng thực tế, bạn sẽ gửi dữ liệu này đến DAO/Database.
        roomData.add(new Room("P" + (roomData.size() + 1), roomName, roomType, totalSeats, status));
        clearRoomDetails(); // Xóa các trường sau khi lưu
    }

    /**
     * Xử lý sự kiện nhấp chuột vào nút 'Cancel'.
     * Xóa các trường nhập liệu và loại bỏ mọi thay đổi chưa lưu.
     */
    @FXML
    private void handleCancelButton() {
        clearRoomDetails();
        System.out.println("Nút 'Cancel' đã được nhấp. Các trường nhập liệu đã được xóa.");
        // Phát triển trong tương lai: Hoàn tác trạng thái biểu mẫu nếu đang chỉnh sửa phòng hiện có.
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
     * Điều hướng đến màn hình Projection Room.
     * @param event ActionEvent từ nút Projection Room.
     */
    @FXML
    private void handleProjectionRoomButton(ActionEvent event) {
        System.out.println("Đã ở màn hình Quản lý phòng chiếu.");
    }

    /**
     * Điều hướng đến màn hình Quản lý người dùng.
     * @param event ActionEvent từ nút Quản lý người dùng.
     */
    @FXML
    private void handleUserManageButton(ActionEvent event) {
        switchScreen(event, "/fxml_Admin/ManageUser.fxml", "Quản lý người dùng", 1229, 768);
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