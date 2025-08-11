package controller.controllerAdmin;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dao.TicketDAO;
import models.Ticket;

public class TicketsHistoryController {

    @FXML private TextField historySearchField;
    @FXML private Button historySearchButton;
    @FXML private TableView<Ticket> ticketsHistoryTable;
    @FXML private TableColumn<Ticket, Integer> ticketIdCol;
    @FXML private TableColumn<Ticket, String> movieNameCol;
    @FXML private TableColumn<Ticket, String> usernameCol;
    @FXML private TableColumn<Ticket, String> seatCol;
    @FXML private TableColumn<Ticket, String> showtimeCol;
    @FXML private TableColumn<Ticket, Double> priceCol;

    @FXML private BarChart<String, Number> ticketBarChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private Button analyzeMostBookedMoviesButton;
    @FXML private Button analyzeMostPopularShowtimesButton;

    private ObservableList<Ticket> ticketData = FXCollections.observableArrayList();
    private TicketDAO ticketDAO;

    @FXML
    public void initialize() {
        ticketDAO = new TicketDAO();

        ticketIdCol.setCellValueFactory(new PropertyValueFactory<>("ticketId"));
        movieNameCol.setCellValueFactory(new PropertyValueFactory<>("movieTitle"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("customerUsername"));
        seatCol.setCellValueFactory(new PropertyValueFactory<>("seatInfo"));
        showtimeCol.setCellValueFactory(new PropertyValueFactory<>("showtimeInfo"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        loadAllTickets();

        ticketBarChart.setTitle("Number of Tickets Sold");
        xAxis.setLabel("Movie Title");
        yAxis.setLabel("Number of Tickets");

        handleMonthlyStatistics();

        // Add listener to monitor TextField changes
        historySearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTicketsByName(newValue);
        });
    }

    // New method: Filter and display tickets by movie name
    private void filterTicketsByName(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            ticketsHistoryTable.setItems(ticketData);
            return;
        }

        String lowerCaseKeyword = keyword.toLowerCase();
        ObservableList<Ticket> filteredTickets = ticketData.stream()
                .filter(ticket -> ticket.getMovieTitle() != null && ticket.getMovieTitle().toLowerCase().startsWith(lowerCaseKeyword))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        ticketsHistoryTable.setItems(filteredTickets);
    }

    @FXML
    private void handleRefresh() {
        historySearchField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        loadAllTickets();
        handleMonthlyStatistics();
        showAlert("Notification", "Data refreshed successfully.", AlertType.INFORMATION);
    }

    private void loadAllTickets() {
        if (ticketDAO == null) return;
        try {
            ticketData.setAll(ticketDAO.getAllPaidTickets());
            ticketsHistoryTable.setItems(ticketData);
        } catch (SQLException e) {
            showAlert("Error", "Unable to load ticket data from the database.", AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearchHistory() {
        String keyword = historySearchField.getText();
        if (keyword == null || keyword.isEmpty()) {
            ticketsHistoryTable.setItems(ticketData);
            return;
        }

        ObservableList<Ticket> filteredTickets = FXCollections.observableArrayList();
        for (Ticket ticket : ticketData) {
            boolean matchesId = String.valueOf(ticket.getTicketId()).contains(keyword);
            boolean matchesUsername = (ticket.getCustomerUsername() != null && ticket.getCustomerUsername().contains(keyword));
            if (matchesId || matchesUsername) {
                filteredTickets.add(ticket);
            }
        }
        ticketsHistoryTable.setItems(filteredTickets);
    }

    @FXML
    private void handleQuarterlyStatistics() {
        LocalDate now = LocalDate.now();
        int quarter = (now.getMonthValue() - 1) / 3 + 1;
        int year = now.getYear();
        LocalDate startOfQuarter = LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
        LocalDate endOfQuarter = startOfQuarter.plusMonths(3).minusDays(1);
        updateChartWithStatistics(startOfQuarter, endOfQuarter);
    }

    @FXML
    private void handleMonthlyStatistics() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        updateChartWithStatistics(startOfMonth, endOfMonth);
    }

    @FXML
    private void handleYearlyStatistics() {
        LocalDate now = LocalDate.now();
        LocalDate startOfYear = now.withDayOfYear(1);
        LocalDate endOfYear = now.withMonth(12).withDayOfMonth(31);
        updateChartWithStatistics(startOfYear, endOfYear);
    }

    @FXML
    private void handleCustomRangeStatistics() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate != null && endDate != null && !startDate.isAfter(endDate)) {
            updateChartWithStatistics(startDate, endDate);
        } else {
            showAlert("Error", "Please select a valid date range.", AlertType.ERROR);
        }
    }

    private void updateChartWithStatistics(LocalDate startDate, LocalDate endDate) {
        if (ticketDAO == null) return;
        try {
            Date sqlStartDate = Date.valueOf(startDate);
            Date sqlEndDate = Date.valueOf(endDate);

            List<Map<String, Object>> statsData = ticketDAO.getTicketStatistics(sqlStartDate, sqlEndDate);
            updateBarChartWithData(statsData, "movieTitle", "ticketCount", "Number of Tickets Sold by Movie", "Movie Title", "Number of Tickets");

        } catch (SQLException e) {
            showAlert("Error", "Unable to retrieve statistics data from the database.", AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAnalyzeMostBookedMovies() {
        if (ticketDAO == null) return;
        try {
            List<Map<String, Object>> mostBookedMovies = ticketDAO.getMostBookedMovies();
            updateBarChartWithData(mostBookedMovies, "movieTitle", "ticketCount", "Most Booked Movies", "Movie Title", "Number of Tickets");
        } catch (SQLException e) {
            showAlert("Error", "Unable to retrieve movie analysis data.", AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAnalyzeMostPopularShowtimes() {
        if (ticketDAO == null) return;
        try {
            List<Map<String, Object>> mostPopularShowtimes = ticketDAO.getMostPopularShowtimes();
            updateBarChartWithData(mostPopularShowtimes, "showtime", "ticketCount", "Most Popular Showtimes", "Showtime", "Number of Tickets");
        } catch (SQLException e) {
            showAlert("Error", "Unable to retrieve showtime analysis data.", AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void updateBarChartWithData(List<Map<String, Object>> data, String categoryKey, String valueKey, String chartTitle, String xAxisLabel, String yAxisLabel) {
        ticketBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Number of Tickets");

        if (data != null) {
            for (Map<String, Object> entry : data) {
                String category = (String) entry.get(categoryKey);
                long value = (long) entry.get(valueKey);
                series.getData().add(new XYChart.Data<>(category, value));
            }
        }
        ticketBarChart.getData().add(series);
        ticketBarChart.setTitle(chartTitle);
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);
    }

    private void showAlert(String title, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
