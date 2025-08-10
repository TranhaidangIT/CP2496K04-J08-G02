package controller.controllerEmployees;

import configs.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import models.*;
import utils.Session;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LockerController {

    @FXML private GridPane lockerGrid;
    @FXML private Label selectedLockerLabel;
    @FXML private VBox bookingInfoBox; // Added to reference the new VBox
    @FXML private Label movieTitleLabel;
    @FXML private Label showtimeLabel;
    @FXML private Label roomLabel;
    @FXML private Label seatsLabel;
    @FXML private Label addonsLabel;
    @FXML private Label totalPriceLabel;
    @FXML private Button skipLockerButton;
    @FXML private Button finishWithLockerButton;
    @FXML private Button backToAddonsButton;
    @FXML private VBox lockerAssignmentBox;
    @FXML private TextField itemDescriptionField;
    @FXML private Label pinCodeLabel;

    private final List<Locker> availableLockers = new ArrayList<>();
    private Locker selectedLocker = null;
    private Map<String, Object> bookingData;
    private String generatedPinCode = null;
    private boolean hasBookingData = false;

    @FXML
    public void initialize() {
        setupLockerAssignmentControls();
        hasBookingData = loadBookingData();
        loadAllLockers();
        displayLockers();
        updateNavigationButtonsVisibility();
    }

    private void setupLockerAssignmentControls() {
        if (lockerAssignmentBox != null) {
            lockerAssignmentBox.setVisible(false);
        }
    }

    private boolean loadBookingData() {
        bookingData = Session.getBookingData();
        if (bookingData != null) {
            if (bookingData.get("showtime") != null && bookingData.get("selectedSeats") != null) {
                displayBookingInfo();
                return true;
            }
        }
        if (movieTitleLabel != null) {
            movieTitleLabel.setText("No booking information.");
            showtimeLabel.setText("");
            roomLabel.setText("");
            seatsLabel.setText("");
            addonsLabel.setText("");
            totalPriceLabel.setText("Please go back to select a ticket.");
        }
        return false;
    }

    private void updateNavigationButtonsVisibility() {
        if (skipLockerButton != null) {
            skipLockerButton.setVisible(hasBookingData);
        }
        if (finishWithLockerButton != null) {
            finishWithLockerButton.setVisible(hasBookingData);
        }
        if (backToAddonsButton != null) {
            backToAddonsButton.setVisible(hasBookingData);
        }
    }

    private void displayBookingInfo() {
        if (bookingInfoBox == null) return;

        Showtime showtime = (Showtime) bookingData.get("showtime");
        List<Map<String, Object>> seats = (List<Map<String, Object>>) bookingData.get("selectedSeats");

        // Handle both BigDecimal and Double for prices
        BigDecimal seatsTotal = convertToBigDecimal(bookingData.get("seatsTotalPrice"));
        BigDecimal addonsTotal = convertToBigDecimal(bookingData.get("addonsTotalPrice"));

        List<Service> selectedAddons = (List<Service>) bookingData.get("selectedAddons");

        if (showtime != null && seats != null) {
            // Update movie title
            movieTitleLabel.setText("Movie: " + showtime.getMovieTitle());

            // Update showtime
            showtimeLabel.setText("Showtime: " + showtime.getShowDate() + " " + showtime.getShowTime());

            // Update room
            roomLabel.setText("Room: " + showtime.getRoomName());

            // Update seat list
            StringBuilder seatsInfo = new StringBuilder("Seats: ");
            for (int i = 0; i < seats.size(); i++) {
                Map<String, Object> seat = seats.get(i);
                seatsInfo.append(seat.get("seatRow")).append(seat.get("seatColumn"));
                if (i < seats.size() - 1) seatsInfo.append(", ");
            }
            seatsLabel.setText(seatsInfo.toString());

            // Update add-ons
            StringBuilder addonsInfo = new StringBuilder("Add-ons: ");
            if (selectedAddons != null && !selectedAddons.isEmpty()) {
                for (int i = 0; i < selectedAddons.size(); i++) {
                    Service addon = selectedAddons.get(i);
                    addonsInfo.append(addon.getServiceName()).append(" (").append(String.format("%.0f VND", addon.getPrice())).append(")");
                    if (i < selectedAddons.size() - 1) addonsInfo.append(", ");
                }
            } else {
                addonsInfo.append("None");
            }
            addonsLabel.setText(addonsInfo.toString());

            // Update total price - use BigDecimal arithmetic
            BigDecimal totalPrice = (seatsTotal != null ? seatsTotal : BigDecimal.ZERO)
                    .add(addonsTotal != null ? addonsTotal : BigDecimal.ZERO);
            totalPriceLabel.setText("Total: " + String.format("%.0f VND", totalPrice.doubleValue()));
        } else {
            movieTitleLabel.setText("No booking information.");
            showtimeLabel.setText("");
            roomLabel.setText("");
            seatsLabel.setText("");
            addonsLabel.setText("");
            totalPriceLabel.setText("Please go back to select a ticket.");
        }
    }

    /**
     * Helper method to convert various number types to BigDecimal
     */
    private BigDecimal convertToBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Double) {
            return BigDecimal.valueOf((Double) value);
        }
        if (value instanceof Float) {
            return BigDecimal.valueOf((Float) value);
        }
        if (value instanceof Integer) {
            return BigDecimal.valueOf((Integer) value);
        }
        if (value instanceof Long) {
            return BigDecimal.valueOf((Long) value);
        }
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    private void loadAllLockers() {
        availableLockers.clear();
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT lockerId, lockerNumber, locationInfo, status FROM lockers ORDER BY lockerNumber";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Locker locker = new Locker(
                        rs.getInt("lockerId"),
                        rs.getString("lockerNumber"),
                        rs.getString("locationInfo"),
                        rs.getString("status")
                );
                availableLockers.add(locker);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load lockers: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void displayLockers() {
        if (lockerGrid == null) return;

        lockerGrid.getChildren().clear();
        int cols = 8;
        int row = 0, col = 0;

        for (Locker locker : availableLockers) {
            Button lockerButton = createLockerButton(locker);
            lockerGrid.add(lockerButton, col, row);
            GridPane.setMargin(lockerButton, new Insets(5));

            col++;
            if (col >= cols) {
                col = 0;
                row++;
            }
        }
    }

    private Button createLockerButton(Locker locker) {
        Button button = new Button(locker.getLockerNumber());
        button.setPrefSize(90, 70);
        String style = getLockerButtonStyle(locker.getStatus());
        button.setStyle(style);

        if ("Available".equalsIgnoreCase(locker.getStatus())) {
            button.setOnAction(e -> handleAvailableLockerClick(locker));
        } else if ("Occupied".equalsIgnoreCase(locker.getStatus())) {
            button.setOnAction(e -> handleOccupiedLockerClick(locker));
        } else {
            button.setOnAction(e -> showLockerInfo(locker));
        }

        return button;
    }

    private void handleAvailableLockerClick(Locker locker) {
        if (!hasBookingData) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Booking Information Required");
            alert.setHeaderText("To select a new locker");
            alert.setContentText("You need booking information to select a locker.\nThe system will redirect you to the movie selection page.");

            alert.setOnHidden(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/ListMovies.fxml"));
                    Parent movieRoot = loader.load();
                    navigateToPage(movieRoot);
                } catch (IOException ex) {
                    System.err.println("Error redirecting to movie selection page: " + ex.getMessage());
                }
            });
            alert.showAndWait();
            return;
        }

        selectLocker(locker);
    }

    private void handleOccupiedLockerClick(Locker locker) {
        showRetrieveItemDialog(locker);
    }

    private void showRetrieveItemDialog(Locker locker) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Retrieve Item from Locker " + locker.getLockerNumber());
        dialog.setHeaderText("Enter PIN to retrieve item");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        Label infoLabel = new Label("Locker: " + locker.getLockerNumber() + " - " + locker.getLocationInfo());
        infoLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        Label pinLabel = new Label("PIN (4 digits):");
        TextField pinField = new TextField();
        pinField.setPromptText("Enter PIN...");
        pinField.setPrefWidth(200);

        pinField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                pinField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 4) {
                pinField.setText(oldValue);
            }
        });

        content.getChildren().addAll(infoLabel, pinLabel, pinField);
        dialog.getDialogPane().setContent(content);

        ButtonType retrieveButtonType = new ButtonType("Retrieve Item", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(retrieveButtonType, cancelButtonType);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == retrieveButtonType) {
                return pinField.getText().trim();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(pin -> {
            if (pin != null && pin.length() == 4) {
                processRetrieveItem(locker, pin);
            } else {
                showAlert("Error", "PIN must be exactly 4 digits!", Alert.AlertType.ERROR);
            }
        });
    }

    private void processRetrieveItem(Locker locker, String enteredPin) {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("DEBUG: Checking locker " + locker.getLockerId() + " with PIN: " + enteredPin);

            String checkQuery = "SELECT la.assignmentId, la.pinCode, la.itemDescription, la.assignedAt " +
                    "FROM lockerAssignments la " +
                    "WHERE la.lockerId = ? AND la.releasedAt IS NULL";

            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, locker.getLockerId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String correctPin = rs.getString("pinCode");
                String itemDescription = rs.getString("itemDescription");
                int assignmentId = rs.getInt("assignmentId");

                System.out.println("DEBUG: Found assignment ID: " + assignmentId);
                System.out.println("DEBUG: Correct PIN: " + correctPin);
                System.out.println("DEBUG: Item: " + itemDescription);

                if (correctPin != null && correctPin.trim().equals(enteredPin.trim())) {
                    try {
                        releaseLocker(conn, locker.getLockerId(), assignmentId);

                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Item Retrieved Successfully");
                        successAlert.setHeaderText("Locker " + locker.getLockerNumber() + " has been opened");
                        successAlert.setContentText("Item: " + itemDescription + "\n\nPlease retrieve your item and close the locker.");
                        successAlert.showAndWait();

                        loadAllLockers();
                        displayLockers();

                        System.out.println("DEBUG: Successfully released locker");

                    } catch (SQLException e) {
                        System.err.println("DEBUG: Error releasing locker: " + e.getMessage());
                        showAlert("Error", "Failed to update locker status: " + e.getMessage(), Alert.AlertType.ERROR);
                    }

                } else {
                    System.out.println("DEBUG: PIN mismatch. Expected: '" + correctPin + "', Entered: '" + enteredPin + "'");
                    showAlert("Incorrect PIN",
                            "The PIN does not match.\nEntered PIN: " + enteredPin +
                                    "\nPlease check the PIN on your ticket.",
                            Alert.AlertType.ERROR);
                }
            } else {
                System.out.println("DEBUG: No active assignment found for locker " + locker.getLockerId());

                String checkAllQuery = "SELECT COUNT(*) as total FROM lockerAssignments WHERE lockerId = ?";
                PreparedStatement checkAllStmt = conn.prepareStatement(checkAllQuery);
                checkAllStmt.setInt(1, locker.getLockerId());
                ResultSet allRs = checkAllStmt.executeQuery();

                if (allRs.next() && allRs.getInt("total") > 0) {
                    showAlert("Error", "This locker has already been retrieved or has no stored items.", Alert.AlertType.ERROR);
                } else {
                    showAlert("Error", "No storage information found for this locker.", Alert.AlertType.ERROR);
                }
            }

        } catch (SQLException e) {
            System.err.println("DEBUG: Database error: " + e.getMessage());
            e.printStackTrace();
            showAlert("Database Error", "Failed to check locker information: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void releaseLocker(Connection conn, int lockerId, int assignmentId) throws SQLException {
        System.out.println("DEBUG: Starting releaseLocker for lockerId: " + lockerId + ", assignmentId: " + assignmentId);

        boolean originalAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            String updateAssignmentQuery = "UPDATE lockerAssignments SET releasedAt = ? WHERE assignmentId = ?";
            PreparedStatement updateAssignmentStmt = conn.prepareStatement(updateAssignmentQuery);
            updateAssignmentStmt.setObject(1, LocalDateTime.now());
            updateAssignmentStmt.setInt(2, assignmentId);
            int assignmentRows = updateAssignmentStmt.executeUpdate();
            System.out.println("DEBUG: Updated " + assignmentRows + " assignment rows");

            String updateLockerQuery = "UPDATE lockers SET status = 'Available' WHERE lockerId = ?";
            PreparedStatement updateLockerStmt = conn.prepareStatement(updateLockerQuery);
            updateLockerStmt.setInt(1, lockerId);
            int lockerRows = updateLockerStmt.executeUpdate();
            System.out.println("DEBUG: Updated " + lockerRows + " locker rows");

            logLockerHistory(conn, lockerId, "Item retrieved", "Customer retrieved items");
            System.out.println("DEBUG: Logged history");

            conn.commit();
            System.out.println("DEBUG: Transaction committed successfully");

        } catch (SQLException e) {
            System.err.println("DEBUG: Error in releaseLocker: " + e.getMessage());
            e.printStackTrace();
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    private String getLockerButtonStyle(String status) {
        switch (status.toLowerCase()) {
            case "available":
                return "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;";
            case "occupied":
                return "-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;";
            case "maintenance":
                return "-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-opacity: 0.7;";
            default:
                return "-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-opacity: 0.7;";
        }
    }

    private void showLockerInfo(Locker locker) {
        String message;
        switch (locker.getStatus().toLowerCase()) {
            case "occupied":
                message = "Locker " + locker.getLockerNumber() + " is currently in use.\n" +
                        "Click the locker and enter the PIN to retrieve the item.";
                break;
            case "maintenance":
                message = "Locker " + locker.getLockerNumber() + " is under maintenance.\n" +
                        "Please select another locker or contact staff.";
                break;
            default:
                message = "Locker " + locker.getLockerNumber() + " is not available.";
                break;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Locker Information");
        alert.setHeaderText("Locker " + locker.getLockerNumber());
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void selectLocker(Locker locker) {
        this.selectedLocker = locker;

        selectedLockerLabel.setText("Selected: Locker " + locker.getLockerNumber() +
                " - " + locker.getLocationInfo());

        // Tạo PIN mới mỗi lần chọn locker
        generatedPinCode = generatePinCode();
        System.out.println("DEBUG: Generated new PIN for locker " + locker.getLockerNumber() + ": " + generatedPinCode);

        if (lockerAssignmentBox != null) {
            lockerAssignmentBox.setVisible(true);
            pinCodeLabel.setText("PIN: " + generatedPinCode);
        }

        refreshLockerDisplay();
    }

    private void refreshLockerDisplay() {
        lockerGrid.getChildren().forEach(node -> {
            if (node instanceof Button) {
                Button btn = (Button) node;
                String lockerNum = btn.getText();

                if (selectedLocker != null && lockerNum.equals(selectedLocker.getLockerNumber())) {
                    btn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
                } else {
                    Locker originalLocker = availableLockers.stream()
                            .filter(l -> l.getLockerNumber().equals(lockerNum))
                            .findFirst().orElse(null);
                    if (originalLocker != null) {
                        btn.setStyle(getLockerButtonStyle(originalLocker.getStatus()));
                    }
                }
            }
        });
    }

    private String generatePinCode() {
        return String.format("%04d", new Random().nextInt(10000));
    }

    @FXML
    private void handleSkipLocker() {
        navigateToTotal(false);
    }

    @FXML
    private void handleFinishWithLocker() {
        if (selectedLocker == null) {
            showAlert("No Locker Selected", "Please select a locker before proceeding.", Alert.AlertType.WARNING);
            return;
        }

        String itemDescription = itemDescriptionField.getText().trim();
        if (itemDescription.isEmpty()) {
            showAlert("Missing Information", "Please describe the item to be stored.", Alert.AlertType.WARNING);
            return;
        }

        bookingData.put("selectedLocker", selectedLocker);
        bookingData.put("lockerPinCode", generatedPinCode);
        bookingData.put("itemDescription", itemDescription);

        navigateToTotal(true);
    }

    @FXML
    private void handleBackToAddons() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/SellAddons.fxml"));
            Parent addonsRoot = loader.load();

            SellAddonsController controller = loader.getController();
            controller.setBookingData(bookingData);

            navigateToPage(addonsRoot);
        } catch (IOException ex) {
            showAlert("Error", "Failed to return to add-ons page: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void navigateToTotal(boolean hasLocker) {
        try {
            if (hasLocker) {
                if (selectedLocker == null) {
                    showAlert("No Locker Selected", "Please select a locker before proceeding.", Alert.AlertType.WARNING);
                    return;
                }
                String itemDescription = itemDescriptionField.getText().trim();
                if (itemDescription.isEmpty()) {
                    showAlert("Missing Information", "Please describe the item to be stored.", Alert.AlertType.WARNING);
                    return;
                }

                // Store locker information in bookingData
                bookingData.put("selectedLocker", selectedLocker);
                bookingData.put("lockerPinCode", generatedPinCode);
                bookingData.put("itemDescription", itemDescription);

                // DEBUG: In ra thông tin để kiểm tra
                System.out.println("DEBUG: Storing locker data:");
                System.out.println("- Locker: " + selectedLocker.getLockerNumber());
                System.out.println("- PIN: " + generatedPinCode);
                System.out.println("- Description: " + itemDescription);

                Session.setBookingData(bookingData);

                // Verify data was stored correctly
                Map<String, Object> verifyData = Session.getBookingData();
                System.out.println("DEBUG: Verified stored data:");
                System.out.println("- PIN from Session: " + verifyData.get("lockerPinCode"));
            }

            // Navigate to Total.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Employees/Total.fxml"));
            Parent totalRoot = loader.load();
            TotalController controller = loader.getController();
            controller.setContentArea((AnchorPane) lockerGrid.getScene().lookup("#contentArea"));
            navigateToPage(totalRoot);

        } catch (IOException ex) {
            showAlert("Error", "Failed to navigate to payment page: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void navigateToPage(Parent page) {
        AnchorPane parent = (AnchorPane) lockerGrid.getScene().lookup("#contentArea");
        if (parent != null) {
            parent.getChildren().setAll(page);
            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);
        }
    }

    private void updateLockerStatus(int lockerId, String newStatus) {
        try (Connection conn = DBConnection.getConnection()) {
            String updateQuery = "UPDATE lockers SET status = ? WHERE lockerId = ?";
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, newStatus);
            stmt.setInt(2, lockerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating locker status: " + e.getMessage());
        }
    }

    // Updated assignLockerToCustomer method in LockerController
    public static void assignLockerToCustomer(Connection conn, int lockerId, String ticketCode, String pinCode, String itemDescription, String customerName) throws SQLException {
        // Check if ticketCode exists
        String checkQuery = "SELECT COUNT(*) FROM tickets WHERE ticketCode = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, ticketCode);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) == 0) {
                throw new SQLException("TicketCode " + ticketCode + " does not exist in the tickets table.");
            }
        }

        // Insert into lockerAssignments
        String insertQuery = "INSERT INTO lockerAssignments (lockerId, pinCode, itemDescription, ticketCode, assignedAt) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setInt(1, lockerId);
            stmt.setString(2, pinCode);
            stmt.setString(3, itemDescription);
            stmt.setString(4, ticketCode);
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        }

        // Update locker status
        String updateQuery = "UPDATE lockers SET status = 'Occupied' WHERE lockerId = ? AND status = 'Available'";
        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
            updateStmt.setInt(1, lockerId);
            int rowsAffected = updateStmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to update locker status: Locker does not exist or is not in Available status.");
            }
        }

        // Log history
        logLockerHistory(conn, lockerId, "Assigned to customer", "Customer: " + customerName + ", Ticket: " + ticketCode);
    }

    private static void logLockerHistory(Connection conn, int lockerId, String action, String userInfo) throws SQLException {
        String insertQuery = "INSERT INTO lockerHistory (lockerId, userAction, actionTime, userInfo) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setInt(1, lockerId);
            stmt.setString(2, action);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(4, userInfo);
            stmt.executeUpdate();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setBookingData(Map<String, Object> bookingData) {
        this.bookingData = bookingData;
        hasBookingData = loadBookingData();
        updateNavigationButtonsVisibility();
    }
}