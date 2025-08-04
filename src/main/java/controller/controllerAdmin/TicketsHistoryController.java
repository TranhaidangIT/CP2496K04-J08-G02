package controller.controllerAdmin;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;


public class TicketsHistoryController {

    // Các thành phần UI từ TicketHistoryContent.fxml
    @FXML private TextField historySearchField; // TextField cho tìm kiếm lịch sử vé
    @FXML private Button historySearchButton; // Nút Search trong lịch sử vé

    @FXML private TableView<?> ticketsHistoryTable; // TableView hiển thị lịch sử vé
    @FXML private TableColumn<?, ?> ticketIdCol; // Cột ID vé
    @FXML private TableColumn<?, ?> movieNameCol; // Cột tên phim
    @FXML private TableColumn<?, ?> usernameCol; // Cột username khách hàng
    @FXML private TableColumn<?, ?> seatCol; // Cột ghế
    @FXML private TableColumn<?, ?> showtimeCol; // Cột suất chiếu
    @FXML private TableColumn<?, ?> priceCol; // Cột giá

    @FXML
    public void initialize() {

        System.out.println("TicketHistoryContent.fxml initialized.");


    }



    @FXML
    private void handleSearchHistory() {
        String keyword = historySearchField.getText();
        System.out.println("Searching ticket history for: " + keyword);
        // Logic tìm kiếm lịch sử vé và cập nhật TableView ở đây
    }


}