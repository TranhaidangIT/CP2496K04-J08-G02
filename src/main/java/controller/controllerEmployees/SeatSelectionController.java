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
import configs.DBConnection;
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
import java.util.UUID;

public class SeatSelectionController {

    @FXML private Label showtimeLabel;
    @FXML private GridPane seatGrid;
    @FXML private Label totalPriceLabel;
    @FXML private Button cancelButton;
    @FXML private Button confirmButton;

    private Showtime selectedShowtime;
    private List<Map<String, Object>> selectedSeats; // Lưu seatId, seatRow, seatColumn, seatTypeName, price
    private Map<Integer, Double> seatTypePrices; // Lưu seatTypeId -> price

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
        this.selectedShowtime = showtime;
        System.out.println("setData called with showtime: " + selectedShowtime);
        if (selectedShowtime == null) {
            System.err.println("Lỗi: Showtime là null trong SeatSelectionController!");
            return;
        }
        seatGrid.getChildren().clear();
        selectedSeats.clear();
        updateTotalPrice();
        displayShowtimeInfo();
        loadSeatTypePrices();
        loadSeats();
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
                        selectedShowtime.getShowDate() + " " + selectedShowtime.getShowTime()
        );
    }

    private void loadSeatTypePrices() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT seatTypeId, price FROM SeatType";
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

            // Kiểm tra sự tồn tại của bảng Seat
            String checkTableQuery = "SELECT 1 FROM information_schema.tables WHERE table_schema = 'dbo' AND table_name = 'Seat'";
            PreparedStatement checkStmt = conn.prepareStatement(checkTableQuery);
            ResultSet checkRs = checkStmt.executeQuery();
            if (!checkRs.next()) {
                System.err.println("Lỗi: Bảng Seat không tồn tại trong cơ sở dữ liệu!");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi cơ sở dữ liệu");
                alert.setHeaderText(null);
                alert.setContentText("Bảng Seat không tồn tại. Vui lòng kiểm tra cơ sở dữ liệu.");
                alert.showAndWait();
                return;
            }

            // Lấy danh sách ghế từ bảng Seat, join với SeatType
            String seatQuery = "SELECT s.seatId, s.seatRow, s.seatColumn, st.seatTypeName, s.seatTypeId, s.isActive " +
                    "FROM Seat s " +
                    "JOIN SeatType st ON s.seatTypeId = st.seatTypeId " +
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
                System.out.println("Cảnh báo: Không có ghế nào được tải từ bảng Seat cho roomId = " + selectedShowtime.getRoomId());
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

        try (Connection conn = DBConnection.getConnection()) {
            // Tạo ticketCode duy nhất
            String ticketCode = "TICKET_" + UUID.randomUUID().toString().substring(0, 8);

            // Kiểm tra ticketCode không trùng lặp
            String checkQuery = "SELECT COUNT(*) FROM tickets WHERE ticketCode = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, ticketCode);
            ResultSet checkRs = checkStmt.executeQuery(); // Sửa từ executeResultSet()
            checkRs.next();
            if (checkRs.getInt(1) > 0) {
                // Nếu trùng, tạo ticketCode mới
                ticketCode = "TICKET_" + UUID.randomUUID().toString().substring(0, 8);
            }

            // Tạo ticket mới
            String ticketQuery = "INSERT INTO tickets (ticketCode, showtimeId, totalPrice) OUTPUT INSERTED.ticketId VALUES (?, ?, ?)";
            PreparedStatement ticketStmt = conn.prepareStatement(ticketQuery);
            ticketStmt.setString(1, ticketCode);
            ticketStmt.setInt(2, selectedShowtime.getShowtimeId());
            double totalPrice = selectedSeats.stream()
                    .mapToDouble(seat -> seatTypePrices.getOrDefault((int)seat.get("seatTypeId"), 0.0))
                    .sum();
            ticketStmt.setDouble(3, totalPrice);
            ResultSet ticketRs = ticketStmt.executeQuery();
            int ticketId = 0;
            if (ticketRs.next()) {
                ticketId = ticketRs.getInt("ticketId");
            }

            // Thêm ghế vào ticketSeats
            String seatQuery = "INSERT INTO ticketSeats (ticketId, seatId) VALUES (?, ?)";
            PreparedStatement seatStmt = conn.prepareStatement(seatQuery);
            for (Map<String, Object> seatInfo : selectedSeats) {
                seatStmt.setInt(1, ticketId);
                seatStmt.setInt(2, (int) seatInfo.get("seatId"));
                seatStmt.addBatch();
            }
            seatStmt.executeBatch();

            System.out.println("Đã lưu " + selectedSeats.size() + " ghế vào ticketSeats cho ticketId = " + ticketId + ", ticketCode = " + ticketCode);

            // Chuyển về ListMovies.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/ListMovies.fxml"));
            Parent listMoviesRoot = loader.load();
            ListMoviesController controller = loader.getController();
            AnchorPane parent = (AnchorPane) confirmButton.getScene().lookup("#contentArea");
            if (parent != null) {
                controller.setContentArea(parent);
                parent.getChildren().setAll(listMoviesRoot);
                AnchorPane.setTopAnchor(listMoviesRoot, 0.0);
                AnchorPane.setBottomAnchor(listMoviesRoot, 0.0);
                AnchorPane.setLeftAnchor(listMoviesRoot, 0.0);
                AnchorPane.setRightAnchor(listMoviesRoot, 0.0);
            } else {
                System.err.println("Không tìm thấy #contentArea trong scene!");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Xác nhận thành công");
            alert.setHeaderText(null);
            alert.setContentText("Đã đặt " + selectedSeats.size() + " ghế thành công! Mã vé: " + ticketCode);
            alert.showAndWait();

        } catch (SQLException ex) {
            System.err.println("Lỗi khi lưu ticket/ticketSeats: " + ex.getMessage());
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi cơ sở dữ liệu");
            alert.setHeaderText(null);
            alert.setContentText("Không thể lưu vé: " + ex.getMessage());
            alert.showAndWait();
        } catch (IOException ex) {
            System.err.println("Lỗi khi tải ListMovies.fxml: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}