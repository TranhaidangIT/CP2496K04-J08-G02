package controller.controllerAdmin;

import dao.TicketDAO;
import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardOverviewController implements Initializable {

    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label ticketsSoldLabel;
    @FXML
    private Label totalRevenueLabel;
    @FXML
    private BarChart<String, Number> dailyRevenueChart;
    @FXML
    private CategoryAxis xAxisDailyRevenue;
    @FXML
    private NumberAxis yAxisDailyRevenue;

    private UserDAO userDAO;
    private TicketDAO ticketDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userDAO = new UserDAO();
        ticketDAO = new TicketDAO();

        try {
            loadTotalUsers();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        loadTotalTicketsAndRevenue();
        loadDailyRevenueChart();
    }

    private void loadTotalUsers() throws SQLException {
        int totalUsers = 0;
        totalUsers = userDAO.getTotalUsersCount();
        totalUsersLabel.setText(String.valueOf(totalUsers));
    }

    private void loadTotalTicketsAndRevenue() {
        try {
            int totalTickets = ticketDAO.getTotalTicketsCount();
            ticketsSoldLabel.setText(String.valueOf(totalTickets));

            double totalRevenueToday = ticketDAO.getTotalRevenueToday();
            totalRevenueLabel.setText(String.format("%,.2f USD", totalRevenueToday));
        } catch (SQLException e) {
            ticketsSoldLabel.setText("Error");
            totalRevenueLabel.setText("Error");
            e.printStackTrace();
        }
    }

    private void loadDailyRevenueChart() {
        try {
            List<Map<String, Object>> dailyRevenueData = ticketDAO.getDailyRevenueForLast7Days();

            dailyRevenueChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Revenue");

            for (Map<String, Object> data : dailyRevenueData) {
                String day = (String) data.get("day");
                double revenue = (double) data.get("revenue");
                series.getData().add(new XYChart.Data<>(day, revenue));
            }

            dailyRevenueChart.getData().add(series);
            xAxisDailyRevenue.setLabel("Day");
            yAxisDailyRevenue.setLabel("Revenue (USD)");
            dailyRevenueChart.setTitle("Revenue for the Last 7 Days");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
