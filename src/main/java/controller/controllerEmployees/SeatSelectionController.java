package controller.controllerEmployees;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import models.Showtime;
import models.User;
import configs.DBConnection;
import utils.Session;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SeatSelectionController {

    @FXML private Label showtimeLabel;
    @FXML private GridPane seatGrid;
    @FXML private Label totalPriceLabel;
    @FXML private Button cancelButton;
    @FXML private Button confirmButton;

    private Showtime selectedShowtime;
    private List<Map<String, Object>> selectedSeats;
    private Map<String, Object> bookingData;
    private Map<Integer, Double> seatTypePrices;

    @FXML
    public void initialize() {
        System.out.println("Khởi tạo SeatSelectionController");
        selectedSeats = new ArrayList<>();
        seatTypePrices = new HashMap<>();
        if (showtimeLabel == null || seatGrid == null || totalPriceLabel == null ||
                cancelButton == null || confirmButton == null) {
            System.err.println("Lỗi: Một hoặc nhiều fx:id không được khởi tạo đúng trong SeatSelection.fxml");
        }
        updateTotalPrice();
    }

    public void setData(Showtime showtime) {
<<<<<<< HEAD
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("showtime", showtime);
        bookingData.put("selectedSeats", new ArrayList<Map<String, Object>>());
        bookingData.put("seatTypePrices", new HashMap<Integer, Double>());
        setBookingData(bookingData);
=======
        this.selectedShowtime = showtime;
        System.out.println("setData called with showtime: " + (showtime != null ? showtime.toString() : "null"));
        if (selectedShowtime == null) {
            System.err.println("Lỗi: Showtime là null trong SeatSelectionController!");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi suất chiếu");
            alert.setHeaderText(null);
            alert.setContentText("Không có thông tin suất chiếu. Vui lòng chọn lại suất chiếu.");
            alert.showAndWait();
            return;
        }
        System.out.println("showtimeId: " + selectedShowtime.getShowtimeId());

        // Kiểm tra showtimeId có tồn tại trong bảng showtimes
        try (Connection conn = DBConnection.getConnection()) {
            String checkQuery = "SELECT COUNT(*) FROM showtimes WHERE showtimeId = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, selectedShowtime.getShowtimeId());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                System.err.println("Lỗi: showtimeId " + selectedShowtime.getShowtimeId() + " không tồn tại trong bảng showtimes!");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi suất chiếu");
                alert.setHeaderText(null);
                alert.setContentText("Suất chiếu không tồn tại trong cơ sở dữ liệu. Vui lòng kiểm tra lại.");
                alert.showAndWait();
                return;
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi kiểm tra showtimeId: " + ex.getMessage());
            ex.printStackTrace();
        }

        seatGrid.getChildren().clear();
        selectedSeats.clear();
        updateTotalPrice();
        displayShowtimeInfo();
        loadSeatTypePrices();
        loadSeats();
>>>>>>> 44fab4a810779ded600d5ec37ab3c472deb80785
    }

    private void displayShowtimeInfo() {
        System.out.println("Phim: " + selectedShowtime.getMovieTitle());
        System.out.println("Phòng: " + selectedShowtime.getRoomName());
        System.out.println("Ngày chiếu: " + selectedShowtime.getShowDate());
        System.out.println("Giờ bắt đầu: " + selectedShowtime.getShowTime());
        System.out.println("Giờ kết thúc: " + selectedShowtime.getEndTime());

        showtimeLabel.setText(
                selectedShowtime.getMovieTitle() + " | " +
                        selectedShowtime.getRoomName() + " | " +
                        selectedShowtime.getShowDate() + " " + selectedShowtime.getShowTime() + " - " + selectedShowtime.getEndTime()
        );
    }

    private void loadSeatTypePrices() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT seatTypeId, price FROM seatTypes";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                seatTypePrices.put(rs.getInt("seatTypeId"), rs.getDouble("price"));
            }
            System.out.println("Đã tải giá ghế: " + seatTypePrices);
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tải giá SeatType: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadSeats() {
        seatGrid.getChildren().clear();
<<<<<<< HEAD
        if (selectedShowtime == null) {
            showErrorAlert("Lỗi suất chiếu", "Không có thông tin suất chiếu. Vui lòng chọn lại.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                showErrorAlert("Lỗi kết nối", "Không thể kết nối đến cơ sở dữ liệu.");
                return;
            }

            // Kiểm tra bảng seats
            String checkTableQuery = "SELECT 1 FROM information_schema.tables WHERE table_schema = 'dbo' AND table_name = 'seats'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkTableQuery);
                 ResultSet checkRs = checkStmt.executeQuery()) {
                if (!checkRs.next()) {
                    showErrorAlert("Lỗi cơ sở dữ liệu", "Bảng seats không tồn tại.");
                    return;
                }
            }

            // Lấy danh sách ghế
            String seatQuery = "SELECT s.seatId, s.seatRow, s.seatColumn, st.seatTypeName, s.seatTypeId, s.isActive " +
                    "FROM seats s JOIN seatTypes st ON s.seatTypeId = st.seatTypeId " +
                    "WHERE s.roomId = ? AND s.isActive = 1";
            try (PreparedStatement seatStmt = conn.prepareStatement(seatQuery)) {
                seatStmt.setInt(1, selectedShowtime.getRoomId());
                ResultSet seatRs = seatStmt.executeQuery();

                // Lấy danh sách ghế đã đặt
                String bookedSeatQuery = "SELECT ts.seatId FROM ticketSeats ts JOIN tickets t ON ts.ticketId = t.ticketId WHERE t.showtimeId = ?";
                try (PreparedStatement bookedStmt = conn.prepareStatement(bookedSeatQuery)) {
                    bookedStmt.setInt(1, selectedShowtime.getShowtimeId());
                    ResultSet bookedRs = bookedStmt.executeQuery();
                    Set<Integer> bookedSeatIds = new HashSet<>();
                    while (bookedRs.next()) {
                        bookedSeatIds.add(bookedRs.getInt("seatId"));
                    }

                    int seatCount = 0;
                    while (seatRs.next()) {
                        int seatId = seatRs.getInt("seatId");
                        String seatRow = seatRs.getString("seatRow");
                        int seatColumn = seatRs.getInt("seatColumn");
                        String seatTypeName = seatRs.getString("seatTypeName");
                        int seatTypeId = seatRs.getInt("seatTypeId");
                        boolean isActive = seatRs.getBoolean("isActive");

                        if (isActive) {
                            Button seat = new Button(seatRow + seatColumn);
                            seat.setPrefSize(45, 45);
                            seat.setMinSize(45, 45);
                            seat.setMaxSize(45, 45);
                            seat.setAccessibleText("Seat " + seatRow + seatColumn + ", " + seatTypeName + ", " +
                                    (bookedSeatIds.contains(seatId) ? "Booked" : "Available"));

                            SeatInfo seatInfo = getSeatInfo(seatTypeName);
                            boolean isSelected = selectedSeats.stream()
                                    .anyMatch(s -> (int) s.get("seatId") == seatId);

                            if (bookedSeatIds.contains(seatId)) {
                                seat.setStyle(getOccupiedSeatStyle());
                                seat.setDisable(true);
                            } else if (isSelected) {
                                seat.setStyle(getSelectedSeatStyle(seatInfo.color));
                                // Cập nhật button trong selectedSeats
                                selectedSeats.stream()
                                        .filter(s -> (int) s.get("seatId") == seatId)
                                        .findFirst()
                                        .ifPresent(s -> s.put("button", seat));
                            } else {
                                seat.setStyle(getAvailableSeatStyle(seatInfo.color));
                            }

                            setupSeatInteraction(seat, seatInfo, seatId, seatRow, seatColumn, seatTypeName, seatTypeId);
                            seatGrid.add(seat, seatColumn - 1, seatRow.charAt(0) - 'A');
                            seatCount++;
                        }
                    }
                    totalSeats = seatCount;
                    updateSeatInfo();

                    if (seatCount == 0) {
                        showErrorAlert("Không có ghế", "Không tìm thấy ghế khả dụng cho phòng này.");
                    }
                }
            }
        } catch (SQLException ex) {
            showErrorAlert("Lỗi cơ sở dữ liệu", "Không thể tải danh sách ghế: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void setBookingData(Map<String, Object> bookingData) {
        this.bookingData = bookingData;
        if (bookingData != null) {
            this.selectedShowtime = (Showtime) bookingData.get("showtime");
            this.selectedSeats = (List<Map<String, Object>>) bookingData.get("selectedSeats");
            this.seatTypePrices = (Map<Integer, Double>) bookingData.get("seatTypePrices");

            if (selectedSeats == null) {
                selectedSeats = new ArrayList<>();
            }
            if (seatTypePrices == null) {
                seatTypePrices = new HashMap<>();
            }

            displayShowtimeInfo();
            loadSeatTypePrices();
            loadSeats();
            updateTotalPrice();
        } else {
            showErrorAlert("Dữ liệu không hợp lệ", "Không tìm thấy thông tin đặt vé. Vui lòng thử lại.");
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupSeatInteraction(Button seat, SeatInfo seatInfo, int seatId, String seatRow, int seatColumn, String seatTypeName, int seatTypeId) {
        seat.setOnMouseEntered(e -> {
            seat.setStyle(getHoverSeatStyle(seatInfo.color));
            seat.setScaleX(1.1);
            seat.setScaleY(1.1);
        });
        seat.setOnMouseExited(e -> {
            Map<String, Object> selectedSeat = selectedSeats.stream()
                    .filter(s -> (int)s.get("seatId") == seatId)
                    .findFirst()
                    .orElse(null);
            seat.setStyle(selectedSeat != null ? getSelectedSeatStyle(seatInfo.color) : getAvailableSeatStyle(seatInfo.color));
            seat.setScaleX(1.0);
            seat.setScaleY(1.0);
        });
        seat.setOnAction(e -> handleSeatSelection(seat, seatId, seatRow, seatColumn, seatTypeName, seatTypeId));
    }

    private SeatInfo getSeatInfo(String seatTypeName) {
        switch (seatTypeName.toLowerCase()) {
            case "standard":
                return new SeatInfo("#FF6B9D", "Standard");
            case "vip":
                return new SeatInfo("#3498DB", "VIP");
            case "sweetbox":
                return new SeatInfo("#E74C3C", "Sweet Box");
            default:
                return new SeatInfo("#7f8c8d", "Unknown");
        }
    }

    private String getAvailableSeatStyle(String color) {
        return String.format("-fx-background-color: %s; " +
                "-fx-background-radius: 10px; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 12px; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: #34495e; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 10px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 3, 0, 0, 1);", color);
    }

    private String getHoverSeatStyle(String color) {
        return String.format("-fx-background-color: derive(%s, -20%%); " +
                "-fx-background-radius: 10px; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 12px; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: #2c3e50; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 10px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0, 0, 2);", color);
    }

    private String getSelectedSeatStyle(String color) {
        return String.format("-fx-background-color: derive(%s, -40%%); " +
                "-fx-background-radius: 10px; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 12px; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: #2c3e50; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 10px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0, 0, 2);", color);
    }

    private String getOccupiedSeatStyle() {
        return "-fx-background-color: #7f8c8d; " +
                "-fx-background-radius: 10px; " +
                "-fx-text-fill: #bdc3c7; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 12px; " +
                "-fx-border-color: #95a5a6; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 10px; " +
                "-fx-opacity: 0.6;";
    }

    private void updateSeatInfo() {
        if (totalSeatsLabel != null) {
            totalSeatsLabel.setText("Total Seats: " + totalSeats);
        }
    }

=======
        System.out.println("Tạo lưới ghế từ cơ sở dữ liệu");
        System.out.println("roomId: " + selectedShowtime.getRoomId());

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Lỗi: Không thể kết nối đến cơ sở dữ liệu!");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi kết nối");
                alert.setHeaderText(null);
                alert.setContentText("Không thể kết nối đến cơ sở dữ liệu. Vui lòng kiểm tra lại.");
                alert.showAndWait();
                return;
            }

            // Kiểm tra sự tồn tại của bảng seats
            String checkTableQuery = "SELECT 1 FROM information_schema.tables WHERE table_schema = 'dbo' AND table_name = 'seats'";
            PreparedStatement checkStmt = conn.prepareStatement(checkTableQuery);
            ResultSet checkRs = checkStmt.executeQuery();
            if (!checkRs.next()) {
                System.err.println("Lỗi: Bảng seats không tồn tại trong cơ sở dữ liệu!");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi cơ sở dữ liệu");
                alert.setHeaderText(null);
                alert.setContentText("Bảng seats không tồn tại. Vui lòng kiểm tra cơ sở dữ liệu.");
                alert.showAndWait();
                return;
            }

            // Lấy danh sách ghế từ bảng seats, join với SeatType
            String seatQuery = "SELECT s.seatId, s.seatRow, s.seatColumn, st.seatTypeName, s.seatTypeId, s.isActive " +
                    "FROM seats s " +
                    "JOIN seatTypes st ON s.seatTypeId = st.seatTypeId " +
                    "WHERE s.roomId = ? AND s.isActive = 1";
            PreparedStatement seatStmt = conn.prepareStatement(seatQuery);
            seatStmt.setInt(1, selectedShowtime.getRoomId());
            ResultSet seatRs = seatStmt.executeQuery();

            // Lấy danh sách seatId đã đặt từ bảng ticketSeats
            String bookedSeatQuery = "SELECT ts.seatId FROM ticketSeats ts JOIN tickets t ON ts.ticketId = t.ticketId WHERE t.showtimeId = ?";
            PreparedStatement bookedStmt = conn.prepareStatement(bookedSeatQuery);
            bookedStmt.setInt(1, selectedShowtime.getShowtimeId());
            ResultSet bookedRs = bookedStmt.executeQuery();
            Set<Integer> bookedSeatIds = new HashSet<>();
            while (bookedRs.next()) {
                bookedSeatIds.add(bookedRs.getInt("seatId"));
            }
            System.out.println("Số ghế đã đặt: " + bookedSeatIds.size());

            int seatCount = 0;
            while (seatRs.next()) {
                int seatId = seatRs.getInt("seatId");
                String seatRow = seatRs.getString("seatRow");
                int seatColumn = seatRs.getInt("seatColumn");
                String seatTypeName = seatRs.getString("seatTypeName");
                int seatTypeId = seatRs.getInt("seatTypeId");
                boolean isActive = seatRs.getBoolean("isActive");

                if (isActive) {
                    Button seat = new Button(seatRow + seatColumn);
                    seat.setPrefSize(40, 40);

                    // Áp dụng màu theo seatTypeName
                    String seatColor;
                    switch (seatTypeName.toLowerCase()) {
                        case "standard":
                            seatColor = "#ff9999"; // Hồng
                            break;
                        case "vip":
                            seatColor = "#66ccff"; // Xanh da trời sáng
                            break;
                        case "sweetbox":
                            seatColor = "#ff0000"; // Đỏ
                            break;
                        default:
                            seatColor = "#ccc"; // Mặc định
                            break;
                    }

                    // Kiểm tra ghế đã đặt
                    if (bookedSeatIds.contains(seatId)) {
                        seatColor = "#808080"; // Xám cho ghế đã đặt
                        seat.setDisable(true); // Vô hiệu hóa ghế đã đặt
                    }

                    seat.setStyle("-fx-background-color: " + seatColor + "; -fx-background-radius: 5px; -fx-text-fill: black;");
                    seat.setOnAction(e -> handleSeatSelection(seat, seatId, seatRow, seatColumn, seatTypeName, seatTypeId));

                    // Thêm ghế vào seatGrid
                    seatGrid.add(seat, seatColumn - 1, seatRow.charAt(0) - 'A');
                    seatCount++;
                    System.out.println("Ghế " + seatRow + seatColumn + ": " + seatTypeName + ", color: " + seatColor);
                }
            }
            System.out.println("Số ghế được thêm vào seatGrid: " + seatCount);
            if (seatCount == 0) {
                System.out.println("Cảnh báo: Không có ghé nào được tải từ bảng seats cho roomId = " + selectedShowtime.getRoomId());
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Không có ghế");
                alert.setHeaderText(null);
                alert.setContentText("Không tìm thấy ghế khả dụng cho phòng này. Vui lòng kiểm tra dữ liệu.");
                alert.showAndWait();
            }

        } catch (SQLException ex) {
            System.err.println("Lỗi khi truy vấn ghế từ CSDL: " + ex.getMessage());
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi cơ sở dữ liệu");
            alert.setHeaderText(null);
            alert.setContentText("Không thể tải danh sách ghế: " + ex.getMessage());
            alert.showAndWait();
        }
    }

>>>>>>> 44fab4a810779ded600d5ec37ab3c472deb80785
    private void handleSeatSelection(Button seat, int seatId, String seatRow, int seatColumn, String seatTypeName, int seatTypeId) {
        // Kiểm tra xem ghế đã được chọn chưa
        Map<String, Object> selectedSeat = selectedSeats.stream()
                .filter(s -> (int)s.get("seatId") == seatId)
                .findFirst()
                .orElse(null);

        if (selectedSeat == null) {
            // Chọn ghế
            seat.setStyle("-fx-background-color: #66ccff; -fx-text-fill: white; -fx-background-radius: 5px;");
            Map<String, Object> seatInfo = new HashMap<>();
            seatInfo.put("seatId", seatId);
            seatInfo.put("seatRow", seatRow);
            seatInfo.put("seatColumn", seatColumn);
            seatInfo.put("seatTypeName", seatTypeName);
            seatInfo.put("seatTypeId", seatTypeId);
            seatInfo.put("button", seat);
            selectedSeats.add(seatInfo);
            System.out.println("Chọn ghế: " + seatRow + seatColumn + " (" + seatTypeName + ")");
        } else {
            // Bỏ chọn ghế
            selectedSeats.remove(selectedSeat);
            String seatColor;
            switch (seatTypeName.toLowerCase()) {
                case "standard":
                    seatColor = "#ff9999";
                    break;
                case "vip":
                    seatColor = "#66ccff";
                    break;
                case "sweetbox":
                    seatColor = "#ff0000";
                    break;
                default:
                    seatColor = "#ccc";
                    break;
            }
            seat.setStyle("-fx-background-color: " + seatColor + "; -fx-background-radius: 5px; -fx-text-fill: black;");
            System.out.println("Bỏ chọn ghế: " + seatRow + seatColumn + " (" + seatTypeName + ")");
        }
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        double totalPrice = selectedSeats.stream()
                .mapToDouble(seat -> seatTypePrices.getOrDefault((int)seat.get("seatTypeId"), 0.0))
                .sum();
        totalPriceLabel.setText(String.format("Tổng tiền: %.2f", totalPrice));
    }

    @FXML
    private void handleCancel() {
        for (Map<String, Object> seatInfo : selectedSeats) {
            Button seat = (Button) seatInfo.get("button");
            String seatTypeName = (String) seatInfo.get("seatTypeName");
            String seatColor;
            switch (seatTypeName.toLowerCase()) {
                case "standard":
                    seatColor = "#ff9999";
                    break;
                case "vip":
                    seatColor = "#66ccff";
                    break;
                case "sweetbox":
                    seatColor = "#ff0000";
                    break;
                default:
                    seatColor = "#ccc";
                    break;
            }
            seat.setStyle("-fx-background-color: " + seatColor + "; -fx-background-radius: 5px; -fx-text-fill: black;");
        }
        selectedSeats.clear();
        updateTotalPrice();
        System.out.println("Hủy tất cả ghế đã chọn");
    }

    @FXML
    private void handleConfirm() {
        if (selectedSeats.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Chưa chọn ghế");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng chọn ít nhất một ghế trước khi xác nhận.");
            alert.showAndWait();
            return;
        }

        User currentUser = Session.getCurrentUser();
        if (currentUser == null || currentUser.getUserId() == 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi thông tin nhân viên");
            alert.setHeaderText(null);
            alert.setContentText("Không tìm thấy thông tin nhân viên. Vui lòng đăng nhập lại.");
            alert.showAndWait();
            return;
        }

        if (selectedShowtime == null || selectedShowtime.getShowtimeId() <= 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi suất chiếu");
            alert.setHeaderText(null);
            alert.setContentText("Suất chiếu không hợp lệ. Vui lòng chọn lại suất chiếu.");
            alert.showAndWait();
            return;
        }

        // Lưu thông tin tạm thời vào Session thay vì tạo vé ngay
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("showtime", selectedShowtime);
        bookingData.put("selectedSeats", new ArrayList<>(selectedSeats));
        bookingData.put("seatTypePrices", new HashMap<>(seatTypePrices));
        bookingData.put("currentUser", currentUser);

        // Tính tổng tiền ghế
        double seatsTotalPrice = selectedSeats.stream()
                .mapToDouble(seat -> seatTypePrices.getOrDefault((int)seat.get("seatTypeId"), 0.0))
                .sum();
        bookingData.put("seatsTotalPrice", seatsTotalPrice);

        // Lưu vào Session
        Session.setBookingData(bookingData);

        System.out.println("Đã lưu thông tin đặt ghế vào Session. Chuyển sang SellAddons...");

        try {
            // Chuyển sang trang SellAddons
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/SellAddons.fxml"));
            Parent sellAddonsRoot = loader.load();

            // Lấy controller SellAddons và truyền dữ liệu
            SellAddonsController controller = loader.getController();
            controller.setBookingData(bookingData);

            AnchorPane parent = (AnchorPane) confirmButton.getScene().lookup("#contentArea");
            if (parent != null) {
                parent.getChildren().setAll(sellAddonsRoot);
                AnchorPane.setTopAnchor(sellAddonsRoot, 0.0);
                AnchorPane.setBottomAnchor(sellAddonsRoot, 0.0);
                AnchorPane.setLeftAnchor(sellAddonsRoot, 0.0);
                AnchorPane.setRightAnchor(sellAddonsRoot, 0.0);
            } else {
                System.err.println("Không tìm thấy #contentArea trong scene!");
            }

        } catch (IOException ex) {
            System.err.println("Lỗi khi tải SellAddons.fxml: " + ex.getMessage());
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Không thể chuyển sang trang bán addon: " + ex.getMessage());
            alert.showAndWait();
        }
    }
}