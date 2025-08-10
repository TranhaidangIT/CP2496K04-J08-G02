package models;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Ticket {

    private final IntegerProperty ticketId;
    private final StringProperty movieTitle;
    private final StringProperty customerUsername;
    private final StringProperty seatInfo;
    private final StringProperty showtimeInfo;
    private final DoubleProperty totalPrice;

    public Ticket() {
        this.ticketId = new SimpleIntegerProperty();
        this.movieTitle = new SimpleStringProperty();
        this.customerUsername = new SimpleStringProperty();
        this.seatInfo = new SimpleStringProperty();
        this.showtimeInfo = new SimpleStringProperty();
        this.totalPrice = new SimpleDoubleProperty();
    }

    // Getters
    public int getTicketId() { return ticketId.get(); }
    public String getMovieTitle() { return movieTitle.get(); }
    public String getCustomerUsername() { return customerUsername.get(); }
    public String getSeatInfo() { return seatInfo.get(); }
    public String getShowtimeInfo() { return showtimeInfo.get(); }
    public double getTotalPrice() { return totalPrice.get(); }

    // Setters
    public void setTicketId(int ticketId) { this.ticketId.set(ticketId); }
    public void setMovieTitle(String movieTitle) { this.movieTitle.set(movieTitle); }
    public void setCustomerUsername(String customerUsername) { this.customerUsername.set(customerUsername); }
    public void setSeatInfo(String seatInfo) { this.seatInfo.set(seatInfo); }
    public void setShowtimeInfo(String showtimeInfo) { this.showtimeInfo.set(showtimeInfo); }
    public void setTotalPrice(double totalPrice) { this.totalPrice.set(totalPrice); }

    // Property Getters (cho JavaFX TableView)
    public IntegerProperty ticketIdProperty() { return ticketId; }
    public StringProperty movieTitleProperty() { return movieTitle; }
    public StringProperty customerUsernameProperty() { return customerUsername; }
    public StringProperty seatInfoProperty() { return seatInfo; }
    public StringProperty showtimeInfoProperty() { return showtimeInfo; }
    public DoubleProperty totalPriceProperty() { return totalPrice; }
}