package controllerAdmin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Ticket; // Giả định bạn có lớp Ticket trong gói models
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory; // Để liên kết cột với thuộc tính của model

import java.io.IOException;

public class TicketsForSaleController {

    // Sidebar Buttons
    @FXML private javafx.scene.control.Button dashboardBtn;
    @FXML private javafx.scene.control.Button movieShowtimeBtn;
    @FXML private javafx.scene.control.Button ticketsForSaleBtn;
    @FXML private javafx.scene.control.Button projectionRoomBtn;
    @FXML private javafx.scene.control.Button userManageBtn;
    @FXML private javafx.scene.control.Button logoutBtn;

    // Search Fields
    @FXML private TextField searchField;
    @FXML private TextField dateField;
    @FXML private TextField movieField;
    @FXML private TextField statusField;
    @FXML private javafx.scene.control.Button resetButton;

    // TableView and Columns
    @FXML private TableView<Ticket> ticketTable; // Cần thay Ticket bằng lớp model thực tế của bạn
    @FXML private TableColumn<Ticket, Integer> idCol;
    @FXML private TableColumn<Ticket, String> guestNameCol;
    @FXML private TableColumn<Ticket, String> phoneCol;
    @FXML private TableColumn<Ticket, String> movieCol;
    @FXML private TableColumn<Ticket, Integer> rommCol; // Chú ý: trong hình là "Romm", có thể là Room
    @FXML private TableColumn<Ticket, String> showtimeCol;
    @FXML private TableColumn<Ticket, String> seatCol;
    @FXML private TableColumn<Ticket, String> statusCol;

    // Total Labels
    @FXML private Label totalTicketsLabel;
    @FXML private Label totalRevenueLabel;

    // ObservableList để chứa dữ liệu cho TableView
    private ObservableList<Ticket> ticketData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Khởi tạo các cột của TableView với thuộc tính của lớp Ticket
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        guestNameCol.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        movieCol.setCellValueFactory(new PropertyValueFactory<>("movieTitle")); // Giả định thuộc tính trong Ticket là movieTitle
        rommCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber")); // Giả định thuộc tính là roomNumber
        showtimeCol.setCellValueFactory(new PropertyValueFactory<>("showtime"));
        seatCol.setCellValueFactory(new PropertyValueFactory<>("seatNumber"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Load dữ liệu mẫu hoặc từ DB
        loadTicketData();

        // Đặt dữ liệu vào TableView
        ticketTable.setItems(ticketData);

        // Cập nhật tổng số vé và tổng doanh thu
        updateTotals();

        // Xử lý sự kiện cho nút "Làm mới"
        resetButton.setOnAction(event -> handleResetButton());

        // Xử lý sự kiện tìm kiếm (có thể thêm TextPropertyListener cho các TextField)
        // Ví dụ: searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTableData(newVal));
    }

    private void loadTicketData() {
        // --- GIẢ LẬP DỮ LIỆU ---
        // Trong thực tế, bạn sẽ lấy dữ liệu từ DAO (ví dụ: TicketDAO)
        ticketData.add(new Ticket(1, "Tran hai dang", "039...12", "Spiderman", 3, "18:00", "A1", "Printed", 50000.0)); // Thêm giá vé
        ticketData.add(new Ticket(2, "Nguyen Van A", "090...34", "Black Panther", 1, "20:00", "B5", "Pending", 45000.0));
        ticketData.add(new Ticket(3, "Le Thi B", "091...56", "Avatar", 2, "10:30", "C10", "Paid", 60000.0));
        // -----------------------
    }

    private void updateTotals() {
        int totalTickets = ticketData.size();
        double totalRevenue = ticketData.stream().mapToDouble(Ticket::getPrice).sum(); // Giả định Ticket có phương thức getPrice()

        totalTicketsLabel.setText(String.valueOf(totalTickets));
        totalRevenueLabel.setText(String.format("%,.0f VND", totalRevenue)); // Định dạng tiền tệ không có số thập phân
    }

    @FXML
    private void handleResetButton() {
        // Xóa nội dung các trường tìm kiếm
        searchField.clear();
        dateField.clear();
        movieField.clear();
        statusField.clear();
        // Tải lại toàn bộ dữ liệu (hoặc hiển thị lại dữ liệu gốc)
        ticketData.clear();
        loadTicketData();
        updateTotals();
    }

    //region Xử lý các nút sidebar để chuyển màn hình
    private void switchScreen(ActionEvent event, String fxmlPath, String title, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.control.Button) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, width, height); // Tạo Scene mới với kích thước phù hợp
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            // Optional: Show an alert for the error
        }
    }

    @FXML
    private void handleDashboardButton(ActionEvent event) {
        switchScreen(event, "/fxml_Admin/Dashboard.fxml", "Admin Dashboard", 1229, 768);
    }

    @FXML
    private void handleMovieShowtimeButton(ActionEvent event) {
        // Giả sử MovieShowtime.fxml cũng có kích thước tương tự
        // Bạn cần tạo MovieShowtime.fxml và MovieShowtimeController nếu muốn
        // switchScreen(event, "/fxml/MovieShowtime.fxml", "Movie / Showtime", 1229, 768);
        System.out.println("Chuyển đến màn hình Movie / Showtime");
    }

    @FXML
    private void handleTicketsForSaleButton(ActionEvent event) {
        // Bạn đang ở màn hình này, không cần chuyển đổi
        System.out.println("Đã ở màn hình Tickets for sale.");
    }

    @FXML
    private void handleProjectionRoomButton(ActionEvent event) {
        // switchScreen(event, "/fxml/ProjectionRoom.fxml", "Projection Room Management", 1229, 768);
        System.out.println("Chuyển đến màn hình Projection Room");
    }

    @FXML
    private void handleUserManageButton(ActionEvent event) {
        // switchScreen(event, "/fxml/UserManage.fxml", "User Management", 1229, 768);
        System.out.println("Chuyển đến màn hình User Manage");
    }

    @FXML
    private void handleLogoutButton(ActionEvent event) {
        // Chuyển về màn hình đăng nhập (kích thước nhỏ hơn)
        switchScreen(event, "/fxml_Admin/Login.fxml", "Login", 600, 400); // Kích thước ví dụ cho Login
    }
    //endregion
}