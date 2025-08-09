package models;

public class SeatType {
    private int seatTypeId;
    private String seatTypeName;
    private double price;

    public SeatType() {}

    public SeatType(int seatTypeId, String seatTypeName, double price) {
        this.seatTypeId = seatTypeId;
        this.seatTypeName = seatTypeName;
        this.price = price;
    }

    public int getSeatTypeId() {
        return seatTypeId;
    }

    public void setSeatTypeId(int seatTypeId) {
        this.seatTypeId = seatTypeId;
    }

    public String getSeatTypeName() {
        return seatTypeName;
    }

    public void setSeatTypeName(String typeName) {
        this.seatTypeName = typeName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
