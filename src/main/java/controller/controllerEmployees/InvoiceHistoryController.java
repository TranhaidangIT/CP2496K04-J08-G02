package controller.controllerEmployees;

import configs.DBConnection;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import utils.Session;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class InvoiceHistoryController {

    @FXML private TextField searchField;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private Button searchButton;
    @FXML private Button clearButton;
    @FXML private Button refreshButton;

    @FXML private TableView<InvoiceRecord> invoicesTable;
    @FXML private TableColumn<InvoiceRecord, String> invoiceNumberColumn;
    @FXML private TableColumn<InvoiceRecord, String> ticketCodeColumn;
    @FXML private TableColumn<InvoiceRecord, String> createdAtColumn;
    @FXML private TableColumn<InvoiceRecord, String> employeeColumn;
    @FXML private TableColumn<InvoiceRecord, String> totalAmountColumn;
    @FXML private TableColumn<InvoiceRecord, String> paymentMethodColumn;
    @FXML private TableColumn<InvoiceRecord, String> statusColumn;
    @FXML private TableColumn<InvoiceRecord, Void> actionsColumn;

    @FXML private Label totalInvoicesLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label todayInvoicesLabel;

    @FXML private Button backButton;
    @FXML private Button viewInvoiceButton;
    @FXML private Button editInvoiceButton;
    @FXML private Button deleteInvoiceButton;
    @FXML private Button exportButton;

    private ObservableList<InvoiceRecord> invoicesList = FXCollections.observableArrayList();
    private AnchorPane contentArea;

    @FXML
    public void initialize() {
        System.out.println("InvoiceHistoryController initialized. contentArea: " + contentArea);
        setupTable();
        setupEventHandlers();
        loadInvoiceData();
        updateSummary();
    }

    public void setContentArea(AnchorPane contentArea) {
        this.contentArea = contentArea;
        System.out.println("setContentArea called in InvoiceHistoryController with contentArea: " + contentArea);
    }

    private void setupTable() {
        invoiceNumberColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        ticketCodeColumn.setCellValueFactory(new PropertyValueFactory<>("ticketCode"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAtFormatted"));
        employeeColumn.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmountFormatted"));
        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Setup actions column with buttons
        setupActionsColumn();

        invoicesTable.setItems(invoicesList);

        // Enable/disable buttons based on selection
        invoicesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            viewInvoiceButton.setDisable(!hasSelection);

            // Only enable edit/delete if ticket is still valid (show date is in the future)
            boolean canModify = hasSelection && newSelection != null && isTicketStillValid(newSelection.getTicketCode());
            editInvoiceButton.setDisable(!canModify);
            deleteInvoiceButton.setDisable(!canModify);
        });
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(new Callback<TableColumn<InvoiceRecord, Void>, TableCell<InvoiceRecord, Void>>() {
            @Override
            public TableCell<InvoiceRecord, Void> call(TableColumn<InvoiceRecord, Void> param) {
                return new TableCell<InvoiceRecord, Void>() {
                    private final Button viewBtn = new Button("👁");
                    private final Button editBtn = new Button("✏");
                    private final Button deleteBtn = new Button("🗑");
                    private final HBox buttons = new HBox(5, viewBtn, editBtn, deleteBtn);

                    {
                        // Style buttons
                        viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px;");
                        editBtn.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; -fx-font-size: 12px;");
                        deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 12px;");

                        viewBtn.setOnAction(e -> {
                            InvoiceRecord invoice = getTableView().getItems().get(getIndex());
                            viewInvoiceDetails(invoice);
                        });

                        editBtn.setOnAction(e -> {
                            InvoiceRecord invoice = getTableView().getItems().get(getIndex());
                            editInvoice(invoice);
                        });

                        deleteBtn.setOnAction(e -> {
                            InvoiceRecord invoice = getTableView().getItems().get(getIndex());
                            deleteInvoice(invoice);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            InvoiceRecord invoice = getTableView().getItems().get(getIndex());
                            boolean canModify = isTicketStillValid(invoice.getTicketCode());
                            editBtn.setDisable(!canModify);
                            deleteBtn.setDisable(!canModify);
                            setGraphic(buttons);
                        }
                    }
                };
            }
        });
    }

    private void setupEventHandlers() {
        searchButton.setOnAction(e -> searchInvoices());
        clearButton.setOnAction(e -> clearFilters());
        refreshButton.setOnAction(e -> refreshData());
        backButton.setOnAction(e -> goBack());
        viewInvoiceButton.setOnAction(e -> viewSelectedInvoice());
        editInvoiceButton.setOnAction(e -> editSelectedInvoice());
        deleteInvoiceButton.setOnAction(e -> deleteSelectedInvoice());
        exportButton.setOnAction(e -> exportToExcel());

        // Auto search when typing
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() >= 3 || newVal.isEmpty()) {
                searchInvoices();
            }
        });
    }

    private void loadInvoiceData() {
        invoicesList.clear();
        try (Connection conn = DBConnection.getConnection()) {
            String query = """
                SELECT i.invoiceNumber, i.ticketCode, i.employeeId,
                       i.totalAmount, i.paymentMethod, i.receivedAmount, i.changeAmount,
                       i.createdAt, u.fullName as employeeName
                FROM invoices i
                LEFT JOIN users u ON i.employeeId = u.employeeId
                ORDER BY i.createdAt DESC
                """;

            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                InvoiceRecord invoice = new InvoiceRecord(
                        rs.getString("invoiceNumber"),
                        rs.getString("ticketCode"),
                        rs.getString("employeeId"),
                        rs.getString("employeeName"),
                        rs.getDouble("totalAmount"),
                        rs.getString("paymentMethod"),
                        rs.getDouble("receivedAmount"),
                        rs.getDouble("changeAmount"),
                        rs.getTimestamp("createdAt").toLocalDateTime()
                );
                invoicesList.add(invoice);
            }
        } catch (SQLException e) {
            System.err.println("Error loading invoice data: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Error", "Unable to load invoice data: " + e.getMessage());
        }
    }

    private void searchInvoices() {
        String searchText = searchField.getText().trim();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        if (searchText.isEmpty() && fromDate == null && toDate == null) {
            loadInvoiceData();
            return;
        }

        invoicesList.clear();
        try (Connection conn = DBConnection.getConnection()) {
            StringBuilder queryBuilder = new StringBuilder("""
                SELECT i.invoiceNumber, i.ticketCode, i.employeeId,
                       i.totalAmount, i.paymentMethod, i.receivedAmount, i.changeAmount,
                       i.createdAt, u.fullName as employeeName
                FROM invoices i
                LEFT JOIN users u ON i.employeeId = u.employeeId
                WHERE 1=1
                """);

            if (!searchText.isEmpty()) {
                queryBuilder.append(" AND (i.invoiceNumber LIKE ? OR i.ticketCode LIKE ?)");
            }
            if (fromDate != null) {
                queryBuilder.append(" AND CAST(i.createdAt AS DATE) >= ?");
            }
            if (toDate != null) {
                queryBuilder.append(" AND CAST(i.createdAt AS DATE) <= ?");
            }
            queryBuilder.append(" ORDER BY i.createdAt DESC");

            PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString());
            int paramIndex = 1;

            if (!searchText.isEmpty()) {
                stmt.setString(paramIndex++, "%" + searchText + "%");
                stmt.setString(paramIndex++, "%" + searchText + "%");
            }
            if (fromDate != null) {
                stmt.setDate(paramIndex++, Date.valueOf(fromDate));
            }
            if (toDate != null) {
                stmt.setDate(paramIndex++, Date.valueOf(toDate));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                InvoiceRecord invoice = new InvoiceRecord(
                        rs.getString("invoiceNumber"),
                        rs.getString("ticketCode"),
                        rs.getString("employeeId"),
                        rs.getString("employeeName"),
                        rs.getDouble("totalAmount"),
                        rs.getString("paymentMethod"),
                        rs.getDouble("receivedAmount"),
                        rs.getDouble("changeAmount"),
                        rs.getTimestamp("createdAt").toLocalDateTime()
                );
                invoicesList.add(invoice);
            }
        } catch (SQLException e) {
            System.err.println("Error searching invoices: " + e.getMessage());
            showErrorAlert("Error", "Unable to search invoices: " + e.getMessage());
        }
        updateSummary();
    }

    private void clearFilters() {
        searchField.clear();
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        loadInvoiceData();
        updateSummary();
    }

    private void refreshData() {
        loadInvoiceData();
        updateSummary();
    }

    private void updateSummary() {
        int totalInvoices = invoicesList.size();
        double totalRevenue = invoicesList.stream().mapToDouble(InvoiceRecord::getTotalAmount).sum();

        LocalDate today = LocalDate.now();
        long todayInvoices = invoicesList.stream()
                .filter(invoice -> invoice.getCreatedAt().toLocalDate().equals(today))
                .count();

        totalInvoicesLabel.setText(String.valueOf(totalInvoices));
        totalRevenueLabel.setText(String.format("%.0f VND", totalRevenue));
        todayInvoicesLabel.setText(String.valueOf(todayInvoices));
    }

    private boolean isTicketStillValid(String ticketCode) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = """
                SELECT s.showDate, s.showTime
                FROM tickets t
                JOIN showtimes s ON t.showtimeId = s.showtimeId
                WHERE t.ticketCode = ?
                """;

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, ticketCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Date showDate = rs.getDate("showDate");
                Time showTime = rs.getTime("showTime");
                LocalDateTime showDateTime = LocalDateTime.of(showDate.toLocalDate(), showTime.toLocalTime());
                return showDateTime.isAfter(LocalDateTime.now());
            }
        } catch (SQLException e) {
            System.err.println("Error checking ticket validity: " + e.getMessage());
        }
        return false;
    }

    private void viewInvoiceDetails(InvoiceRecord invoice) {
        // Generate and show invoice text file content
        generateAndShowInvoiceTxtFile(invoice);
    }

    private void viewSelectedInvoice() {
        InvoiceRecord selected = invoicesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            viewInvoiceDetails(selected);
        }
    }

    private void editInvoice(InvoiceRecord invoice) {
        // Implementation for editing invoice
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Information");
        info.setHeaderText(null);
        info.setContentText("Edit invoice function is under development.\nInvoice number: " + invoice.getInvoiceNumber());
        info.showAndWait();
    }

    private void editSelectedInvoice() {
        InvoiceRecord selected = invoicesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            editInvoice(selected);
        }
    }

    private void deleteInvoice(InvoiceRecord invoice) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete invoice: " + invoice.getInvoiceNumber() + "?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DBConnection.getConnection()) {
                String deleteQuery = "DELETE FROM invoices WHERE invoiceNumber = ?";
                PreparedStatement stmt = conn.prepareStatement(deleteQuery);
                stmt.setString(1, invoice.getInvoiceNumber());

                int rowsDeleted = stmt.executeUpdate();
                if (rowsDeleted > 0) {
                    invoicesList.remove(invoice);
                    updateSummary();
                    showSuccessAlert("Success", "Invoice deleted successfully!");
                } else {
                    showErrorAlert("Error", "Unable to delete invoice!");
                }
            } catch (SQLException e) {
                System.err.println("Error deleting invoice: " + e.getMessage());
                showErrorAlert("Error", "Error deleting invoice: " + e.getMessage());
            }
        }
    }

    private void deleteSelectedInvoice() {
        InvoiceRecord selected = invoicesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            deleteInvoice(selected);
        }
    }

    private void exportToExcel() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Information");
        info.setHeaderText(null);
        info.setContentText("Export to Excel function is under development.");
        info.showAndWait();
    }

    private void generateAndShowInvoiceTxtFile(InvoiceRecord invoice) {
        try {
            // Create invoices directory if it doesn't exist
            String invoicesDir = "invoices";
            if (!Files.exists(Paths.get(invoicesDir))) {
                Files.createDirectories(Paths.get(invoicesDir));
            }

            String fileName = invoicesDir + "/" + invoice.getInvoiceNumber() + ".txt";

            // Get detailed invoice information from db
            String invoiceContent = generateInvoiceContent(invoice);

            // Write to file
            FileWriter writer = new FileWriter(fileName);
            writer.write(invoiceContent);
            writer.close();

            // Show content in a dialog
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Invoice Details");
            alert.setHeaderText("Invoice: " + invoice.getInvoiceNumber());

            TextArea textArea = new TextArea(invoiceContent);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefRowCount(20);
            textArea.setPrefColumnCount(60);

            alert.getDialogPane().setContent(textArea);
            alert.setResizable(true);
            alert.showAndWait();

            showSuccessAlert("Success", "Invoice file generated at: " + fileName);

        } catch (IOException e) {
            System.err.println("Error generating invoice file: " + e.getMessage());
            showErrorAlert("Error", "Unable to generate invoice file: " + e.getMessage());
        }
    }

    private String generateInvoiceContent(InvoiceRecord invoice) {
        StringBuilder content = new StringBuilder();

        try (Connection conn = DBConnection.getConnection()) {
            // Get movie and showtime information
            String movieQuery = """
                SELECT m.title, s.showDate, s.showTime, s.endTime, sr.roomNumber
                FROM tickets t
                JOIN showtimes s ON t.showtimeId = s.showtimeId
                JOIN movies m ON s.movieId = m.movieId
                JOIN screeningRooms sr ON s.roomId = sr.roomId
                WHERE t.ticketCode = ?
                """;

            PreparedStatement movieStmt = conn.prepareStatement(movieQuery);
            movieStmt.setString(1, invoice.getTicketCode());
            ResultSet movieRs = movieStmt.executeQuery();

            String movieTitle = "N/A";
            String showDateTime = "N/A";
            String roomNumber = "N/A";

            if (movieRs.next()) {
                movieTitle = movieRs.getString("title");
                showDateTime = movieRs.getString("showDate") + " " +
                        (movieRs.getString("showTime") != null ? movieRs.getString("showTime") : "") +
                        (movieRs.getString("endTime") != null ? " - " + movieRs.getString("endTime") : "");
                roomNumber = "Room " + movieRs.getString("roomNumber");
            }

            // Get locker information
            String lockerQuery = """
                SELECT l.lockerNumber, la.pinCode, la.itemDescription
                FROM tickets t
                JOIN lockerAssignments la ON t.ticketCode = ? 
                JOIN lockers l ON la.lockerId = l.lockerId
                WHERE la.releasedAt IS NULL
                """;

            PreparedStatement lockerStmt = conn.prepareStatement(lockerQuery);
            lockerStmt.setString(1, invoice.getTicketCode());
            ResultSet lockerRs = lockerStmt.executeQuery();

            String lockerInfo = "No locker used";
            if (lockerRs.next()) {
                String lockerNumber = lockerRs.getString("lockerNumber");
                String pinCode = lockerRs.getString("pinCode");
                String itemDescription = lockerRs.getString("itemDescription");

                lockerInfo = String.format("Locker %s - PIN: %s", lockerNumber, pinCode);
                if (itemDescription != null && !itemDescription.isEmpty()) {
                    lockerInfo += String.format("\nDescription: %s", itemDescription);
                }
            }

            // Build invoice content
            content.append("=====================================\n");
            content.append("           PAYMENT INVOICE           \n");
            content.append("         CGV Xuan Khanh Cinema      \n");
            content.append("=====================================\n\n");

            content.append("Invoice Number: ").append(invoice.getInvoiceNumber()).append("\n");
            content.append("Date & Time: ").append(invoice.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
            content.append("Employee: ").append(invoice.getEmployeeName() != null ? invoice.getEmployeeName() : invoice.getEmployeeId()).append("\n\n");

            content.append("-------------------------------------\n");
            content.append("          MOVIE INFORMATION         \n");
            content.append("-------------------------------------\n");
            content.append("Movie: ").append(movieTitle).append("\n");
            content.append("Showtime: ").append(showDateTime).append("\n");
            content.append("Room: ").append(roomNumber).append("\n\n");

            // Get seat information
            String seatQuery = """
                SELECT seat.seatRow, seat.seatColumn, st.seatTypeName, st.price
                FROM tickets t
                JOIN ticketSeats ts ON t.ticketId = ts.ticketId
                JOIN seats seat ON ts.seatId = seat.seatId
                JOIN seatTypes st ON seat.seatTypeId = st.seatTypeId
                WHERE t.ticketCode = ?
                """;

            PreparedStatement seatStmt = conn.prepareStatement(seatQuery);
            seatStmt.setString(1, invoice.getTicketCode());
            ResultSet seatRs = seatStmt.executeQuery();

            content.append("-------------------------------------\n");
            content.append("           TICKET DETAILS           \n");
            content.append("-------------------------------------\n");

            while (seatRs.next()) {
                String seatNumber = seatRs.getString("seatRow") + seatRs.getInt("seatColumn");
                String seatType = seatRs.getString("seatTypeName");
                double price = seatRs.getDouble("price");
                content.append(String.format("seats %s (%s): %.0f VND\n", seatNumber, seatType, price));
            }

            // Get service information
            String serviceQuery = """
                SELECT s.serviceName, ts.quantity, ts.servicePrice
                FROM tickets t
                JOIN ticketServices ts ON t.ticketId = ts.ticketId
                JOIN services s ON ts.serviceId = s.serviceId
                WHERE t.ticketCode = ?
                """;

            PreparedStatement serviceStmt = conn.prepareStatement(serviceQuery);
            serviceStmt.setString(1, invoice.getTicketCode());
            ResultSet serviceRs = serviceStmt.executeQuery();

            boolean hasServices = false;
            StringBuilder servicesContent = new StringBuilder();
            while (serviceRs.next()) {
                if (!hasServices) {
                    servicesContent.append("\n-------------------------------------\n");
                    servicesContent.append("         ADDITIONAL SERVICES        \n");
                    servicesContent.append("-------------------------------------\n");
                    hasServices = true;
                }

                String serviceName = serviceRs.getString("serviceName");
                int quantity = serviceRs.getInt("quantity");
                double servicePrice = serviceRs.getDouble("servicePrice");
                double totalPrice = servicePrice * quantity;
                servicesContent.append(String.format("%s x%d: %.0f VND\n", serviceName, quantity, totalPrice));
            }

            if (hasServices) {
                content.append(servicesContent.toString());
            }

            // Add locker information to invoice
            content.append("\n-------------------------------------\n");
            content.append("          LOCKER INFORMATION        \n");
            content.append("-------------------------------------\n");
            content.append(lockerInfo).append("\n");

            content.append("\n=====================================\n");
            content.append(String.format("TOTAL: %.0f VND\n", invoice.getTotalAmount()));
            content.append("=====================================\n\n");

            content.append("-------------------------------------\n");
            content.append("         PAYMENT (%s)               \n".formatted(invoice.getPaymentMethod().toUpperCase()));
            content.append("-------------------------------------\n");
            content.append(String.format("Amount Received: %.0f VND\n", invoice.getReceivedAmount()));
            content.append(String.format("Change: %.0f VND\n\n", invoice.getChangeAmount()));

            content.append("=====================================\n");
            content.append("      THANK YOU FOR YOUR VISIT      \n");
            content.append("=====================================\n");

        } catch (SQLException e) {
            System.err.println("Error generating invoice content: " + e.getMessage());
            content.append("Error generating invoice content: ").append(e.getMessage());
        }

        return content.toString();
    }

    @FXML
    private void goBack() {
        System.out.println("goBack called in InvoiceHistoryController. contentArea: " + contentArea);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/Total.fxml"));
            Parent totalRoot = loader.load();
            TotalController controller = loader.getController();

            if (contentArea == null) {
                System.err.println("contentArea is null in InvoiceHistoryController!");
                showErrorAlert("Error", "contentArea is not initialized!");
                return;
            }

            controller.setContentArea(contentArea); // Pass contentArea to TotalController
            contentArea.getChildren().setAll(totalRoot);
            AnchorPane.setTopAnchor(totalRoot, 0.0);
            AnchorPane.setBottomAnchor(totalRoot, 0.0);
            AnchorPane.setLeftAnchor(totalRoot, 0.0);
            AnchorPane.setRightAnchor(totalRoot, 0.0);
            System.out.println("Successfully switched to Total.fxml");
        } catch (IOException ex) {
            System.err.println("Error loading Total.fxml: " + ex.getMessage());
            ex.printStackTrace();
            showErrorAlert("Error", "Unable to load Total interface: " + ex.getMessage());
        }
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class for Invoice Record
    public static class InvoiceRecord {
        private final StringProperty invoiceNumber;
        private final StringProperty ticketCode;
        private final StringProperty employeeId;
        private final StringProperty employeeName;
        private final DoubleProperty totalAmount;
        private final StringProperty paymentMethod;
        private final DoubleProperty receivedAmount;
        private final DoubleProperty changeAmount;
        private final LocalDateTime createdAt;
        private final StringProperty status;

        public InvoiceRecord(String invoiceNumber, String ticketCode, String employeeId, String employeeName,
                             double totalAmount, String paymentMethod,
                             double receivedAmount, double changeAmount, LocalDateTime createdAt) {
            this.invoiceNumber = new SimpleStringProperty(invoiceNumber);
            this.ticketCode = new SimpleStringProperty(ticketCode);
            this.employeeId = new SimpleStringProperty(employeeId);
            this.employeeName = new SimpleStringProperty(employeeName);
            this.totalAmount = new SimpleDoubleProperty(totalAmount);
            this.paymentMethod = new SimpleStringProperty(paymentMethod);
            this.receivedAmount = new SimpleDoubleProperty(receivedAmount);
            this.changeAmount = new SimpleDoubleProperty(changeAmount);
            this.createdAt = createdAt;
            this.status = new SimpleStringProperty("Completed");
        }

        // Getters
        public String getInvoiceNumber() { return invoiceNumber.get(); }
        public String getTicketCode() { return ticketCode.get(); }
        public String getEmployeeId() { return employeeId.get(); }
        public String getEmployeeName() { return employeeName.get(); }
        public double getTotalAmount() { return totalAmount.get(); }
        public String getTotalAmountFormatted() { return String.format("%.0f VND", totalAmount.get()); }
        public String getPaymentMethod() { return paymentMethod.get(); }
        public double getReceivedAmount() { return receivedAmount.get(); }
        public double getChangeAmount() { return changeAmount.get(); }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public String getCreatedAtFormatted() {
            return createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        public String getStatus() { return status.get(); }

        // Property getters for TableView
        public StringProperty invoiceNumberProperty() { return invoiceNumber; }
        public StringProperty ticketCodeProperty() { return ticketCode; }
        public StringProperty employeeIdProperty() { return employeeId; }
        public StringProperty employeeNameProperty() { return employeeName; }
        public StringProperty paymentMethodProperty() { return paymentMethod; }
        public StringProperty statusProperty() { return status; }
    }
}