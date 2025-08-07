package controller.controllerEmployees;

import configs.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import models.*;
import utils.Session;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LockerController {

    @FXML private GridPane lockerGrid;
    @FXML private Label selectedLockerLabel;
    @FXML private VBox bookingInfoBox; // Thêm để tham chiếu đến VBox mới
    @FXML private Label movieTitleLabel;
    @FXML private Label showtimeLabel;
    @FXML private Label roomLabel;
    @FXML private Label seatsLabel;
    @FXML private Label addonsLabel;
    @FXML private Label totalPriceLabel;
    @FXML private Button skipLockerButton;
    @FXML private Button finishWithLockerButton;
    @FXML private Button backToAddonsButton;
    @FXML private VBox lockerAssignmentBox;
    @FXML private TextField itemDescriptionField;
    @FXML private Label pinCodeLabel;

    private final List<Locker> availableLockers = new ArrayList<>();
    private Locker selectedLocker = null;
    private Map<String, Object> bookingData;
    private String generatedPinCode = null;
    private boolean hasBookingData = false;

    @FXML
    public void initialize() {
        setupLockerAssignmentControls();
        hasBookingData = loadBookingData();
        loadAllLockers();
        displayLockers();
        updateNavigationButtonsVisibility();
    }

    private void setupLockerAssignmentControls() {
        if (lockerAssignmentBox != null) {
            lockerAssignmentBox.setVisible(false);
        }
    }

    private boolean loadBookingData() {
        bookingData = Session.getBookingData();
        if (bookingData != null) {
            if (bookingData.get("showtime") != null && bookingData.get("selectedSeats") != null) {
                displayBookingInfo();
                return true;
            }
        }
        if (movieTitleLabel != null) {
            movieTitleLabel.setText("Không có thông tin đặt vé.");
            showtimeLabel.setText("");
            roomLabel.setText("");
            seatsLabel.setText("");
            addonsLabel.setText("");
            totalPriceLabel.setText("Vui lòng quay lại chọn vé.");
        }
        return false;
    }

    private void updateNavigationButtonsVisibility() {
        if (skipLockerButton != null) {
            skipLockerButton.setVisible(hasBookingData);
        }
        if (finishWithLockerButton != null) {
            finishWithLockerButton.setVisible(hasBookingData);
        }
        if (backToAddonsButton != null) {
            backToAddonsButton.setVisible(hasBookingData);
        }
    }

    private void displayBookingInfo() {
        if (bookingInfoBox == null) return;

        Showtime showtime = (Showtime) bookingData.get("showtime");
        List<Map<String, Object>> seats = (List<Map<String, Object>>) bookingData.get("selectedSeats");
        Double seatsTotal = (Double) bookingData.get("seatsTotalPrice");
        Double addonsTotal = (Double) bookingData.get("addonsTotalPrice");
        List<Service> selectedAddons = (List<Service>) bookingData.get("selectedAddons");

        if (showtime != null && seats != null) {
            // Cập nhật tiêu đề phim
            movieTitleLabel.setText("Phim: " + showtime.getMovieTitle());

            // Cập nhật suất chiếu
            showtimeLabel.setText("Suất chiếu: " + showtime.getShowDate() + " " + showtime.getShowTime());

            // Cập nhật phòng chiếu
            roomLabel.setText("Phòng: " + showtime.getRoomName());

            // Cập nhật danh sách ghế
            StringBuilder seatsInfo = new StringBuilder("Ghế: ");
            for (int i = 0; i < seats.size(); i++) {
                Map<String, Object> seat = seats.get(i);
                seatsInfo.append(seat.get("seatRow")).append(seat.get("seatColumn"));
                if (i < seats.size() - 1) seatsInfo.append(", ");
            }
            seatsLabel.setText(seatsInfo.toString());

            // Cập nhật dịch vụ bổ sung
            StringBuilder addonsInfo = new StringBuilder("Dịch vụ bổ sung: ");
            if (selectedAddons != null && !selectedAddons.isEmpty()) {
                for (int i = 0; i < selectedAddons.size(); i++) {
                    Service addon = selectedAddons.get(i);
                    addonsInfo.append(addon.getServiceName()).append(" (").append(String.format("%.0f VND", addon.getPrice())).append(")");
                    if (i < selectedAddons.size() - 1) addonsInfo.append(", ");
                }
            } else {
                addonsInfo.append("Không có");
            }
            addonsLabel.setText(addonsInfo.toString());

            // Cập nhật tổng tiền
            double totalPrice = (seatsTotal != null ? seatsTotal : 0) + (addonsTotal != null ? addonsTotal : 0);
            totalPriceLabel.setText("Tổng tiền: " + String.format("%.0f VND", totalPrice));
        } else {
            movieTitleLabel.setText("Không có thông tin đặt vé.");
            showtimeLabel.setText("");
            roomLabel.setText("");
            seatsLabel.setText("");
            addonsLabel.setText("");
            totalPriceLabel.setText("Vui lòng quay lại chọn vé.");
        }
    }

    private void loadAllLockers() {
        availableLockers.clear();
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT lockerId, lockerNumber, locationInfo, status FROM lockers ORDER BY lockerNumber";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Locker locker = new Locker(
                        rs.getInt("lockerId"),
                        rs.getString("lockerNumber"),
                        rs.getString("locationInfo"),
                        rs.getString("status")
                );
                availableLockers.add(locker);
            }
        } catch (SQLException e) {
            showAlert("Lỗi cơ sở dữ liệu", "Không thể tải danh sách locker: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void displayLockers() {
        if (lockerGrid == null) return;

        lockerGrid.getChildren().clear();
        int cols = 8;
        int row = 0, col = 0;

        for (Locker locker : availableLockers) {
            Button lockerButton = createLockerButton(locker);
            lockerGrid.add(lockerButton, col, row);
            GridPane.setMargin(lockerButton, new Insets(5));

            col++;
            if (col >= cols) {
                col = 0;
                row++;
            }
        }
    }

    private Button createLockerButton(Locker locker) {
        Button button = new Button(locker.getLockerNumber());
        button.setPrefSize(90, 70);
        String style = getLockerButtonStyle(locker.getStatus());
        button.setStyle(style);

        if ("Available".equalsIgnoreCase(locker.getStatus())) {
            button.setOnAction(e -> handleAvailableLockerClick(locker));
        } else if ("Occupied".equalsIgnoreCase(locker.getStatus())) {
            button.setOnAction(e -> handleOccupiedLockerClick(locker));
        } else {
            button.setOnAction(e -> showLockerInfo(locker));
        }

        return button;
    }

    private void handleAvailableLockerClick(Locker locker) {
        if (!hasBookingData) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cần thông tin đặt vé");
            alert.setHeaderText("Để chọn tủ giữ đồ mới");
            alert.setContentText("Bạn cần có thông tin đặt vé để chọn tủ giữ đồ.\nHệ thống sẽ chuyển bạn về trang chọn phim.");

            alert.setOnHidden(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/ListMovies.fxml"));
                    Parent movieRoot = loader.load();
                    navigateToPage(movieRoot);
                } catch (IOException ex) {
                    System.err.println("Lỗi khi chuyển về trang chọn phim: " + ex.getMessage());
                }
            });
            alert.showAndWait();
            return;
        }

        selectLocker(locker);
    }

    private void handleOccupiedLockerClick(Locker locker) {
        showRetrieveItemDialog(locker);
    }

    private void showRetrieveItemDialog(Locker locker) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Lấy đồ từ tủ " + locker.getLockerNumber());
        dialog.setHeaderText("Nhập mã PIN để lấy đồ");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        Label infoLabel = new Label("Tủ: " + locker.getLockerNumber() + " - " + locker.getLocationInfo());
        infoLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        Label pinLabel = new Label("Mã PIN (4 số):");
        TextField pinField = new TextField();
        pinField.setPromptText("Nhập mã PIN...");
        pinField.setPrefWidth(200);

        pinField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                pinField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 4) {
                pinField.setText(oldValue);
            }
        });

        content.getChildren().addAll(infoLabel, pinLabel, pinField);
        dialog.getDialogPane().setContent(content);

        ButtonType retrieveButtonType = new ButtonType("Lấy đồ", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(retrieveButtonType, cancelButtonType);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == retrieveButtonType) {
                return pinField.getText().trim();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(pin -> {
            if (pin != null && pin.length() == 4) {
                processRetrieveItem(locker, pin);
            } else {
                showAlert("Lỗi", "Mã PIN phải có đúng 4 chữ số!", Alert.AlertType.ERROR);
            }
        });
    }

    private void processRetrieveItem(Locker locker, String enteredPin) {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("DEBUG: Checking locker " + locker.getLockerId() + " with PIN: " + enteredPin);

            String checkQuery = "SELECT la.assignmentId, la.pinCode, la.itemDescription, la.assignedAt " +
                    "FROM lockerAssignments la " +
                    "WHERE la.lockerId = ? AND la.releasedAt IS NULL";

            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, locker.getLockerId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String correctPin = rs.getString("pinCode");
                String itemDescription = rs.getString("itemDescription");
                int assignmentId = rs.getInt("assignmentId");

                System.out.println("DEBUG: Found assignment ID: " + assignmentId);
                System.out.println("DEBUG: Correct PIN: " + correctPin);
                System.out.println("DEBUG: Item: " + itemDescription);

                if (correctPin != null && correctPin.trim().equals(enteredPin.trim())) {
                    try {
                        releaseLocker(conn, locker.getLockerId(), assignmentId);

                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Lấy đồ thành công");
                        successAlert.setHeaderText("Tủ " + locker.getLockerNumber() + " đã được mở");
                        successAlert.setContentText("Đồ vật: " + itemDescription + "\n\nVui lòng lấy đồ và đóng tủ lại.");
                        successAlert.showAndWait();

                        loadAllLockers();
                        displayLockers();

                        System.out.println("DEBUG: Successfully released locker");

                    } catch (SQLException e) {
                        System.err.println("DEBUG: Error releasing locker: " + e.getMessage());
                        showAlert("Lỗi", "Không thể cập nhật trạng thái tủ: " + e.getMessage(), Alert.AlertType.ERROR);
                    }

                } else {
                    System.out.println("DEBUG: PIN mismatch. Expected: '" + correctPin + "', Entered: '" + enteredPin + "'");
                    showAlert("Mã PIN không đúng",
                            "Mã PIN không khớp.\nMã đã nhập: " + enteredPin +
                                    "\nVui lòng kiểm tra lại mã PIN trên vé của bạn.",
                            Alert.AlertType.ERROR);
                }
            } else {
                System.out.println("DEBUG: No active assignment found for locker " + locker.getLockerId());

                String checkAllQuery = "SELECT COUNT(*) as total FROM lockerAssignments WHERE lockerId = ?";
                PreparedStatement checkAllStmt = conn.prepareStatement(checkAllQuery);
                checkAllStmt.setInt(1, locker.getLockerId());
                ResultSet allRs = checkAllStmt.executeQuery();

                if (allRs.next() && allRs.getInt("total") > 0) {
                    showAlert("Lỗi", "Tủ này đã được lấy đồ rồi hoặc không có đồ gửi.", Alert.AlertType.ERROR);
                } else {
                    showAlert("Lỗi", "Không tìm thấy thông tin gửi đồ cho tủ này.", Alert.AlertType.ERROR);
                }
            }

        } catch (SQLException e) {
            System.err.println("DEBUG: Database error: " + e.getMessage());
            e.printStackTrace();
            showAlert("Lỗi cơ sở dữ liệu", "Không thể kiểm tra thông tin tủ: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void releaseLocker(Connection conn, int lockerId, int assignmentId) throws SQLException {
        System.out.println("DEBUG: Starting releaseLocker for lockerId: " + lockerId + ", assignmentId: " + assignmentId);

        boolean originalAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            String updateAssignmentQuery = "UPDATE lockerAssignments SET releasedAt = ? WHERE assignmentId = ?";
            PreparedStatement updateAssignmentStmt = conn.prepareStatement(updateAssignmentQuery);
            updateAssignmentStmt.setObject(1, LocalDateTime.now());
            updateAssignmentStmt.setInt(2, assignmentId);
            int assignmentRows = updateAssignmentStmt.executeUpdate();
            System.out.println("DEBUG: Updated " + assignmentRows + " assignment rows");

            String updateLockerQuery = "UPDATE lockers SET status = 'Available' WHERE lockerId = ?";
            PreparedStatement updateLockerStmt = conn.prepareStatement(updateLockerQuery);
            updateLockerStmt.setInt(1, lockerId);
            int lockerRows = updateLockerStmt.executeUpdate();
            System.out.println("DEBUG: Updated " + lockerRows + " locker rows");

            logLockerHistory(conn, lockerId, "Item retrieved", "Customer retrieved items");
            System.out.println("DEBUG: Logged history");

            conn.commit();
            System.out.println("DEBUG: Transaction committed successfully");

        } catch (SQLException e) {
            System.err.println("DEBUG: Error in releaseLocker: " + e.getMessage());
            e.printStackTrace();
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    private String getLockerButtonStyle(String status) {
        switch (status.toLowerCase()) {
            case "available":
                return "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;";
            case "occupied":
                return "-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;";
            case "maintenance":
                return "-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-opacity: 0.7;";
            default:
                return "-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-opacity: 0.7;";
        }
    }

    private void showLockerInfo(Locker locker) {
        String message;
        switch (locker.getStatus().toLowerCase()) {
            case "occupied":
                message = "Tủ " + locker.getLockerNumber() + " đang được sử dụng.\n" +
                        "Nhấp vào tủ và nhập mã PIN để lấy đồ.";
                break;
            case "maintenance":
                message = "Tủ " + locker.getLockerNumber() + " đang bảo trì.\n" +
                        "Vui lòng chọn tủ khác hoặc liên hệ nhân viên.";
                break;
            default:
                message = "Tủ " + locker.getLockerNumber() + " không khả dụng.";
                break;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông tin tủ giữ đồ");
        alert.setHeaderText("Tủ " + locker.getLockerNumber());
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void selectLocker(Locker locker) {
        this.selectedLocker = locker;

        selectedLockerLabel.setText("Đã chọn: Locker " + locker.getLockerNumber() +
                " - " + locker.getLocationInfo());

        generatedPinCode = generatePinCode();

        if (lockerAssignmentBox != null) {
            lockerAssignmentBox.setVisible(true);
            pinCodeLabel.setText("Mã PIN: " + generatedPinCode);
        }

        refreshLockerDisplay();
    }

    private void refreshLockerDisplay() {
        lockerGrid.getChildren().forEach(node -> {
            if (node instanceof Button) {
                Button btn = (Button) node;
                String lockerNum = btn.getText();

                if (selectedLocker != null && lockerNum.equals(selectedLocker.getLockerNumber())) {
                    btn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
                } else {
                    Locker originalLocker = availableLockers.stream()
                            .filter(l -> l.getLockerNumber().equals(lockerNum))
                            .findFirst().orElse(null);
                    if (originalLocker != null) {
                        btn.setStyle(getLockerButtonStyle(originalLocker.getStatus()));
                    }
                }
            }
        });
    }

    private String generatePinCode() {
        return String.format("%04d", new Random().nextInt(10000));
    }

    @FXML
    private void handleSkipLocker() {
        navigateToTotal(false);
    }

    @FXML
    private void handleFinishWithLocker() {
        if (selectedLocker == null) {
            showAlert("Chưa chọn locker", "Vui lòng chọn một locker trước khi tiếp tục.", Alert.AlertType.WARNING);
            return;
        }

        String itemDescription = itemDescriptionField.getText().trim();
        if (itemDescription.isEmpty()) {
            showAlert("Thiếu thông tin", "Vui lòng mô tả đồ vật cần gửi.", Alert.AlertType.WARNING);
            return;
        }

        bookingData.put("selectedLocker", selectedLocker);
        bookingData.put("lockerPinCode", generatedPinCode);
        bookingData.put("itemDescription", itemDescription);

        navigateToTotal(true);
    }

    @FXML
    private void handleBackToAddons() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/SellAddons.fxml"));
            Parent addonsRoot = loader.load();

            SellAddonsController controller = loader.getController();
            controller.setBookingData(bookingData);

            navigateToPage(addonsRoot);
        } catch (IOException ex) {
            showAlert("Lỗi", "Không thể quay lại trang addons: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Cập nhật phương thức navigateToTotal để truyền ticketCode
    private void navigateToTotal(boolean hasLocker) {
        try {
            if (hasLocker) {
                if (selectedLocker == null) {
                    showAlert("Chưa chọn locker", "Vui lòng chọn một locker trước khi tiếp tục.", Alert.AlertType.WARNING);
                    return;
                }
                String itemDescription = itemDescriptionField.getText().trim();
                if (itemDescription.isEmpty()) {
                    showAlert("Thiếu thông tin", "Vui lòng mô tả đồ vật cần gửi.", Alert.AlertType.WARNING);
                    return;
                }

                // Lưu thông tin locker vào bookingData
                bookingData.put("selectedLocker", selectedLocker);
                bookingData.put("lockerPinCode", generatedPinCode);
                bookingData.put("itemDescription", itemDescription);
                Session.setBookingData(bookingData);
            }

            // Chuyển đến trang Total.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/Total.fxml"));
            Parent totalRoot = loader.load();
            TotalController controller = loader.getController();
            controller.setContentArea((AnchorPane) lockerGrid.getScene().lookup("#contentArea"));
            navigateToPage(totalRoot);

        } catch (IOException ex) {
            showAlert("Lỗi", "Không thể chuyển đến trang thanh toán: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void navigateToPage(Parent page) {
        AnchorPane parent = (AnchorPane) lockerGrid.getScene().lookup("#contentArea");
        if (parent != null) {
            parent.getChildren().setAll(page);
            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);
        }
    }

    private void updateLockerStatus(int lockerId, String newStatus) {
        try (Connection conn = DBConnection.getConnection()) {
            String updateQuery = "UPDATE lockers SET status = ? WHERE lockerId = ?";
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, newStatus);
            stmt.setInt(2, lockerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật trạng thái locker: " + e.getMessage());
        }
    }

    // Cập nhật phương thức assignLockerToCustomer trong LockerController
    public static void assignLockerToCustomer(Connection conn, int lockerId, String ticketCode, String pinCode, String itemDescription, String customerName) throws SQLException {
        // Kiểm tra ticketCode tồn tại
        String checkQuery = "SELECT COUNT(*) FROM tickets WHERE ticketCode = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, ticketCode);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) == 0) {
                throw new SQLException("TicketCode " + ticketCode + " không tồn tại trong bảng tickets.");
            }
        }

        // Chèn vào lockerAssignments
        String insertQuery = "INSERT INTO lockerAssignments (lockerId, pinCode, itemDescription, ticketCode, assignedAt) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setInt(1, lockerId);
            stmt.setString(2, pinCode);
            stmt.setString(3, itemDescription);
            stmt.setString(4, ticketCode);
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        }

        // Cập nhật trạng thái tủ
        String updateQuery = "UPDATE lockers SET status = 'Occupied' WHERE lockerId = ? AND status = 'Available'";
        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
            updateStmt.setInt(1, lockerId);
            int rowsAffected = updateStmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Không thể cập nhật trạng thái tủ: Tủ không tồn tại hoặc không ở trạng thái Available.");
            }
        }

        // Ghi lịch sử
        logLockerHistory(conn, lockerId, "Assigned to customer", "Customer: " + customerName + ", Ticket: " + ticketCode);
    }

    private static void logLockerHistory(Connection conn, int lockerId, String action, String userInfo) throws SQLException {
        String insertQuery = "INSERT INTO lockerHistory (lockerId, userAction, actionTime, userInfo) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setInt(1, lockerId);
            stmt.setString(2, action);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(4, userInfo);
            stmt.executeUpdate();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setBookingData(Map<String, Object> bookingData) {
        this.bookingData = bookingData;
        hasBookingData = loadBookingData();
        updateNavigationButtonsVisibility();
    }
}