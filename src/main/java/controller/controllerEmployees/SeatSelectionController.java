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
    @FXML private Label totalSeatsLabel;
    @FXML private Button cancelButton;
    @FXML private Button confirmButton;

    private Showtime selectedShowtime;
    private List<Map<String, Object>> selectedSeats;
    private Map<String, Object> bookingData;
    private Map<Integer, Double> seatTypePrices;
    private int totalSeats = 0;

    @FXML
    public void initialize() {
        System.out.println("Initializing SeatSelectionController");
        selectedSeats = new ArrayList<>();
        seatTypePrices = new HashMap<>();
        if (showtimeLabel == null || seatGrid == null || totalPriceLabel == null ||
                totalSeatsLabel == null || cancelButton == null || confirmButton == null) {
            System.err.println("Error: One or more fx:id not properly initialized in SeatSelection.fxml");
        }
        updateTotalPrice();
    }

    public void setData(Showtime showtime) {
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("showtime", showtime);
        bookingData.put("selectedSeats", new ArrayList<Map<String, Object>>());
        bookingData.put("seatTypePrices", new HashMap<Integer, Double>());
        setBookingData(bookingData);
    }

    private void displayShowtimeInfo() {
        System.out.println("Movie: " + selectedShowtime.getMovieTitle());
        System.out.println("Room: " + selectedShowtime.getRoomName());
        System.out.println("Date: " + selectedShowtime.getShowDate());
        System.out.println("Start Time: " + selectedShowtime.getShowTime());
        System.out.println("End Time: " + selectedShowtime.getEndTime());

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
            System.out.println("Loaded seat prices: " + seatTypePrices);
        } catch (SQLException ex) {
            System.err.println("Error loading SeatType prices: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadSeats() {
        seatGrid.getChildren().clear();
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

    private void handleSeatSelection(Button seat, int seatId, String seatRow, int seatColumn, String seatTypeName, int seatTypeId) {
        Map<String, Object> selectedSeat = selectedSeats.stream()
                .filter(s -> (int)s.get("seatId") == seatId)
                .findFirst()
                .orElse(null);

        SeatInfo seatInfo = getSeatInfo(seatTypeName);
        if (selectedSeat == null) {
            seat.setStyle(getSelectedSeatStyle(seatInfo.color));
            Map<String, Object> seatInfoMap = new HashMap<>();
            seatInfoMap.put("seatId", seatId);
            seatInfoMap.put("seatRow", seatRow);
            seatInfoMap.put("seatColumn", seatColumn);
            seatInfoMap.put("seatTypeName", seatTypeName);
            seatInfoMap.put("seatTypeId", seatTypeId);
            seatInfoMap.put("button", seat);
            selectedSeats.add(seatInfoMap);
            System.out.println("Selected seat: " + seatRow + seatColumn + " (" + seatTypeName + ")");
        } else {
            selectedSeats.remove(selectedSeat);
            seat.setStyle(getAvailableSeatStyle(seatInfo.color));
            System.out.println("Deselected seat: " + seatRow + seatColumn + " (" + seatTypeName + ")");
        }
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        double totalPrice = selectedSeats.stream()
                .mapToDouble(seat -> seatTypePrices.getOrDefault((int)seat.get("seatTypeId"), 0.0))
                .sum();
        totalPriceLabel.setText(String.format("Total Price: %.2f", totalPrice));
    }

    @FXML
    private void handleCancel() {
        for (Map<String, Object> seatInfo : selectedSeats) {
            Button seat = (Button) seatInfo.get("button");
            SeatInfo seatTypeInfo = getSeatInfo((String) seatInfo.get("seatTypeName"));
            seat.setStyle(getAvailableSeatStyle(seatTypeInfo.color));
        }
        selectedSeats.clear();
        updateTotalPrice();
        System.out.println("Cancelled all selected seats");
    }

    @FXML
    private void handleConfirm() {
        if (selectedSeats.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Seats Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select at least one seat before confirming.");
            alert.showAndWait();
            return;
        }

        User currentUser = Session.getCurrentUser();
        if (currentUser == null || currentUser.getUserId() == 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("User Information Error");
            alert.setHeaderText(null);
            alert.setContentText("User information not found. Please log in again.");
            alert.showAndWait();
            return;
        }

        if (selectedShowtime == null || selectedShowtime.getShowtimeId() <= 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Showtime Error");
            alert.setHeaderText(null);
            alert.setContentText("Invalid showtime. Please select a showtime again.");
            alert.showAndWait();
            return;
        }

        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("showtime", selectedShowtime);
        bookingData.put("selectedSeats", new ArrayList<>(selectedSeats));
        bookingData.put("seatTypePrices", new HashMap<>(seatTypePrices));
        bookingData.put("currentUser", currentUser);

        double seatsTotalPrice = selectedSeats.stream()
                .mapToDouble(seat -> seatTypePrices.getOrDefault((int)seat.get("seatTypeId"), 0.0))
                .sum();
        bookingData.put("seatsTotalPrice", seatsTotalPrice);

        Session.setBookingData(bookingData);
        System.out.println("Saved seat selection data to Session. Navigating to SellAddons...");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/SellAddons.fxml"));
            Parent sellAddonsRoot = loader.load();
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
                System.err.println("Could not find #contentArea in the scene!");
                confirmButton.getScene().setRoot(sellAddonsRoot);
            }
        } catch (IOException ex) {
            System.err.println("Error loading SellAddons.fxml: " + ex.getMessage());
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Unable to navigate to add-ons page: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    private static class SeatInfo {
        final String color;
        final String type;

        SeatInfo(String color, String type) {
            this.color = color;
            this.type = type;
        }
    }
}