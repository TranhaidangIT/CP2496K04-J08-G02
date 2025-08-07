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
    @FXML private TableColumn<TicketDetail, Double> ticketPriceColumn;

    // Services Table
    @FXML private TableView<ServiceDetail> servicesTable;
    @FXML private TableColumn<ServiceDetail, String> serviceNameColumn;
    @FXML private TableColumn<ServiceDetail, Integer> quantityColumn;
    @FXML private TableColumn<ServiceDetail, Double> unitPriceColumn;
    @FXML private TableColumn<ServiceDetail, Double> totalPriceColumn;

    // Locker Information
    @FXML private Label lockerInfoLabel;

    // Total Information (Simplified)
    @FXML private Label totalAmountLabel;

    // Payment (Simplified - Cash only)
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
    private double totalAmount = 0.0;

    // Variables to store booking information from Session
    private Map<String, Object> bookingData;
    private List<Map<String, Object>> selectedSeats;
    private Showtime selectedShowtime;
    private User currentUser;
    private List<Service> selectedAddons;
    private Locker selectedLocker;

    private String lockerPinCode = null;
    private String lockerItemDescription = null;

    // Variable for contentArea
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

    // Method to set contentArea
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
                    Double.parseDouble(newVal);
                    calculateChange();
                } catch (NumberFormatException ex) {
                    // Invalid input, don't calculate
                }
            }
        });
    }

    // Modified openInvoiceHistory to pass contentArea
    @FXML
    private void openInvoiceHistory() {
        try {
            System.out.println("openInvoiceHistory called. contentArea: " + contentArea);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/InvoiceHistory.fxml"));
            Parent invoiceHistoryRoot = loader.load();
            InvoiceHistoryController controller = loader.getController();

            // Pass contentArea to InvoiceHistoryController
            controller.setContentArea(this.contentArea);

            if (contentArea == null) {
                System.err.println("contentArea has not been initialized in TotalController!");
                showAlert(Alert.AlertType.ERROR, "Error", "contentArea has not been initialized in TotalController!");
                return;
            }

            contentArea.getChildren().setAll(invoiceHistoryRoot);
            AnchorPane.setTopAnchor(invoiceHistoryRoot, 0.0);
            AnchorPane.setBottomAnchor(invoiceHistoryRoot, 0.0);
            AnchorPane.setLeftAnchor(invoiceHistoryRoot, 0.0);
            AnchorPane.setRightAnchor(invoiceHistoryRoot, 0.0);
        } catch (IOException ex) {
            System.err.println("Error loading InvoiceHistory.fxml: " + ex.getMessage());
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load invoice history: " + ex.getMessage());
        }
    }

    private void loadBookingDataFromSession() {
        bookingData = Session.getBookingData();
        if (bookingData != null) {
            selectedShowtime = (Showtime) bookingData.get("showtime");
            selectedSeats = (List<Map<String, Object>>) bookingData.get("selectedSeats");
            currentUser = (User) bookingData.get("currentUser");
            selectedAddons = (List<Service>) bookingData.get("selectedAddons");
            selectedLocker = (Locker) bookingData.get("selectedLocker");

            // QUAN TRỌNG: Lưu PIN và mô tả vào biến instance ngay khi load
            if (selectedLocker != null) {
                lockerPinCode = (String) bookingData.get("lockerPinCode");
                lockerItemDescription = (String) bookingData.get("itemDescription");
                System.out.println("DEBUG: Loaded locker PIN: " + lockerPinCode);
                System.out.println("DEBUG: Loaded item description: " + lockerItemDescription);
            }

            System.out.println("Loaded booking data from Session:");
            System.out.println("- Showtime: " + (selectedShowtime != null ? selectedShowtime.getMovieTitle() : "null"));
            System.out.println("- Seats count: " + (selectedSeats != null ? selectedSeats.size() : 0));
            System.out.println("- Addons count: " + (selectedAddons != null ? selectedAddons.size() : 0));
            System.out.println("- Locker: " + (selectedLocker != null ? selectedLocker.getLockerNumber() : "null"));
            if (selectedLocker != null) {
                System.out.println("- Locker PinCode: " + lockerPinCode);
                System.out.println("- Item Description: " + lockerItemDescription);
            }
        }
    }

    private void generateInvoiceTxtFile() {
        try {
            String invoicesDir = "invoices";
            if (!Files.exists(Paths.get(invoicesDir))) {
                Files.createDirectories(Paths.get(invoicesDir));
            }

            String fileName = invoicesDir + "/" + invoiceNumberLabel.getText() + ".txt";
            FileWriter writer = new FileWriter(fileName);

            StringBuilder invoice = new StringBuilder();
            invoice.append("=====================================\n");
            invoice.append("          PAYMENT INVOICE           \n");
            invoice.append("         CGV Xuan Khanh Cinema      \n");
            invoice.append("=====================================\n\n");

            invoice.append("Invoice Number: ").append(invoiceNumberLabel.getText()).append("\n");
            invoice.append("Date & Time: ").append(dateTimeLabel.getText()).append("\n");
            invoice.append("Employee: ").append(employeeLabel.getText()).append("\n\n");

            invoice.append("-------------------------------------\n");
            invoice.append("           MOVIE INFORMATION        \n");
            invoice.append("-------------------------------------\n");
            invoice.append("Movie: ").append(movieTitleLabel.getText()).append("\n");
            invoice.append("Showtime: ").append(showtimeLabel.getText()).append("\n");
            invoice.append("Room: ").append(roomLabel.getText()).append("\n\n");

            invoice.append("-------------------------------------\n");
            invoice.append("            TICKET DETAILS          \n");
            invoice.append("-------------------------------------\n");
            for (TicketDetail ticket : ticketDetails) {
                invoice.append(String.format("Seat %s (%s): %.0f VND\n",
                        ticket.getSeatNumber(), ticket.getSeatType(), ticket.getPrice()));
            }

            if (!serviceDetails.isEmpty()) {
                invoice.append("\n-------------------------------------\n");
                invoice.append("           ADDITIONAL SERVICES      \n");
                invoice.append("-------------------------------------\n");
                for (ServiceDetail service : serviceDetails) {
                    invoice.append(String.format("%s x%d: %.0f VND\n",
                            service.getServiceName(), service.getQuantity(), service.getTotalPrice()));
                }
            }

            invoice.append("\n-------------------------------------\n");
            invoice.append("          LOCKER INFORMATION        \n");
            invoice.append("-------------------------------------\n");
            if (selectedLocker != null && lockerPinCode != null) {
                invoice.append("Locker: ").append(selectedLocker.getLockerNumber())
                        .append(" - ").append(selectedLocker.getLocationInfo()).append("\n");
                invoice.append("PIN: ").append(lockerPinCode).append("\n");
                if (lockerItemDescription != null && !lockerItemDescription.isEmpty()) {
                    invoice.append("Item Description: ").append(lockerItemDescription).append("\n");
                }
            } else {
                invoice.append("Locker: No locker used\n");
            }
            invoice.append("\n");

            invoice.append("=====================================\n");
            invoice.append(String.format("TOTAL: %.0f VND\n", totalAmount));
            invoice.append("=====================================\n\n");

            invoice.append("-------------------------------------\n");
            invoice.append("         PAYMENT (CASH)            \n");
            invoice.append("-------------------------------------\n");
            invoice.append(String.format("Received Amount: %.0f VND\n",
                    Double.parseDouble(receivedAmountField.getText())));

            String changeText = changeLabel.getText().replace("VND", "").replace(".", "").replace(",", "").trim();
            double changeAmount = 0.0;
            try {
                changeAmount = Double.parseDouble(changeText);
            } catch (NumberFormatException e) {
                changeAmount = 0.0;
            }
            invoice.append(String.format("Change: %.0f VND\n\n", changeAmount));

            invoice.append("=====================================\n");
            invoice.append("     THANK YOU FOR YOUR PATRONAGE    \n");
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
                serviceDetails.add(new ServiceDetail(
                        addon.getServiceName(),
                        1,
                        addon.getPrice(),
                        addon.getPrice()
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
        totalAmount = 0.0;
        for (TicketDetail ticket : ticketDetails) {
            totalAmount += ticket.getPrice();
        }
        for (ServiceDetail service : serviceDetails) {
            totalAmount += service.getTotalPrice();
        }
        totalAmountLabel.setText(String.format("%.0f VND", totalAmount));
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

    @FXML
    private void calculateChange() {
        try {
            double receivedAmount = Double.parseDouble(receivedAmountField.getText().trim());
            double change = receivedAmount - totalAmount;

            if (change >= 0) {
                changeLabel.setText(String.format("%.0f VND", change));
                changeLabel.setStyle("-fx-text-fill: green;");
                printInvoiceButton.setDisable(false);
            } else {
                changeLabel.setText("Insufficient amount");
                changeLabel.setStyle("-fx-text-fill: red;");
                printInvoiceButton.setDisable(true);
            }
        } catch (NumberFormatException e) {
            changeLabel.setText("Invalid amount");
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

                // Create ticket
                String ticketCode = createFinalTicket();
                if (ticketCode == null) {
                    conn.rollback();
                    resetLockerStatus(selectedLocker != null ? selectedLocker.getLockerId() : 0);
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to create ticket. Please try again.");
                    return;
                }

                // Assign locker if applicable - SỬ DỤNG BIẾN INSTANCE
                if (selectedLocker != null && lockerPinCode != null) {
                    try {
                        System.out.println("DEBUG: Assigning locker with PIN: " + lockerPinCode);
                        LockerController.assignLockerToCustomer(
                                conn,
                                selectedLocker.getLockerId(),
                                ticketCode,
                                lockerPinCode, // SỬ DỤNG BIẾN INSTANCE THAY VÌ LẤY TỪ bookingData
                                lockerItemDescription != null ? lockerItemDescription : "",
                                ""
                        );
                        System.out.println("Assigned locker: lockerId=" + selectedLocker.getLockerId() +
                                ", ticketCode=" + ticketCode + ", pinCode=" + lockerPinCode);
                    } catch (SQLException e) {
                        conn.rollback();
                        resetLockerStatus(selectedLocker.getLockerId());
                        System.err.println("Error assigning locker: " + e.getMessage());
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to assign locker: " + e.getMessage());
                        return;
                    }
                }

                // Save invoice
                saveInvoiceWithTicketCode(ticketCode);

                // Commit transaction
                conn.commit();

                // Generate invoice file
                generateInvoiceTxtFile();

                // Show success message với PIN chính xác
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

                // Clear session data và reset biến instance
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
            // Cancel transaction, reset locker status
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

            // Check showtime
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

            // Generate unique ticketCode
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

            // Insert ticket
            String ticketQuery = "INSERT INTO tickets (ticketCode, showtimeId, totalPrice, soldBy) OUTPUT INSERTED.ticketId VALUES (?, ?, ?, ?)";
            int ticketId = 0;
            try (PreparedStatement ticketStmt = conn.prepareStatement(ticketQuery)) {
                ticketStmt.setString(1, ticketCode);
                ticketStmt.setInt(2, selectedShowtime.getShowtimeId());
                ticketStmt.setDouble(3, totalAmount);
                ticketStmt.setInt(4, currentUser.getUserId());
                ResultSet ticketRs = ticketStmt.executeQuery();
                if (ticketRs.next()) {
                    ticketId = ticketRs.getInt("ticketId");
                }
            }

            // Insert seats
            String seatQuery = "INSERT INTO ticketSeats (ticketId, seatId) VALUES (?, ?)";
            try (PreparedStatement seatStmt = conn.prepareStatement(seatQuery)) {
                for (Map<String, Object> seatInfo : selectedSeats) {
                    seatStmt.setInt(1, ticketId);
                    seatStmt.setInt(2, (Integer) seatInfo.get("seatId"));
                    seatStmt.addBatch();
                }
                seatStmt.executeBatch();
            }

            // Insert additional services
            if (selectedAddons != null && !selectedAddons.isEmpty()) {
                String addonQuery = "INSERT INTO ticketServices (ticketId, serviceId, quantity, servicePrice) VALUES (?, ?, ?, ?)";
                try (PreparedStatement addonStmt = conn.prepareStatement(addonQuery)) {
                    for (Service addon : selectedAddons) {
                        addonStmt.setInt(1, ticketId);
                        addonStmt.setInt(2, Integer.parseInt(addon.getId()));
                        addonStmt.setInt(3, 1);
                        addonStmt.setDouble(4, addon.getPrice());
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
            stmt.setDouble(4, totalAmount);
            stmt.setString(5, "Cash");
            stmt.setDouble(6, Double.parseDouble(receivedAmountField.getText()));

            String changeText = changeLabel.getText().replace("VND", "").replace(".", "").replace(",", "").trim();
            double changeAmount = 0.0;
            try {
                changeAmount = Double.parseDouble(changeText);
            } catch (NumberFormatException e) {
                changeAmount = 0.0;
            }
            stmt.setDouble(7, changeAmount);
            stmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error saving invoice: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save invoice: " + e.getMessage());
        }
    }

    private void updateLockerInfo() {
        Map<String, Object> bookingData = Session.getBookingData();
        if (bookingData != null) {
            Locker selectedLocker = (Locker) bookingData.get("selectedLocker");
            String pinCode = (String) bookingData.get("lockerPinCode");
            String itemDescription = (String) bookingData.get("itemDescription");

            if (selectedLocker != null && pinCode != null) {
                String lockerInfo = String.format("Locker %s - PIN: %s",
                        selectedLocker.getLockerNumber(), pinCode);
                if (itemDescription != null && !itemDescription.isEmpty()) {
                    lockerInfo += String.format("\nDescription: %s", itemDescription);
                }
                lockerInfoLabel.setText(lockerInfo);
            } else {
                lockerInfoLabel.setText("No locker used");
            }
        } else {
            lockerInfoLabel.setText("No locker used");
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

        // Reset tất cả biến instance
        ticketDetails.clear();
        serviceDetails.clear();
        receivedAmountField.clear();
        changeLabel.setText("0 VND");
        movieTitleLabel.setText("No movie selected");
        showtimeLabel.setText("No showtime selected");
        roomLabel.setText("No room selected");
        lockerInfoLabel.setText("No locker used");

        // Reset locker data
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/ListMovies.fxml"));
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

    public static class TicketDetail {
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
        public double getPrice() { return price; }
    }

    public static class ServiceDetail {
        private String serviceName;
        private int quantity;
        private double unitPrice;
        private double totalPrice;

        public ServiceDetail(String serviceName, int quantity, double unitPrice, double totalPrice) {
            this.serviceName = serviceName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
        }

        public String getServiceName() { return serviceName; }
        public int getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }
        public double getTotalPrice() { return totalPrice; }
    }
}