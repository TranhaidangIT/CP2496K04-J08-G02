package controller.controllerEmployees;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import java.io.IOException;

public class SeatDemoController {

    @FXML private Label messageLabel;
    @FXML private GridPane seatGrid;
    @FXML private Button backButton;

    @FXML
    public void initialize() {
        System.out.println("Khởi tạo SeatDemoController");
        if (seatGrid == null) {
            System.err.println("seatGrid is null! Kiểm tra fx:id trong SeatDemo.fxml");
        }
        if (messageLabel == null) {
            System.err.println("messageLabel is null! Kiểm tra fx:id trong SeatDemo.fxml");
        }
        if (backButton == null) {
            System.err.println("backButton is null! Kiểm tra fx:id trong SeatDemo.fxml");
        }
        loadDemoSeats();
    }

    private void loadDemoSeats() {
        seatGrid.getChildren().clear();
        System.out.println("Tạo lưới ghế giả lập 10x10");
        // Tạo lưới ghế giả lập 10x10
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                Button seat = new Button((char)('A' + row) + String.valueOf(col + 1));
                seat.setPrefSize(40, 40);
                // Áp dụng màu theo hàng
                String seatColor;
                String seatType;
                if (row < 3) { // Hàng A, B, C (Standard)
                    seatColor = "#FF66CC"; // Hồng
                    seatType = "Standard";
                } else if (row == 9) { // Hàng J (SweetBox)
                    seatColor = "#ff0000"; // Đỏ
                    seatType = "SweetBox";
                } else { // Hàng D, E, F, G, H, I (VIP)
                    seatColor = "#66ccff"; // Xanh da trời sáng
                    seatType = "VIP";
                }
                seat.setStyle("-fx-background-color: " + seatColor + "; -fx-background-radius: 5px; -fx-text-fill: black;");
                seat.setOnAction(e -> {
                    System.out.println("Nhấn ghế giả lập: " + seat.getText() + " (" + seatType + ")");
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Chưa chọn suất chiếu");
                    alert.setHeaderText(null);
                    alert.setContentText("Vui lòng chọn một phim và suất chiếu trước khi chọn ghế.");
                    alert.showAndWait();
                    handleBack();
                });
                seatGrid.add(seat, col, row);
            }
        }
        System.out.println("Số ghế được thêm vào seatGrid: " + seatGrid.getChildren().size());
    }

    @FXML
    private void handleBack() {
        try {
            System.out.println("Nhấn Quay lại chọn phim, tải ListMovies.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/ListMovies.fxml"));
            Parent listMoviesRoot = loader.load();
            ListMoviesController controller = loader.getController();
            AnchorPane parent = (AnchorPane) backButton.getScene().lookup("#contentArea");
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
        } catch (IOException ex) {
            System.err.println("Lỗi khi tải ListMovies.fxml: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}