package controller.controllerEmployees;

import configs.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import models.Service;
import models.Showtime;
import models.User;
import utils.Session;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SellAddonsController {

    @FXML
    private TableView<Service> addonsTable;

    @FXML
    private TableColumn<Service, String> serviceNameColumn;

    @FXML
    private TableColumn<Service, Double> priceColumn;

    @FXML
    private TableColumn<Service, String> categoryColumn;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> categoryFilter;

    @FXML
    private ImageView productImage;

    @FXML
    private Label selectedProductLabel;

    @FXML
    private Label bookingInfoLabel;

    @FXML
    private ListView<String> cartListView;

    @FXML
    private Label totalLabel;

    @FXML
    private Button addToCartButton;

    @FXML
    private Button removeFromCartButton;

    @FXML
    private Button skipAddonsButton;

    @FXML
    private Button finishOrderButton;

    @FXML
    private Button backToSeatsButton;

    private ObservableList<Service> addonsList = FXCollections.observableArrayList();
    private ObservableList<Service> allServices = FXCollections.observableArrayList();
    private ObservableList<String> categories = FXCollections.observableArrayList();
    private ObservableList<String> cartItems = FXCollections.observableArrayList();

    private Map<String, Object> bookingData;
    private List<Map<String, Object>> selectedSeats;
    private Map<Integer, Double> seatTypePrices;
    private Showtime selectedShowtime;
    private User currentUser;
    private double seatsTotalPrice;
    private List<Service> selectedAddons = new ArrayList<>();

    @FXML
    public void initialize() {
        // Cấu hình các cột của TableView
        serviceNameColumn.setCellValueFactory(cellData -> cellData.getValue().serviceNameProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());

        // Khởi tạo danh sách danh mục từ cơ sở dữ liệu
        categories.add("All");
        try {
            categories.addAll(fetchCategoriesFromDatabase());
        } catch (SQLException e) {
            showCustomAlert("Lỗi hệ thống", "Lỗi khi lấy danh mục từ cơ sở dữ liệu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        if (categoryFilter != null) {
            categoryFilter.setItems(categories);
            categoryFilter.setValue("All");
        }

        // Khởi tạo danh sách dịch vụ từ cơ sở dữ liệu
        try {
            allServices.addAll(fetchServicesFromDatabase());
        } catch (SQLException e) {
            showCustomAlert("Lỗi hệ thống", "Lỗi khi lấy dịch vụ từ cơ sở dữ liệu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        filterByCategory();
        addonsTable.setItems(addonsList);

        // Khởi tạo cart ListView
        if (cartListView != null) {
            cartListView.setItems(cartItems);
        }

        // Lấy booking data từ Session và khôi phục giỏ hàng
        loadBookingData();

        // Lắng nghe sự kiện chọn dịch vụ
        addonsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (bookingData == null || selectedShowtime == null) {
                showCustomAlert("Chưa có thông tin đặt ghế", "Vui lòng chọn ghế trước khi chọn dịch vụ.", Alert.AlertType.WARNING);
                addonsTable.getSelectionModel().clearSelection();
                selectedProductLabel.setText("Chưa chọn dịch vụ");
                productImage.setImage(null);
                return;
            }
            if (newSelection != null) {
                selectedProductLabel.setText(newSelection.getServiceName());
                String imgPath = newSelection.getImg();
                if (imgPath != null && !imgPath.trim().isEmpty()) {
                    try {
                        Image image = new Image(getClass().getResourceAsStream("/images/" + imgPath.trim()));
                        productImage.setImage(image);
                    } catch (Exception e) {
                        productImage.setImage(null);
                        selectedProductLabel.setText(newSelection.getServiceName() + " (Không có hình ảnh)");
                    }
                } else {
                    productImage.setImage(null);
                    selectedProductLabel.setText(newSelection.getServiceName() + " (Không có hình ảnh)");
                }
            } else {
                selectedProductLabel.setText("Chưa chọn dịch vụ");
                productImage.setImage(null);
            }
        });

        // Lắng nghe sự kiện tìm kiếm
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> {
                filterByCategoryAndSearch(newValue.trim());
            });
        }

        updateTotalLabel();
    }

    private void loadBookingData() {
        bookingData = Session.getBookingData();
        if (bookingData != null) {
            selectedShowtime = (Showtime) bookingData.get("showtime");
            selectedSeats = (List<Map<String, Object>>) bookingData.get("selectedSeats");
            seatTypePrices = (Map<Integer, Double>) bookingData.get("seatTypePrices");
            currentUser = (User) bookingData.get("currentUser");
            seatsTotalPrice = (Double) bookingData.get("seatsTotalPrice");

            // Khôi phục giỏ hàng
            selectedAddons.clear();
            cartItems.clear();
            List<Service> savedAddons = (List<Service>) bookingData.get("selectedAddons");
            if (savedAddons != null) {
                selectedAddons.addAll(savedAddons);
                for (Service service : savedAddons) {
                    cartItems.add(service.getServiceName() + " - " + String.format("%.0f VND", service.getPrice()));
                }
            }

            displayBookingInfo();
        } else {
            // Nếu không có booking data, hiển thị cảnh báo
            showCustomAlert("Không có thông tin đặt ghế", "Vui lòng chọn ghế trước khi vào trang này.", Alert.AlertType.WARNING);
        }
    }

    private void displayBookingInfo() {
        if (selectedShowtime != null && selectedSeats != null && bookingInfoLabel != null) {
            StringBuilder info = new StringBuilder();
            info.append("Phim: ").append(selectedShowtime.getMovieTitle()).append("\n");
            info.append("Phòng: ").append(selectedShowtime.getRoomName()).append("\n");
            info.append("Suất chiếu: ").append(selectedShowtime.getShowDate())
                    .append(" ").append(selectedShowtime.getShowTime()).append("\n");
            info.append("Ghế đã chọn: ");

            for (int i = 0; i < selectedSeats.size(); i++) {
                Map<String, Object> seat = selectedSeats.get(i);
                info.append(seat.get("seatRow")).append(seat.get("seatColumn"));
                if (i < selectedSeats.size() - 1) {
                    info.append(", ");
                }
            }
            info.append("\nTổng tiền ghế: ").append(String.format("%.0f VND", seatsTotalPrice));

            bookingInfoLabel.setText(info.toString());
        }
    }

    @FXML
    private void handleAddToCart() {
        Service selectedService = addonsTable.getSelectionModel().getSelectedItem();
        if (selectedService != null) {
            selectedAddons.add(selectedService);
            cartItems.add(selectedService.getServiceName() + " - " + String.format("%.0f VND", selectedService.getPrice()));
            updateTotalLabel();
            System.out.println("Đã thêm vào giỏ: " + selectedService.getServiceName());
        } else {
            showCustomAlert("Chưa chọn dịch vụ", "Vui lòng chọn một dịch vụ để thêm vào giỏ hàng.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleRemoveFromCart() {
        int selectedIndex = cartListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < selectedAddons.size()) {
            Service removedService = selectedAddons.remove(selectedIndex);
            cartItems.remove(selectedIndex);
            updateTotalLabel();
            System.out.println("Đã xóa khỏi giỏ: " + removedService.getServiceName());
        } else {
            showCustomAlert("Chưa chọn item", "Vui lòng chọn một item trong giỏ hàng để xóa.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleSkipAddons() {
        navigateToLockerSelection();
    }

    @FXML
    private void handleFinishOrder() {
        navigateToLockerSelection();
    }

    @FXML
    private void handleBackToSeats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/SeatSelection.fxml"));
            Parent seatSelectionRoot = loader.load();
            SeatSelectionController controller = loader.getController();
            controller.setData(selectedShowtime);

            AnchorPane parent = (AnchorPane) addonsTable.getScene().lookup("#contentArea");
            if (parent != null) {
                parent.getChildren().setAll(seatSelectionRoot);
                AnchorPane.setTopAnchor(seatSelectionRoot, 0.0);
                AnchorPane.setBottomAnchor(seatSelectionRoot, 0.0);
                AnchorPane.setLeftAnchor(seatSelectionRoot, 0.0);
                AnchorPane.setRightAnchor(seatSelectionRoot, 0.0);
            }

            Session.clearBookingData();

        } catch (IOException ex) {
            System.err.println("Lỗi khi quay lại trang chọn ghế: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void updateTotalLabel() {
        double addonsTotal = selectedAddons.stream().mapToDouble(Service::getPrice).sum();
        double grandTotal = seatsTotalPrice + addonsTotal;

        if (totalLabel != null) {
            totalLabel.setText(String.format("Tổng tiền ghế: %.0f VND\nTổng tiền addon: %.0f VND\nTổng cộng: %.0f VND",
                    seatsTotalPrice, addonsTotal, grandTotal));
        }
    }

    public void setBookingData(Map<String, Object> bookingData) {
        this.bookingData = bookingData;

        if (bookingData != null) {
            selectedShowtime = (Showtime) bookingData.get("showtime");
            selectedSeats = (List<Map<String, Object>>) bookingData.get("selectedSeats");
            seatTypePrices = (Map<Integer, Double>) bookingData.get("seatTypePrices");
            currentUser = (User) bookingData.get("currentUser");
            seatsTotalPrice = (Double) bookingData.get("seatsTotalPrice");

            // Khôi phục giỏ hàng
            selectedAddons.clear();
            cartItems.clear();
            List<Service> savedAddons = (List<Service>) bookingData.get("selectedAddons");
            if (savedAddons != null) {
                selectedAddons.addAll(savedAddons);
                for (Service service : savedAddons) {
                    cartItems.add(service.getServiceName() + " - " + String.format("%.0f VND", service.getPrice()));
                }
            }

            displayBookingInfo();
            updateTotalLabel();

            System.out.println("Đã nhận booking data từ SeatSelection hoặc LockerController");
        } else {
            System.out.println("BookingData truyền vào là null");
        }
    }

    private void navigateToLockerSelection() {
        try {
            if (bookingData == null) {
                bookingData = new HashMap<>();
            }

            bookingData.put("selectedAddons", selectedAddons);
            double addonsTotal = selectedAddons.stream().mapToDouble(Service::getPrice).sum();
            bookingData.put("addonsTotalPrice", addonsTotal);

            Session.setBookingData(bookingData);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/Locker.fxml"));
            Parent lockerRoot = loader.load();
            LockerController controller = loader.getController();

            controller.setBookingData(bookingData);

            AnchorPane parent = (AnchorPane) addonsTable.getScene().lookup("#contentArea");
            if (parent != null) {
                parent.getChildren().setAll(lockerRoot);
                AnchorPane.setTopAnchor(lockerRoot, 0.0);
                AnchorPane.setBottomAnchor(lockerRoot, 0.0);
                AnchorPane.setLeftAnchor(lockerRoot, 0.0);
                AnchorPane.setRightAnchor(lockerRoot, 0.0);
            }

            System.out.println("Đã chuyển sang trang chọn locker");

        } catch (IOException ex) {
            System.err.println("Lỗi khi chuyển sang trang chọn locker: " + ex.getMessage());
            ex.printStackTrace();
            showCustomAlert("Lỗi hệ thống", "Không thể chuyển sang trang chọn locker: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void filterByCategory() {
        String selectedCategory = categoryFilter.getValue();
        addonsList.clear();
        if ("All".equals(selectedCategory)) {
            addonsList.addAll(allServices);
        } else {
            addonsList.addAll(
                    allServices.filtered(service -> selectedCategory.equals(service.getCategory()))
            );
        }
        filterByCategoryAndSearch(searchField != null ? searchField.getText().trim() : "");
    }

    private void filterByCategoryAndSearch(String searchText) {
        String selectedCategory = categoryFilter.getValue();
        addonsList.clear();
        ObservableList<Service> filteredList = allServices.filtered(service ->
                (selectedCategory.equals("All") || selectedCategory.equals(service.getCategory())) &&
                        (searchText.isEmpty() || service.getServiceName().toLowerCase().contains(searchText.toLowerCase()))
        );
        addonsList.addAll(filteredList);
    }

    private ObservableList<String> fetchCategoriesFromDatabase() throws SQLException {
        ObservableList<String> categories = FXCollections.observableArrayList();
        String query = "SELECT categoryName FROM serviceCategories";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                categories.add(rs.getString("categoryName"));
            }
        }
        return categories;
    }

    private ObservableList<Service> fetchServicesFromDatabase() throws SQLException {
        ObservableList<Service> services = FXCollections.observableArrayList();
        String query = "SELECT s.serviceId, s.serviceName, s.price, c.categoryName, s.img " +
                "FROM services s JOIN serviceCategories c ON s.categoryId = c.categoryId";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                services.add(new Service(
                        rs.getString("serviceId"),
                        rs.getString("serviceName"),
                        rs.getDouble("price"),
                        0,
                        rs.getString("categoryName"),
                        rs.getString("img") != null ? rs.getString("img").trim() : ""
                ));
            }
        }
        return services;
    }

    private void showCustomAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #f9f9f9;");
        alert.getButtonTypes().setAll(ButtonType.CLOSE);
        alert.showAndWait();
    }
}