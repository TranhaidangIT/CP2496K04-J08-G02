package controller.controllerEmployees;

import configs.DBConnection;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class SellAddonsController {

    @FXML private TableView<Service> addonsTable;
    @FXML private TableColumn<Service, String> serviceNameColumn;
    @FXML private TableColumn<Service, BigDecimal> priceColumn;
    @FXML private TableColumn<Service, String> categoryColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ImageView productImage;
    @FXML private Label selectedProductLabel;
    @FXML private Label bookingInfoLabel;
    @FXML private ListView<String> cartListView;
    @FXML private Label totalLabel;
    @FXML private Button addToCartButton;
    @FXML private Button removeFromCartButton;
    @FXML private Button skipAddonsButton;
    @FXML private Button finishOrderButton;
    @FXML private Button backToSeatsButton;

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
        // Table columns
        serviceNameColumn.setCellValueFactory(cellData -> cellData.getValue().serviceNameProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty());
        // Format BigDecimal hiển thị
        priceColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.0f VND", item));
            }
        });
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryNameProperty());

        // Categories
        categories.add("All");
        try {
            categories.addAll(fetchCategoriesFromDatabase());
        } catch (SQLException e) {
            showCustomAlert("Error", "Cannot load categories: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        categoryFilter.setItems(categories);
        categoryFilter.setValue("All");

        // Services
        try {
            allServices.addAll(fetchServicesFromDatabase());
        } catch (SQLException e) {
            showCustomAlert("Error", "Cannot load services: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        filterByCategory();
        addonsTable.setItems(addonsList);

        // Cart ListView
        cartListView.setItems(cartItems);

        // Load booking
        loadBookingData();

        // Select service event
        addonsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (bookingData == null || selectedShowtime == null) {
                showCustomAlert("No seat data", "Please select seat before choosing service.", Alert.AlertType.WARNING);
                addonsTable.getSelectionModel().clearSelection();
                selectedProductLabel.setText("No service selected");
                productImage.setImage(null);
                return;
            }
            if (newSel != null) {
                selectedProductLabel.setText(newSel.getServiceName());
                String imgPath = newSel.getImg();
                if (imgPath != null && !imgPath.isBlank()) {
                    try {
                        Image image = new Image(getClass().getResourceAsStream("/images/" + imgPath));
                        productImage.setImage(image);
                    } catch (Exception e) {
                        productImage.setImage(null);
                        selectedProductLabel.setText(newSel.getServiceName() + " (No image)");
                    }
                } else {
                    productImage.setImage(null);
                    selectedProductLabel.setText(newSel.getServiceName() + " (No image)");
                }
            } else {
                selectedProductLabel.setText("No service selected");
                productImage.setImage(null);
            }
        });

        // Search event
        searchField.textProperty().addListener((obs, o, n) -> filterByCategoryAndSearch(n.trim()));

        updateTotalLabel();
    }

    private void loadBookingData() {
        bookingData = Session.getBookingData();
        if (bookingData != null) {
            selectedShowtime = (Showtime) bookingData.get("showtime");
            selectedSeats = (List<Map<String, Object>>) bookingData.get("selectedSeats");
            seatTypePrices = (Map<Integer, Double>) bookingData.get("seatTypePrices");
            currentUser = (User) bookingData.get("currentUser");

            // Handle both Double and BigDecimal for seatsTotalPrice
            Object seatsTotal = bookingData.get("seatsTotalPrice");
            if (seatsTotal instanceof BigDecimal) {
                seatsTotalPrice = ((BigDecimal) seatsTotal).doubleValue();
            } else if (seatsTotal instanceof Double) {
                seatsTotalPrice = (Double) seatsTotal;
            } else {
                seatsTotalPrice = 0.0;
            }

            selectedAddons.clear();
            cartItems.clear();
            List<Service> savedAddons = (List<Service>) bookingData.get("selectedAddons");
            if (savedAddons != null) {
                selectedAddons.addAll(savedAddons);
                for (Service s : savedAddons) {
                    cartItems.add(s.getServiceName() + " - " + formatMoney(s.getPrice()));
                }
            }

            displayBookingInfo();
        } else {
            showCustomAlert("No seat data", "Please select a seat first.", Alert.AlertType.WARNING);
        }
    }

    private void displayBookingInfo() {
        if (selectedShowtime != null && selectedSeats != null) {
            StringBuilder info = new StringBuilder();
            info.append("Movie: ").append(selectedShowtime.getMovieTitle()).append("\n");
            info.append("Room: ").append(selectedShowtime.getRoomName()).append("\n");
            info.append("Showtime: ").append(selectedShowtime.getShowDate())
                    .append(" ").append(selectedShowtime.getShowTime()).append("\n");
            info.append("Selected Seat: ");
            for (int i = 0; i < selectedSeats.size(); i++) {
                Map<String, Object> seat = selectedSeats.get(i);
                info.append(seat.get("seatRow")).append(seat.get("seatColumn"));
                if (i < selectedSeats.size() - 1) info.append(", ");
            }
            info.append("\nTotal seat price: ").append(String.format("%.0f VND", seatsTotalPrice));
            bookingInfoLabel.setText(info.toString());
        }
    }

    @FXML
    private void handleAddToCart() {
        Service selectedService = addonsTable.getSelectionModel().getSelectedItem();
        if (selectedService != null) {
            selectedAddons.add(selectedService);
            cartItems.add(selectedService.getServiceName() + " - " + formatMoney(selectedService.getPrice()));
            updateTotalLabel();
        } else {
            showCustomAlert("No service selected", "Please select a service.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleRemoveFromCart() {
        int idx = cartListView.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < selectedAddons.size()) {
            selectedAddons.remove(idx);
            cartItems.remove(idx);
            updateTotalLabel();
        } else {
            showCustomAlert("No item selected", "Please select an item to remove.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleBackToSeats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/SeatSelection.fxml"));
            Parent seatRoot = loader.load();

            SeatSelectionController controller = loader.getController();
            controller.setBookingData(bookingData); // truyền dữ liệu đặt chỗ

            AnchorPane parent = (AnchorPane) addonsTable.getScene().lookup("#contentArea");
            if (parent != null) {
                parent.getChildren().setAll(seatRoot);
                AnchorPane.setTopAnchor(seatRoot, 0.0);
                AnchorPane.setBottomAnchor(seatRoot, 0.0);
                AnchorPane.setLeftAnchor(seatRoot, 0.0);
                AnchorPane.setRightAnchor(seatRoot, 0.0);
            }
        } catch (IOException e) {
            showCustomAlert("Error", "Cannot go back to seat selection: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    @FXML private void handleSkipAddons() { navigateToLockerSelection(); }
    @FXML private void handleFinishOrder() { navigateToLockerSelection(); }

    private void updateTotalLabel() {
        BigDecimal addonsTotal = selectedAddons.stream()
                .map(Service::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal grandTotal = addonsTotal.add(BigDecimal.valueOf(seatsTotalPrice));

        totalLabel.setText(String.format(
                "Total seat price: %.0f VND\nTotal addon price: %.0f VND\nTotal: %.0f VND",
                seatsTotalPrice, addonsTotal, grandTotal));
    }

    private String formatMoney(BigDecimal value) {
        return String.format("%.0f VND", value);
    }

    private ObservableList<String> fetchCategoriesFromDatabase() throws SQLException {
        ObservableList<String> list = FXCollections.observableArrayList();
        String query = "SELECT categoryName FROM serviceCategories";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) list.add(rs.getString("categoryName"));
        }
        return list;
    }

    private ObservableList<Service> fetchServicesFromDatabase() throws SQLException {
        ObservableList<Service> list = FXCollections.observableArrayList();
        String sql = """
            SELECT s.serviceId, s.serviceName, s.price, c.categoryId, c.categoryName, s.img, s.createdAt
            FROM services s
            JOIN serviceCategories c ON s.categoryId = c.categoryId
        """;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Service(
                        rs.getInt("serviceId"),
                        rs.getString("serviceName"),
                        rs.getBigDecimal("price"),
                        0, // quantity mặc định
                        rs.getInt("categoryId"),
                        rs.getString("categoryName"),
                        rs.getString("img") != null ? rs.getString("img").trim() : "",
                        rs.getTimestamp("createdAt").toLocalDateTime()
                ));
            }
        }
        return list;
    }

    @FXML
    private void filterByCategory() {
        String selectedCategory = categoryFilter.getValue();
        addonsList.clear();
        if ("All".equals(selectedCategory)) {
            addonsList.addAll(allServices);
        } else {
            addonsList.addAll(allServices.filtered(s -> selectedCategory.equals(s.getCategoryName())));
        }
        filterByCategoryAndSearch(searchField.getText().trim());
    }

    private void filterByCategoryAndSearch(String searchText) {
        String selectedCategory = categoryFilter.getValue();
        addonsList.clear();
        addonsList.addAll(allServices.filtered(s ->
                ("All".equals(selectedCategory) || selectedCategory.equals(s.getCategoryName())) &&
                        (searchText.isEmpty() || s.getServiceName().toLowerCase().contains(searchText.toLowerCase()))
        ));
    }

    private void navigateToLockerSelection() {
        try {
            if (bookingData == null) bookingData = new HashMap<>();
            bookingData.put("selectedAddons", selectedAddons);
            BigDecimal addonsTotal = selectedAddons.stream()
                    .map(Service::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
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
        } catch (IOException e) {
            showCustomAlert("Error", "Cannot go to locker selection: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showCustomAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Method to set booking data from external controllers
     * This method is called when navigating from other controllers
     */
    public void setBookingData(Map<String, Object> bookingData) {
        this.bookingData = bookingData;
        if (bookingData != null) {
            selectedShowtime = (Showtime) bookingData.get("showtime");
            selectedSeats = (List<Map<String, Object>>) bookingData.get("selectedSeats");
            seatTypePrices = (Map<Integer, Double>) bookingData.get("seatTypePrices");
            currentUser = (User) bookingData.get("currentUser");

            // Handle both Double and BigDecimal for seatsTotalPrice
            Object seatsTotal = bookingData.get("seatsTotalPrice");
            if (seatsTotal instanceof BigDecimal) {
                seatsTotalPrice = ((BigDecimal) seatsTotal).doubleValue();
            } else if (seatsTotal instanceof Double) {
                seatsTotalPrice = (Double) seatsTotal;
            } else {
                seatsTotalPrice = 0.0;
            }

            // Load existing addons if any
            selectedAddons.clear();
            cartItems.clear();
            List<Service> savedAddons = (List<Service>) bookingData.get("selectedAddons");
            if (savedAddons != null) {
                selectedAddons.addAll(savedAddons);
                for (Service s : savedAddons) {
                    cartItems.add(s.getServiceName() + " - " + formatMoney(s.getPrice()));
                }
            }

            displayBookingInfo();
            updateTotalLabel();
        }
    }
}