package models;

import javafx.beans.property.*;

public class Ticket {
    private final IntegerProperty id;
    private final StringProperty guestName;
    private final StringProperty phone;
    private final StringProperty movieTitle;
    private final IntegerProperty roomNumber;
    private final StringProperty showtime;
    private final StringProperty seatNumber;
    private final StringProperty status;
    private final DoubleProperty price; // Thêm thuộc tính giá vé để tính tổng doanh thu

    public Ticket(int id, String guestName, String phone, String movieTitle, int roomNumber, String showtime, String seatNumber, String status, double price) {
        this.id = new SimpleIntegerProperty(id);
        this.guestName = new SimpleStringProperty(guestName);
        this.phone = new SimpleStringProperty(phone);
        this.movieTitle = new SimpleStringProperty(movieTitle);
        this.roomNumber = new SimpleIntegerProperty(roomNumber);
        this.showtime = new SimpleStringProperty(showtime);
        this.seatNumber = new SimpleStringProperty(seatNumber);
        this.status = new SimpleStringProperty(status);
        this.price = new SimpleDoubleProperty(price);
    }

    // Getters cho thuộc tính (JavaFX Property values)
    public IntegerProperty idProperty() { return id; }
    public StringProperty guestNameProperty() { return guestName; }
    public StringProperty phoneProperty() { return phone; }
    public StringProperty movieTitleProperty() { return movieTitle; }
    public IntegerProperty roomNumberProperty() { return roomNumber; }
    public StringProperty showtimeProperty() { return showtime; }
    public StringProperty seatNumberProperty() { return seatNumber; }
    public StringProperty statusProperty() { return status; }
    public DoubleProperty priceProperty() { return price; }

    // Getters cho giá trị (để sử dụng trực tiếp trong code, ví dụ tính tổng)
    public int getId() { return id.get(); }
    public String getGuestName() { return guestName.get(); }
    public String getPhone() { return phone.get(); }
    public String getMovieTitle() { return movieTitle.get(); }
    public int getRoomNumber() { return roomNumber.get(); }
    public String getShowtime() { return showtime.get(); }
    public String getSeatNumber() { return seatNumber.get(); }
    public String getStatus() { return status.get(); }
    public double getPrice() { return price.get(); }

    // Setters (tùy chọn, nếu bạn muốn chỉnh sửa dữ liệu)
    public void setId(int id) { this.id.set(id); }
    public void setGuestName(String guestName) { this.guestName.set(guestName); }
    public void setPhone(String phone) { this.phone.set(phone); }
    public void setMovieTitle(String movieTitle) { this.movieTitle.set(movieTitle); }
    public void setRoomNumber(int roomNumber) { this.roomNumber.set(roomNumber); }
    public void setShowtime(String showtime) { this.showtime.set(showtime); }
    public void setSeatNumber(String seatNumber) { this.seatNumber.set(seatNumber); }
    public void setStatus(String status) { this.status.set(status); }
    public void setPrice(double price) { this.price.set(price); }
}