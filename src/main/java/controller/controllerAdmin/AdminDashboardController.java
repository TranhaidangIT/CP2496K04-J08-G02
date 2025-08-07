package controller.controllerAdmin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminDashboardController {

    @FXML private BorderPane mainBorderPane;
    @FXML private AnchorPane centerContent;
    @FXML private Label helloUser;

    @FXML private Button btnOverview;
    @FXML private Button btnUser;
    @FXML private Button btnTicketsHistory;
    @FXML private Button btnLogout;

    @FXML
    public void initialize() {
        helloUser.setText("Hello, Admin");
        // Tải trang DashboardOverview mặc định khi khởi động
        loadContent("/views/DashboardOverview.fxml");
        highlightButton(btnOverview);
    }

    /**
     * Phương thức tiện ích để tải và hiển thị nội dung FXML mới vào AnchorPane.
     * @param fxmlPath Đường dẫn đến file FXML cần tải.
     */
    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newContent = fxmlLoader.load();

            centerContent.getChildren().clear();
            centerContent.getChildren().add(newContent);

            // Neo nội dung mới vào tất cả các cạnh của AnchorPane
            // Điều này đảm bảo nó lấp đầy không gian có sẵn
            AnchorPane.setTopAnchor(newContent, 0.0);
            AnchorPane.setBottomAnchor(newContent, 0.0);
            AnchorPane.setLeftAnchor(newContent, 0.0);
            AnchorPane.setRightAnchor(newContent, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load content from path: " + fxmlPath);
        }
    }

    /**
     * Phương thức tô sáng nút được chọn và loại bỏ hiệu ứng tô sáng ở các nút khác.
     * @param activeButton Nút cần tô sáng.
     */
    private void highlightButton(Button activeButton) {
        // Reset style của tất cả các nút
        btnOverview.getStyleClass().remove("btnSelect:selected");
        btnUser.getStyleClass().remove("btnSelect:selected");
        btnTicketsHistory.getStyleClass().remove("btnSelect:selected");

        // Thêm style cho nút được chọn
        if (activeButton != null) {
            activeButton.getStyleClass().add("btnSelect:selected");
        }
    }

    // ===================================
    // === Xử lý sự kiện khi nhấn nút ===
    // ===================================

    @FXML
    private void handleDashboardClick(ActionEvent event) {
        loadContent("/views/fxml_Admin/DashboardOverview.fxml");
        highlightButton(btnOverview);
    }

    @FXML
    private void handleUserManageClick(ActionEvent event) {
        loadContent("/views/fxml_Admin/UserManagementContent.fxml");
        highlightButton(btnUser);
    }

    @FXML
    private void handleTicketsHistoryClick(ActionEvent event) {
        loadContent("/views/fxml_Admin/TicketHistoryContent.fxml");
        highlightButton(btnTicketsHistory);
    }

    @FXML
    private void handleLogoutClicked(ActionEvent event) {
        try {
            // Lấy Stage hiện tại
            Stage stage = (Stage) mainBorderPane.getScene().getWindow();

            // Tải file login FXML
            Parent loginPage = FXMLLoader.load(getClass().getResource("/views/fxml_Admin/Login.fxml"));

            // Tạo một Scene mới và thiết lập nó cho Stage
            Scene scene = new Scene(loginPage);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}