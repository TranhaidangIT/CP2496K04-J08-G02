package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
<<<<<<< HEAD
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
=======
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
>>>>>>> 44fab4a810779ded600d5ec37ab3c472deb80785

import java.io.IOException;

public class DashboardController {

    // Avatar hiển thị theo vai trò
    @FXML private ImageView avrManager;
    @FXML private ImageView avtAdmin;
    @FXML private ImageView avtEmployee;

    // Các nút điều hướng cho Admin
    @FXML private Button btnTicketsHistory;
    @FXML private Button btnUser;
    @FXML private Button btnOverviewAdmin;
    @FXML private Button btnLogoutAdmin;

    // Các nút điều hướng cho Manager
    @FXML private Button btnMovie;
    @FXML private Button btnRoom;
    @FXML private Button btnService;
    @FXML private Button btnShowtime;
    @FXML private Button btnOverviewManager;
    @FXML private Button btnLogoutManager;

    // Các nút điều hướng cho Employee
    @FXML private Button btnMovieList;
    @FXML private Button btnLocker;
    @FXML private Button btnSellTicket;
    @FXML private Button btnTotal;
    @FXML private Button btnOverviewEmp;
    @FXML private Button btnLogoutEmp;

    // Label chào mừng theo vai trò
    @FXML private Label helloAdmin;
    @FXML private Label helloManager;
    @FXML private Label helloEmployee;

    // Vùng nội dung chính để load các FXML
    @FXML private Pane mainContent;

    // Sidebar riêng cho từng vai trò
    @FXML private VBox sideBarAdmin;
    @FXML private VBox sideBarManager;
    @FXML private VBox sideBarEmployee;

    // Tạm thời hardcode role do chưa có chức năng đăng nhập
    private String currentRole = "manager" ;

    @FXML
    public void initialize() {
        // Ẩn toàn bộ giao diện theo role
        sideBarAdmin.setVisible(false);
        sideBarManager.setVisible(false);
        sideBarEmployee.setVisible(false);

        avtAdmin.setVisible(false);
        avrManager.setVisible(false);
        avtEmployee.setVisible(false);

        helloAdmin.setVisible(false);
        helloManager.setVisible(false);
        helloEmployee.setVisible(false);

        // Hiển thị giao diện theo vai trò
        switch (currentRole.toLowerCase()) {
            case "admin":
                sideBarAdmin.setVisible(true);
                avtAdmin.setVisible(true);
                helloAdmin.setVisible(true);
                break;
            case "manager":
                sideBarManager.setVisible(true);
                avrManager.setVisible(true);
                helloManager.setVisible(true);
                break;
            case "employee":
                sideBarEmployee.setVisible(true);
                avtEmployee.setVisible(true);
                helloEmployee.setVisible(true);
                break;
            default:
                System.out.println();
        }
    }

    private void loadUI(String fxmlPath) {
        try {
            Pane content = FXMLLoader.load(getClass().getResource(fxmlPath));
            mainContent.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // ----------------- Event Handler theo Role -----------------

    // Admin
    @FXML
    void handleTicketsHistoryClick(ActionEvent event) {
        loadUI("/views/fxml_Admin/");
    }

    @FXML
    void handleUserManageClick(ActionEvent event) {
        loadUI("/views/fxml_Admin/UserManagementContent.fxml");
    }

    @FXML
    void handleDashboardClick(ActionEvent event) {


    }


    // ------------------MANAGER-------------------
    @FXML
    void onMovieClickedbyManager(ActionEvent event) {

        loadUI("/views/fxml_Manager/Movie/MovieList.fxml");
    }

    @FXML
    void onRoomClickedbbyManager(ActionEvent event) {

        loadUI("/views/fxml_Manager/ScreeningRoom/RoomList.fxml");
    }

    @FXML
    void onServiceClickedbyManager(ActionEvent event) {

        loadUI("/views/fxml_Manager");
    }

    @FXML
    void onShowtimeClickedbyManager(ActionEvent event) {

        loadUI("/views/fxml_Manager/Showtime/ShowtimeList.fxml");
    }

    @FXML
    void onOverviewClickedbyManager(ActionEvent event) {

        loadUI("/views/fxml_Manager/Overview.fxml");
    }


    // -----------------EMPLOYEE--------------------------
    @FXML
    void onMovieClickedbyEmp(ActionEvent event) {

        loadUI("views/");
    }

    @FXML
<<<<<<< HEAD
    private void onLockerClicked() { loadUI("/views/fxml_Manager/Locker/LockerList.fxml");}

    @FXML
    private void onLogoutClicked(ActionEvent event) {
        try {
            Node source = (Node) event.getSource();
            Parent root = FXMLLoader.load(getClass().getResource("/views/fxml_Admin/login.fxml"));
            source.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
=======
    void onLockerClickedbyEmp(ActionEvent event) {

        System.out.println("Employee clicked Locker");
    }

    @FXML
    void onSellTicketClickedbyEmp(ActionEvent event) {
        System.out.println("Employee clicked Sell Ticket");
    }

    @FXML
    void onTotalClickedbyEmp(ActionEvent event) {

        System.out.println("Employee clicked Total");
    }

    @FXML
    void onOverviewClickedbyEmp(ActionEvent event) {

        System.out.println("Employee clicked Overview");
    }

    // Logout chung
    @FXML
    void onLogoutClicked(ActionEvent event) {
        loadUI("/views/fxml_Admin/Login.fxml");
>>>>>>> 44fab4a810779ded600d5ec37ab3c472deb80785
    }
}
