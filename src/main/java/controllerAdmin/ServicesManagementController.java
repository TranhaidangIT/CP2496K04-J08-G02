package controllerAdmin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import models.Service; // Đảm bảo lớp Service đã được định nghĩa đúng

import java.io.IOException;
import java.util.Optional;

/**
 * Lớp điều khiển cho màn hình Quản lý dịch vụ.
 * Xử lý các tương tác UI và điều hướng.
 */
public class ServicesManagementController {

    // --- Các thành phần UI được inject từ FXML ---

    // Sidebar navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button movieShowtimeBtn;
    @FXML private Button servicesManagementBtn;
    @FXML private Button ticketsForSaleBtn;
    @FXML private Button projectionRoomBtn;
    @FXML private Button userManageBtn;
    @FXML private Button logoutBtn;

    // Search input fields
    @FXML private TextField searchField;

    // Service data table and its columns
    @FXML private TableView<Service> serviceTable;
    @FXML private TableColumn<Service, String> idCol;
    @FXML private TableColumn<Service, String> serviceNameCol;
    @FXML private TableColumn<Service, Double> priceCol;
    @FXML private TableColumn<Service, Integer> quantityCol;
    @FXML private TableColumn<Service, Double> totalCol;
    @FXML private TableColumn<Service, String> categoryCol;
    @FXML private TableColumn<Service, Void> editCol;
    @FXML private TableColumn<Service, Void> deleteCol;

    // Input fields for service details
    @FXML private TextField serviceNameField;
    @FXML private TextField priceField;
    @FXML private TextField categoryField;
    @FXML private TextField quantityField;

    // Action buttons for managing service data
    @FXML private Button addButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    // Danh sách quan sát (ObservableList) cho dữ liệu dịch vụ.
    private ObservableList<Service> serviceData = FXCollections.observableArrayList();

    // Biến để lưu trữ dịch vụ đang được chọn/chỉnh sửa
    private Service selectedService;
    // Biến để theo dõi đang ở chế độ thêm mới hay chỉnh sửa
    private boolean isAddingNew = false;

    /**
     * Khởi tạo bộ điều khiển sau khi phần tử gốc của nó đã được xử lý hoàn toàn.
     * Phương thức này được FXMLLoader tự động gọi.
     */
    @FXML
    public void initialize() {
        // Cấu hình PropertyValueFactory cho các cột dữ liệu.
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        serviceNameCol.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total")); // Total sẽ được tính trong model
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        // Cấu hình cột nút "Sửa"
        Callback<TableColumn<Service, Void>, TableCell<Service, Void>> editCellFactory = new Callback<TableColumn<Service, Void>, TableCell<Service, Void>>() {
            @Override
            public TableCell<Service, Void> call(final TableColumn<Service, Void> param) {
                final TableCell<Service, Void> cell = new TableCell<Service, Void>() {
                    private final Button btn = new Button("Edit");
                    {
                        btn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black;");
                        btn.setPrefWidth(60);
                        btn.setOnAction((ActionEvent event) -> {
                            selectedService = getTableView().getItems().get(getIndex());
                            isAddingNew = false; // Chuyển sang chế độ chỉnh sửa
                            System.out.println("Nút Sửa được nhấp cho dịch vụ: " + selectedService.getServiceName());
                            displayServiceDetails(selectedService);
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
        editCol.setCellFactory(editCellFactory);

        // Cấu hình cột nút "Xóa"
        Callback<TableColumn<Service, Void>, TableCell<Service, Void>> deleteCellFactory = new Callback<TableColumn<Service, Void>, TableCell<Service, Void>>() {
            @Override
            public TableCell<Service, Void> call(final TableColumn<Service, Void> param) {
                final TableCell<Service, Void> cell = new TableCell<Service, Void>() {
                    private final Button btn = new Button("Delete");
                    {
                        btn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                        btn.setPrefWidth(60);
                        btn.setOnAction((ActionEvent event) -> {
                            Service serviceToDelete = getTableView().getItems().get(getIndex());
                            System.out.println("Nút Xóa được nhấp cho dịch vụ: " + serviceToDelete.getServiceName());
                            handleDeleteService(serviceToDelete);
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
        deleteCol.setCellFactory(deleteCellFactory);

        // Thêm dữ liệu mẫu vào bảng (dựa trên hình ảnh bạn cung cấp)
        serviceData.add(new Service("DV1", "Cake + Water", 20000.0, 50, "Combo"));
        serviceData.add(new Service("DV2", "Cocacola", 10000.0, 50, "Beverage"));
        // Thêm các hàng trống để làm cho bảng trông đầy đủ hơn
        for (int i = 0; i < 8; i++) {
            // serviceData.add(new Service("DV" + (i + 3), "", 0.0, 0, "")); // Có thể thêm hàng rỗng nếu cần
        }

        serviceTable.setItems(serviceData);

        // Khởi tạo các trường nhập liệu
        clearServiceDetails();
    }

    /**
     * Xóa tất cả các trường nhập liệu văn bản trong phần chi tiết dịch vụ.
     */
    private void clearServiceDetails() {
        serviceNameField.clear();
        priceField.clear();
        categoryField.clear();
        quantityField.clear();
        selectedService = null; // Đặt lại dịch vụ đang chọn
        isAddingNew = false; // Mặc định không ở chế độ thêm mới
    }

    /**
     * Hiển thị chi tiết của một dịch vụ vào các trường nhập liệu.
     * @param service Đối tượng Service để hiển thị.
     */
    private void displayServiceDetails(Service service) {
        if (service != null) {
            serviceNameField.setText(service.getServiceName());
            priceField.setText(String.valueOf(service.getPrice()));
            categoryField.setText(service.getCategory());
            quantityField.setText(String.valueOf(service.getQuantity()));
        } else {
            clearServiceDetails();
        }
    }

    /**
     * Xử lý sự kiện nhấp chuột vào nút 'Add'.
     * Xóa các trường nhập liệu và đặt trạng thái thêm mới.
     */
    @FXML
    private void handleAddButton() {
        clearServiceDetails();
        isAddingNew = true; // Đặt trạng thái là đang thêm mới
        serviceNameField.requestFocus(); // Đặt con trỏ vào trường đầu tiên
        System.out.println("Nút 'Add' đã được nhấp. Các trường nhập liệu đã được xóa và sẵn sàng cho dịch vụ mới.");
    }

    /**
     * Xử lý sự kiện nhấp chuột vào nút 'Save'.
     * Thu thập dữ liệu từ các trường nhập liệu và lưu/cập nhật dịch vụ.
     */
    @FXML
    private void handleSaveButton() {
        System.out.println("Nút 'Save' đã được nhấp.");

        String serviceName = serviceNameField.getText();
        String category = categoryField.getText();

        if (serviceName.isEmpty() || priceField.getText().isEmpty() || category.isEmpty() || quantityField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi đầu vào", "Vui lòng điền đầy đủ tất cả các trường.");
            return;
        }

        double price;
        int quantity;
        try {
            price = Double.parseDouble(priceField.getText());
            if (price < 0) {
                showAlert(Alert.AlertType.ERROR, "Lỗi đầu vào", "Giá phải là một số không âm.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi đầu vào", "Giá không hợp lệ. Vui lòng nhập một số.");
            return;
        }

        try {
            quantity = Integer.parseInt(quantityField.getText());
            if (quantity < 0) {
                showAlert(Alert.AlertType.ERROR, "Lỗi đầu vào", "Số lượng phải là một số nguyên không âm.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi đầu vào", "Số lượng không hợp lệ. Vui lòng nhập một số nguyên.");
            return;
        }


        if (isAddingNew) {
            // Chế độ thêm mới
            String newId = generateNewServiceId(); // Tạo ID mới tự động
            Service newService = new Service(newId, serviceName, price, quantity, category);
            serviceData.add(newService);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm dịch vụ mới: " + serviceName);
        } else {
            // Chế độ chỉnh sửa
            if (selectedService != null) {
                selectedService.setServiceName(serviceName);
                selectedService.setPrice(price);
                selectedService.setQuantity(quantity);
                selectedService.setCategory(category);
                // Cần làm mới bảng để hiển thị các thay đổi
                serviceTable.refresh();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật dịch vụ: " + serviceName);
            } else {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có dịch vụ nào được chọn để lưu. Vui lòng chọn một dịch vụ hoặc nhấn 'Add' để thêm mới.");
                return;
            }
        }
        clearServiceDetails(); // Xóa các trường sau khi lưu/cập nhật
    }

    /**
     * Tạo ID dịch vụ mới đơn giản (trong ứng dụng thực tế nên dùng UUID hoặc ID từ CSDL).
     * @return ID dịch vụ mới.
     */
    private String generateNewServiceId() {
        // Tìm số lớn nhất hiện có và tăng lên 1
        int maxIdNum = 0;
        for (Service service : serviceData) {
            String idStr = service.getId();
            if (idStr != null && idStr.startsWith("DV")) {
                try {
                    int num = Integer.parseInt(idStr.substring(2));
                    if (num > maxIdNum) {
                        maxIdNum = num;
                    }
                } catch (NumberFormatException e) {
                    // Bỏ qua ID không hợp lệ
                }
            }
        }
        return String.format("DV%d", maxIdNum + 1);
    }

    /**
     * Xử lý sự kiện nhấp chuột vào nút 'Cancel'.
     * Xóa các trường nhập liệu và loại bỏ mọi thay đổi chưa lưu.
     */
    @FXML
    private void handleCancelButton() {
        clearServiceDetails();
        System.out.println("Nút 'Cancel' đã được nhấp. Các trường nhập liệu đã được xóa.");
    }

    /**
     * Xử lý việc xóa một dịch vụ sau khi người dùng xác nhận.
     * @param serviceToDelete Dịch vụ cần xóa.
     */
    private void handleDeleteService(Service serviceToDelete) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa dịch vụ này?");
        alert.setContentText("Dịch vụ: " + serviceToDelete.getServiceName() + " (ID: " + serviceToDelete.getId() + ") sẽ bị xóa vĩnh viễn.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            serviceData.remove(serviceToDelete);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa dịch vụ: " + serviceToDelete.getServiceName());
            clearServiceDetails(); // Xóa các trường nếu dịch vụ đang được chỉnh sửa bị xóa
        } else {
            System.out.println("Hủy xóa dịch vụ: " + serviceToDelete.getServiceName());
        }
    }

    /**
     * Hiển thị một hộp thoại thông báo.
     * @param alertType Kiểu thông báo (INFORMATION, WARNING, ERROR, CONFIRMATION).
     * @param title Tiêu đề của hộp thoại.
     * @param message Nội dung thông báo.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- Phương thức điều hướng thanh bên ---

    /**
     * Phương thức chung để chuyển đổi giữa các màn hình FXML khác nhau.
     * @param event ActionEvent từ việc nhấp nút.
     * @param fxmlPath Đường dẫn đến tệp FXML đích.
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
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDashboardButton(ActionEvent event) {
        switchScreen(event, "/fxml_Admin/Dashboard.fxml", "Bảng điều khiển Admin", 1229, 768);
    }

    @FXML
    private void handleMovieShowtimeButton(ActionEvent event) {
        System.out.println("Điều hướng đến màn hình Movie / Showtime (Chưa triển khai).");
    }

    @FXML
    private void handleServicesManagementButton(ActionEvent event) {
        System.out.println("Đã ở màn hình Quản lý dịch vụ.");
    }

    @FXML
    private void handleTicketsForSaleButton(ActionEvent event) {
        switchScreen(event, "/fxml_Admin/TicketsForSale.fxml", "Quản lý vé bán", 1229, 768);
    }

    @FXML
    private void handleProjectionRoomButton(ActionEvent event) {
        switchScreen(event, "/fxml_Admin/ProjectionRoom.fxml", "Quản lý phòng chiếu", 1229, 768);
    }

    @FXML
    private void handleUserManageButton(ActionEvent event) {
        switchScreen(event, "/fxml_Admin/ManageUser.fxml", "Quản lý người dùng", 1229, 768);
    }

    @FXML
    private void handleLogoutButton(ActionEvent event) {
        switchScreen(event, "/fxml_Admin/Login.fxml", "Đăng nhập", 600, 400);
    }
}