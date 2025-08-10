package controller.controllerEmployees;

import configs.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import models.*;
import utils.Session;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.text.NumberFormat;
import java.util.Locale;
import java.math.BigDecimal;

public class TotalController {

    @FXML private Label invoiceNumberLabel;
    @FXML private Label employeeLabel;
    @FXML private Label dateTimeLabel;

    // Movie Information
    @FXML private Label movieTitleLabel;
    @FXML private Label showtimeLabel;
    @FXML private Label roomLabel;

    // Tickets Table
    @FXML private TableView<TicketDetail> ticketsTable;
    @FXML private TableColumn<TicketDetail, String> seatColumn;
    @FXML private TableColumn<TicketDetail, String> seatTypeColumn;
    @FXML private TableColumn<TicketDetail, String> ticketPriceColumn;

    // Services Table
    @FXML private TableView<ServiceDetail> servicesTable;
    @FXML private TableColumn<ServiceDetail, String> serviceNameColumn;
    @FXML private TableColumn<ServiceDetail, Integer> quantityColumn;
    @FXML private TableColumn<ServiceDetail, String> unitPriceColumn;
    @FXML private TableColumn<ServiceDetail, String> totalPriceColumn;

    // Locker Information
    @FXML private Label lockerInfoLabel;

    // Total Information
    @FXML private Label totalAmountLabel;

    // Payment (Cash only)
    @FXML private TextField receivedAmountField;
    @FXML private Label changeLabel;

    // Buttons
    @FXML private Button calculateChangeButton;
    @FXML private Button printInvoiceButton;
    @FXML private Button newTransactionButton;
    @FXML private Button backButton;
    @FXML private Button invoiceHistoryButton;

    private ObservableList<TicketDetail> ticketDetails = FXCollections.observableArrayList();
    private ObservableList<ServiceDetail> serviceDetails = FXCollections.observableArrayList();

    private String currentTicketCode = "";
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // Variables to store booking information from Session
    private Map<String, Object> bookingData;
    private List<Map<String, Object>> selectedSeats;
    private Showtime selectedShowtime;
    private User currentUser;
    private List<Service> selectedAddons;
    private Locker selectedLocker;

    private String lockerPinCode = null;
    private String lockerItemDescription = null;

    private AnchorPane contentArea;

    @FXML
    public void initialize() {
        setupTables();
        setupEventHandlers();
        loadBookingDataFromSession();
        loadCurrentTransaction();
        updateDateTime();
        setEmployeeInfo();
        generateInvoiceNumber();
    }

    public void setContentArea(AnchorPane contentArea) {
        this.contentArea = contentArea;
        System.out.println("setContentArea called in TotalController with contentArea: " + contentArea);
    }

    private void setupTables() {
        // Setup Tickets Table
        seatColumn.setCellValueFactory(new PropertyValueFactory<>("seatNumber"));
        seatTypeColumn.setCellValueFactory(new PropertyValueFactory<>("seatType"));
        ticketPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        ticketsTable.setItems(ticketDetails);

        // Setup Services Table
        serviceNameColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        servicesTable.setItems(serviceDetails);
    }

    private void setupEventHandlers() {
        calculateChangeButton.setOnAction(e -> calculateChange());
        printInvoiceButton.setOnAction(e -> printInvoice());
        newTransactionButton.setOnAction(e -> startNewTransaction());
        backButton.setOnAction(e -> goBack());
        invoiceHistoryButton.setOnAction(e -> openInvoiceHistory());

        receivedAmountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                try {
                    new BigDecimal(newVal);
                    calculateChange();
                } catch (NumberFormatException ex) {
                    // Invalid input, don't calculate
                }
            }
        });
    }

    private void loadBookingDataFromSession() {
        bookingData = Session.getBookingData();
        if (bookingData != null) {
            selectedShowtime = (Showtime) bookingData.get("showtime");
            selectedSeats = (List<Map<String, Object>>) bookingData.get("selectedSeats");
            currentUser = (User) bookingData.get("currentUser");
            selectedAddons = (List<Service>) bookingData.get("selectedAddons");
            selectedLocker = (Locker) bookingData.get("selectedLocker");

            if (selectedLocker != null) {
                lockerPinCode = (String) bookingData.get("lockerPinCode");
                lockerItemDescription = (String) bookingData.get("itemDescription");
                System.out.println("DEBUG: Loaded locker PIN: " + lockerPinCode);
                System.out.println("DEBUG: Loaded item description: " + lockerItemDescription);
            }
        }
    }

    private void generateInvoiceTxtFile() {
        try {
            String invoicesDir = "D:/invoices";
            if (!Files.exists(Paths.get(invoicesDir))) {
                Files.createDirectories(Paths.get(invoicesDir));
            }

            String fileName = invoicesDir + "/" + invoiceNumberLabel.getText() + ".txt";
            FileWriter writer = new FileWriter(fileName);

            StringBuilder invoice = new StringBuilder();
            invoice.append("=====================================\n");
            invoice.append("          PAYMENT INVOICE       \n");
            invoice.append("         Xuan Khanh CGV   \n");
            invoice.append("=====================================\n\n");

            invoice.append("Invoice number: ").append(invoiceNumberLabel.getText()).append("\n");
            invoice.append("Date & Time: ").append(dateTimeLabel.getText()).append("\n");
            invoice.append("Staff: ").append(employeeLabel.getText()).append("\n\n");

            invoice.append("-------------------------------------\n");
            invoice.append("        MOVIE INFORMATION            \n");
            invoice.append("-------------------------------------\n");
            invoice.append("Movie: ").append(movieTitleLabel.getText()).append("\n");
            invoice.append("Showtime: ").append(showtimeLabel.getText()).append("\n");
            invoice.append("Room: ").append(roomLabel.getText()).append("\n\n");

            invoice.append("-------------------------------------\n");
            invoice.append("        TICKET DETAILS              \n");
            invoice.append("-------------------------------------\n");
            for (TicketDetail ticket : ticketDetails) {
                invoice.append(String.format("Seat %s (%s): %s\n",
                        ticket.getSeatNumber(), ticket.getSeatType(), ticket.getPrice()));
            }

            if (!serviceDetails.isEmpty()) {
                invoice.append("\n-------------------------------------\n");
                invoice.append("        ADDITIONAL SERVICES         \n");
                invoice.append("-------------------------------------\n");
                for (ServiceDetail service : serviceDetails) {
                    invoice.append(String.format("%s x%d: %s\n",
                            service.getServiceName(), service.getQuantity(), service.getTotalPrice()));
                }
            }

            invoice.append("\n-------------------------------------\n");
            invoice.append("        LOCKER INFORMATION         \n");
            invoice.append("-------------------------------------\n");
            if (selectedLocker != null && lockerPinCode != null) {
                invoice.append("Locker: ").append(selectedLocker.getLockerNumber())
                        .append(" - ").append(selectedLocker.getLocationInfo()).append("\n");
                invoice.append("PIN Code: ").append(lockerPinCode).append("\n");
                if (lockerItemDescription != null && !lockerItemDescription.isEmpty()) {
                    invoice.append("Description of the item: ").append(lockerItemDescription).append("\n");
                }
            } else {
                invoice.append("Locker: Not used\n");
            }
            invoice.append("\n");

            invoice.append("=====================================\n");
            invoice.append(String.format("Total: %s\n", formatVND(totalAmount)));
            invoice.append("=====================================\n\n");

            invoice.append("-------------------------------------\n");
            invoice.append("        PAYMENT (CASH)      \n");
            invoice.append("-------------------------------------\n");
            BigDecimal receivedAmount;
            try {
                receivedAmount = new BigDecimal(receivedAmountField.getText().trim());
                invoice.append(String.format("Amount received: %s\n", formatVND(receivedAmount)));
            } catch (NumberFormatException e) {
                invoice.append("Amount received: Invalid\n");
            }

            BigDecimal changeAmount = BigDecimal.ZERO;
            String changeText = changeLabel.getText().replace("VND", "").replace(".", "").replace(",", "").trim();
            try {
                changeAmount = new BigDecimal(changeText);
            } catch (NumberFormatException e) {
                changeAmount = BigDecimal.ZERO;
            }
            invoice.append(String.format("Change: %s\n\n", formatVND(changeAmount)));

            invoice.append("=====================================\n");
            invoice.append("     THANK YOU FOR YOUR SUPPORT    \n");
            invoice.append("=====================================\n");

            writer.write(invoice.toString());
            writer.close();

            System.out.println("DEBUG: Invoice generated with PIN: " + lockerPinCode);

        } catch (IOException e) {
            System.err.println("Error generating invoice file: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create invoice file: " + e.getMessage());
        }
    }

    private void loadCurrentTransaction() {
        loadLatestTicket();
        loadSampleServices();
        loadLockerInfo();
        calculateTotal();
    }

    private void loadLatestTicket() {
        if (selectedShowtime != null && selectedSeats != null && !selectedSeats.isEmpty()) {
            movieTitleLabel.setText(selectedShowtime.getMovieTitle());
            String showTime = String.valueOf(selectedShowtime.getShowTime());
            String endTime = String.valueOf(selectedShowtime.getEndTime());
            showtimeLabel.setText(selectedShowtime.getShowDate() + " " +
                    (showTime != null ? showTime : "") + (endTime != null ? " - " + endTime : ""));
            roomLabel.setText("Room " + selectedShowtime.getRoomName());

            ticketDetails.clear();
            boolean validData = true;
            for (Map<String, Object> seatInfo : selectedSeats) {
                if (seatInfo != null &&
                        seatInfo.containsKey("seatRow") && seatInfo.get("seatRow") != null &&
                        seatInfo.containsKey("seatColumn") && seatInfo.get("seatColumn") != null &&
                        seatInfo.containsKey("seatTypeName") && seatInfo.get("seatTypeName") != null &&
                        seatInfo.containsKey("seatId") && seatInfo.get("seatId") != null) {
                    String seatNumber = seatInfo.get("seatRow") + "" + seatInfo.get("seatColumn");
                    String seatType = (String) seatInfo.get("seatTypeName");
                    Double price = (Double) seatInfo.get("price");
                    if (price == null) {
                        price = getPriceFromSeatId((Integer) seatInfo.get("seatId"));
                        if (price == null) price = 0.0;
                    }
                    ticketDetails.add(new TicketDetail(seatNumber, seatType, price));
                } else {
                    System.err.println("Invalid seat data in Session: " + seatInfo);
                    validData = false;
                    break;
                }
            }

            if (!validData) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Invalid seat data from Session. Loading data from database.");
                loadLatestTicketFromDatabase();
            }
        } else {
            System.out.println("No valid session data. Falling back to database.");
            loadLatestTicketFromDatabase();
        }
    }

    private Double getPriceFromSeatId(int seatId) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT st.price FROM seats s JOIN seatTypes st ON s.seatTypeId = st.seatTypeId WHERE s.seatId = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, seatId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("price");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching price from seatId: " + e.getMessage());
        }
        return null;
    }

    private void loadLatestTicketFromDatabase() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = """
                SELECT TOP 1 t.ticketCode, t.totalPrice, m.title, s.showDate, s.showTime, s.endTime,
                       sr.roomNumber, ts.seatId, seat.seatRow, seat.seatColumn, st.seatTypeName, st.price
                FROM tickets t
                JOIN showtimes s ON t.showtimeId = s.showtimeId
                JOIN movies m ON s.movieId = m.movieId
                JOIN screeningRooms sr ON s.roomId = sr.roomId
                JOIN ticketSeats ts ON t.ticketId = ts.ticketId
                JOIN seats seat ON ts.seatId = seat.seatId
                JOIN seatTypes st ON seat.seatTypeId = st.seatTypeId
                ORDER BY t.ticketId DESC
                """;

            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                currentTicketCode = rs.getString("ticketCode");
                movieTitleLabel.setText(rs.getString("title"));
                String showTime = rs.getString("showTime");
                String endTime = rs.getString("endTime");
                showtimeLabel.setText(rs.getString("showDate") + " " +
                        (showTime != null ? showTime : "") + (endTime != null ? " - " + endTime : ""));
                roomLabel.setText("Room " + rs.getString("roomNumber"));

                do {
                    String seatNumber = rs.getString("seatRow") + rs.getInt("seatColumn");
                    String seatType = rs.getString("seatTypeName");
                    double price = rs.getDouble("price");
                    ticketDetails.add(new TicketDetail(seatNumber, seatType, price));
                } while (rs.next());
            } else {
                movieTitleLabel.setText("No movie selected");
                showtimeLabel.setText("No showtime selected");
                roomLabel.setText("No room selected");
            }
        } catch (SQLException e) {
            System.err.println("Error loading ticket information: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load ticket information: " + e.getMessage());
        }
    }

    private void loadSampleServices() {
        serviceDetails.clear();
        if (selectedAddons != null && !selectedAddons.isEmpty()) {
            for (Service addon : selectedAddons) {
                BigDecimal price = addon.getPrice() != null ? addon.getPrice() : BigDecimal.ZERO;
                serviceDetails.add(new ServiceDetail(
                        addon.getServiceName(),
                        1,
                        price,
                        price
                ));
            }
        } else {
            System.out.println("No add-ons selected from Session");
        }
    }

    private void loadLockerInfo() {
        if (selectedLocker != null && lockerPinCode != null) {
            lockerInfoLabel.setText("Locker " + selectedLocker.getLockerNumber() +
                    " - " + selectedLocker.getLocationInfo());
        } else {
            lockerInfoLabel.setText("No locker used");
        }
    }

    private void calculateTotal() {
        totalAmount = BigDecimal.ZERO;
        for (TicketDetail ticket : ticketDetails) {
            totalAmount = totalAmount.add(BigDecimal.valueOf(ticket.getPriceRaw()));
        }
        for (ServiceDetail service : serviceDetails) {
            totalAmount = totalAmount.add(service.getTotalPriceRaw());
        }
        totalAmountLabel.setText(formatVND(totalAmount));
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        dateTimeLabel.setText(formatter.format(now));
    }

    private void setEmployeeInfo() {
        User currentUser = Session.getCurrentUser();
        if (currentUser != null) {
            employeeLabel.setText(currentUser.getFullName() + " (ID: " + currentUser.getEmployeeId() + ")");
        } else {
            employeeLabel.setText("Employee: Unknown");
            showAlert(Alert.AlertType.WARNING, "Warning", "Unable to identify employee information.");
        }
    }

    private void generateInvoiceNumber() {
        String invoiceNumber = "INV" + System.currentTimeMillis();
        invoiceNumberLabel.setText(invoiceNumber);
    }

    private String formatVND(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount).replace("₫", "VND").trim();
    }

    @FXML
    private void calculateChange() {
        try {
            String input = receivedAmountField.getText().trim();
            if (input.isEmpty()) {
                changeLabel.setText("Số tiền không hợp lệ");
                changeLabel.setStyle("-fx-text-fill: red;");
                printInvoiceButton.setDisable(true);
                return;
            }
            BigDecimal receivedAmount = new BigDecimal(input);
            BigDecimal change = receivedAmount.subtract(totalAmount);

            if (change.compareTo(BigDecimal.ZERO) >= 0) {
                changeLabel.setText(formatVND(change));
                changeLabel.setStyle("-fx-text-fill: green;");
                printInvoiceButton.setDisable(false);
            } else {
                changeLabel.setText("Số tiền không đủ");
                changeLabel.setStyle("-fx-text-fill: red;");
                printInvoiceButton.setDisable(true);
            }
        } catch (NumberFormatException e) {
            changeLabel.setText("Số tiền không hợp lệ");
            changeLabel.setStyle("-fx-text-fill: red;");
            printInvoiceButton.setDisable(true);
        }
    }

    @FXML
    private void printInvoice() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Payment");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to process the payment and create the ticket?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);

                String ticketCode = createFinalTicket();
                if (ticketCode == null) {
                    conn.rollback();
                    resetLockerStatus(selectedLocker != null ? selectedLocker.getLockerId() : 0);
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to create ticket. Please try again.");
                    return;
                }

                if (selectedLocker != null && lockerPinCode != null) {
                    try {
                        System.out.println("DEBUG: Assigning locker with PIN: " + lockerPinCode);
                        LockerController.assignLockerToCustomer(
                                conn,
                                selectedLocker.getLockerId(),
                                ticketCode,
                                lockerPinCode,
                                lockerItemDescription != null ? lockerItemDescription : "",
                                ""
                        );
                    } catch (SQLException e) {
                        conn.rollback();
                        resetLockerStatus(selectedLocker.getLockerId());
                        System.err.println("Error assigning locker: " + e.getMessage());
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to assign locker: " + e.getMessage());
                        return;
                    }
                }

                saveInvoiceWithTicketCode(ticketCode);
                conn.commit();
                generateInvoiceTxtFile();

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Payment Successful");
                success.setHeaderText(null);
                success.setContentText("Payment and ticket creation successful!\n" +
                        "Ticket Code: " + ticketCode + "\n" +
                        "Invoice Number: " + invoiceNumberLabel.getText() +
                        (selectedLocker != null && lockerPinCode != null ?
                                "\nLocker PIN: " + lockerPinCode : "") +
                        "\nInvoice file saved at: invoices/" + invoiceNumberLabel.getText() + ".txt");
                success.showAndWait();

                Session.clearBookingData();
                lockerPinCode = null;
                lockerItemDescription = null;
                selectedLocker = null;
                newTransactionButton.setDisable(false);

            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Database error: " + e.getMessage());
                resetLockerStatus(selectedLocker != null ? selectedLocker.getLockerId() : 0);
            }
        } else {
            resetLockerStatus(selectedLocker != null ? selectedLocker.getLockerId() : 0);
        }
    }

    private void resetLockerStatus(int lockerId) {
        if (lockerId == 0) return;
        try (Connection conn = DBConnection.getConnection()) {
            String updateQuery = "UPDATE lockers SET status = 'Available' WHERE lockerId = ? AND status = 'Occupied'";
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setInt(1, lockerId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error resetting locker status: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to reset locker status: " + e.getMessage());
        }
    }

    private String createFinalTicket() {
        if (selectedSeats == null || selectedSeats.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "No seat information available to create ticket.");
            return null;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            String checkShowtimeQuery = "SELECT COUNT(*) FROM showtimes WHERE showtimeId = ?";
            try (PreparedStatement checkShowtimeStmt = conn.prepareStatement(checkShowtimeQuery)) {
                checkShowtimeStmt.setInt(1, selectedShowtime.getShowtimeId());
                ResultSet checkRs = checkShowtimeStmt.executeQuery();
                if (checkRs.next() && checkRs.getInt(1) == 0) {
                    conn.rollback();
                    showAlert(Alert.AlertType.ERROR, "Showtime Error", "Showtime does not exist in the database.");
                    return null;
                }
            }

            String ticketCode = "TICKET_" + UUID.randomUUID().toString().substring(0, 8);
            String checkQuery = "SELECT COUNT(*) FROM tickets WHERE ticketCode = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, ticketCode);
                ResultSet checkTicketRs = checkStmt.executeQuery();
                checkTicketRs.next();
                if (checkTicketRs.getInt(1) > 0) {
                    ticketCode = "TICKET_" + UUID.randomUUID().toString().substring(0, 8);
                }
            }

            String ticketQuery = "INSERT INTO tickets (ticketCode, showtimeId, totalPrice, soldBy) OUTPUT INSERTED.ticketId VALUES (?, ?, ?, ?)";
            int ticketId = 0;
            try (PreparedStatement ticketStmt = conn.prepareStatement(ticketQuery)) {
                ticketStmt.setString(1, ticketCode);
                ticketStmt.setInt(2, selectedShowtime.getShowtimeId());
                ticketStmt.setBigDecimal(3, totalAmount);
                ticketStmt.setInt(4, currentUser.getUserId());
                ResultSet ticketRs = ticketStmt.executeQuery();
                if (ticketRs.next()) {
                    ticketId = ticketRs.getInt("ticketId");
                }
            }

            String seatQuery = "INSERT INTO ticketSeats (ticketId, seatId) VALUES (?, ?)";
            try (PreparedStatement seatStmt = conn.prepareStatement(seatQuery)) {
                for (Map<String, Object> seatInfo : selectedSeats) {
                    seatStmt.setInt(1, ticketId);
                    seatStmt.setInt(2, (Integer) seatInfo.get("seatId"));
                    seatStmt.addBatch();
                }
                seatStmt.executeBatch();
            }

            if (selectedAddons != null && !selectedAddons.isEmpty()) {
                String addonQuery = "INSERT INTO ticketServices (ticketId, serviceId, quantity, servicePrice) VALUES (?, ?, ?, ?)";
                try (PreparedStatement addonStmt = conn.prepareStatement(addonQuery)) {
                    for (Service addon : selectedAddons) {
                        addonStmt.setInt(1, ticketId);
                        addonStmt.setInt(2, addon.getServiceId());
                        addonStmt.setInt(3, 1);
                        addonStmt.setBigDecimal(4, addon.getPrice() != null ? addon.getPrice() : BigDecimal.ZERO);
                        addonStmt.addBatch();
                    }
                    addonStmt.executeBatch();
                }
            }

            conn.commit();
            System.out.println("DEBUG: Ticket created successfully! TicketId: " + ticketId + ", TicketCode: " + ticketCode);
            return ticketCode;

        } catch (SQLException ex) {
            System.err.println("Error creating ticket: " + ex.getMessage());
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to create ticket: " + ex.getMessage());
            return null;
        }
    }

    private void saveInvoiceWithTicketCode(String ticketCode) {
        createInvoicesTableIfNotExists();

        try (Connection conn = DBConnection.getConnection()) {
            String insertInvoice = """
            INSERT INTO invoices (invoiceNumber, ticketCode, employeeId, 
                                totalAmount, paymentMethod, receivedAmount, changeAmount, createdAt)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

            PreparedStatement stmt = conn.prepareStatement(insertInvoice);
            stmt.setString(1, invoiceNumberLabel.getText());
            stmt.setString(2, ticketCode);
            stmt.setString(3, currentUser != null ? currentUser.getEmployeeId() : "");
            stmt.setBigDecimal(4, totalAmount);
            stmt.setString(5, "Cash");
            BigDecimal receivedAmount;
            try {
                receivedAmount = new BigDecimal(receivedAmountField.getText().trim());
            } catch (NumberFormatException e) {
                receivedAmount = BigDecimal.ZERO;
            }
            stmt.setBigDecimal(6, receivedAmount);

            BigDecimal changeAmount = BigDecimal.ZERO;
            String changeText = changeLabel.getText().replace("VND", "").replace(".", "").replace(",", "").trim();
            try {
                changeAmount = new BigDecimal(changeText);
            } catch (NumberFormatException e) {
                changeAmount = BigDecimal.ZERO;
            }
            stmt.setBigDecimal(7, changeAmount);
            stmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error saving invoice: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save invoice: " + e.getMessage());
        }
    }

    private void createInvoicesTableIfNotExists() {
        try (Connection conn = DBConnection.getConnection()) {
            String createTable = """
                IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='invoices' AND xtype='U')
                CREATE TABLE invoices (
                    invoiceId INT IDENTITY(1,1) PRIMARY KEY,
                    invoiceNumber VARCHAR(50) UNIQUE NOT NULL,
                    ticketCode VARCHAR(50),
                    employeeId VARCHAR(20),
                    totalAmount DECIMAL(10,2),
                    paymentMethod VARCHAR(50) DEFAULT 'Cash',
                    receivedAmount DECIMAL(10,2),
                    changeAmount DECIMAL(10,2),
                    createdAt DATETIME DEFAULT GETDATE()
                )
                """;

            PreparedStatement stmt = conn.prepareStatement(createTable);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error creating invoices table: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create invoices table: " + e.getMessage());
        }
    }

    @FXML
    private void openInvoiceHistory() {
        try {
            // Tải FXML của InvoiceHistoryController
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/InvoiceHistory.fxml"));
            Parent invoiceHistoryRoot = loader.load();
            InvoiceHistoryController controller = loader.getController();

            // Đặt contentArea cho InvoiceHistoryController
            if (contentArea == null) {
                System.err.println("contentArea is null in TotalController!");
                showAlert(Alert.AlertType.ERROR, "Error", "contentArea has not been initialized!");
                return;
            }
            controller.setContentArea(contentArea);

            // Thay thế nội dung trong contentArea bằng giao diện InvoiceHistory
            contentArea.getChildren().setAll(invoiceHistoryRoot);
            AnchorPane.setTopAnchor(invoiceHistoryRoot, 0.0);
            AnchorPane.setBottomAnchor(invoiceHistoryRoot, 0.0);
            AnchorPane.setLeftAnchor(invoiceHistoryRoot, 0.0);
            AnchorPane.setRightAnchor(invoiceHistoryRoot, 0.0);

            System.out.println("Successfully switched to InvoiceHistory.fxml");
        } catch (IOException ex) {
            System.err.println("Error loading InvoiceHistory.fxml: " + ex.getMessage());
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load invoice history: " + ex.getMessage());
        }
    }

    private ObservableList<InvoiceRecord> loadInvoiceHistory() {
        ObservableList<InvoiceRecord> invoices = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            String query = """
                SELECT TOP 50 invoiceNumber, ticketCode, employeeId, totalAmount, createdAt 
                FROM invoices 
                ORDER BY createdAt DESC
                """;

            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            while (rs.next()) {
                String invoiceNumber = rs.getString("invoiceNumber");
                String ticketCode = rs.getString("ticketCode");
                String employeeId = rs.getString("employeeId");
                BigDecimal totalAmount = rs.getBigDecimal("totalAmount");
                Timestamp createdAt = rs.getTimestamp("createdAt");

                String formattedDate = createdAt != null ?
                        createdAt.toLocalDateTime().format(formatter) : "N/A";
                String formattedTotal = totalAmount != null ?
                        formatVND(totalAmount) : "0 VND";

                invoices.add(new InvoiceRecord(
                        invoiceNumber != null ? invoiceNumber : "N/A",
                        ticketCode != null ? ticketCode : "N/A",
                        employeeId != null ? employeeId : "N/A",
                        formattedTotal,
                        formattedDate
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error loading invoice history: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Failed to load invoice history: " + e.getMessage());
        }

        return invoices;
    }

    @FXML
    private void startNewTransaction() {
        if (selectedLocker != null) {
            try (Connection conn = DBConnection.getConnection()) {
                String updateQuery = "UPDATE lockers SET status = 'Available' WHERE lockerId = ?";
                PreparedStatement stmt = conn.prepareStatement(updateQuery);
                stmt.setInt(1, selectedLocker.getLockerId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error resetting locker status: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to reset locker status: " + e.getMessage());
            }
        }

        ticketDetails.clear();
        serviceDetails.clear();
        receivedAmountField.clear();
        changeLabel.setText("0 VND");
        movieTitleLabel.setText("No movie selected");
        showtimeLabel.setText("No showtime selected");
        roomLabel.setText("No room selected");
        lockerInfoLabel.setText("No locker used");

        selectedLocker = null;
        lockerPinCode = null;
        lockerItemDescription = null;

        generateInvoiceNumber();
        updateDateTime();
        goBack();
    }

    @FXML
    private void goBack() {
        if (selectedLocker != null) {
            try (Connection conn = DBConnection.getConnection()) {
                String updateQuery = "UPDATE lockers SET status = 'Available' WHERE lockerId = ?";
                PreparedStatement stmt = conn.prepareStatement(updateQuery);
                stmt.setInt(1, selectedLocker.getLockerId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error resetting locker status: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to reset locker status: " + e.getMessage());
            }
        }

        try {
            System.out.println("goBack called in TotalController. contentArea: " + contentArea);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/Locker.fxml"));
            Parent listMoviesRoot = loader.load();

            if (contentArea == null) {
                System.err.println("contentArea has not been initialized in TotalController!");
                showAlert(Alert.AlertType.ERROR, "Error", "contentArea has not been initialized in TotalController!");
                return;
            }

            contentArea.getChildren().setAll(listMoviesRoot);
            AnchorPane.setTopAnchor(listMoviesRoot, 0.0);
            AnchorPane.setBottomAnchor(listMoviesRoot, 0.0);
            AnchorPane.setLeftAnchor(listMoviesRoot, 0.0);
            AnchorPane.setRightAnchor(listMoviesRoot, 0.0);
        } catch (IOException ex) {
            System.err.println("Error loading ListMovies.fxml: " + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load movie list: " + ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Inner class for Invoice History records
    public static class InvoiceRecord {
        private String invoiceNumber;
        private String ticketCode;
        private String employeeId;
        private String totalAmount;
        private String createdAt;

        public InvoiceRecord(String invoiceNumber, String ticketCode, String employeeId,
                             String totalAmount, String createdAt) {
            this.invoiceNumber = invoiceNumber;
            this.ticketCode = ticketCode;
            this.employeeId = employeeId;
            this.totalAmount = totalAmount;
            this.createdAt = createdAt;
        }

        // Getters for PropertyValueFactory
        public String getInvoiceNumber() { return invoiceNumber; }
        public String getTicketCode() { return ticketCode; }
        public String getEmployeeId() { return employeeId; }
        public String getTotalAmount() { return totalAmount; }
        public String getCreatedAt() { return createdAt; }
    }

    // Modified TicketDetail to handle double (unchanged from original)
    public static class TicketDetail {
        private static final NumberFormat VND_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        private String seatNumber;
        private String seatType;
        private double price;

        public TicketDetail(String seatNumber, String seatType, double price) {
            this.seatNumber = seatNumber;
            this.seatType = seatType;
            this.price = price;
        }

        public String getSeatNumber() { return seatNumber; }
        public String getSeatType() { return seatType; }
        public String getPrice() {
            return VND_FORMATTER.format(price).replace("₫", "VND").trim();
        }
        public double getPriceRaw() {
            return price;
        }
    }

    // Modified ServiceDetail to use BigDecimal
    public static class ServiceDetail {
        private static final NumberFormat VND_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        private String serviceName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;

        public ServiceDetail(String serviceName, int quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
            this.serviceName = serviceName;
            this.quantity = quantity;
            this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
            this.totalPrice = totalPrice != null ? totalPrice : BigDecimal.ZERO;
        }

        public String getServiceName() { return serviceName; }
        public int getQuantity() { return quantity; }
        public String getUnitPrice() {
            return VND_FORMATTER.format(unitPrice).replace("₫", "VND").trim();
        }
        public String getTotalPrice() {
            return VND_FORMATTER.format(totalPrice).replace("₫", "VND").trim();
        }
        public BigDecimal getUnitPriceRaw() {
            return unitPrice;
        }
        public BigDecimal getTotalPriceRaw() {
            return totalPrice;
        }
    }
}