package models;

public class SeatType {
    private int seatTypeId;
    private String typeName;
    private double price;

    public SeatType() {}

    public SeatType(int seatTypeId, String typeName, double price) {
        this.seatTypeId = seatTypeId;
        this.typeName = typeName;
        this.price = price;
    }

    public int getSeatTypeId() {

        return seatTypeId;
    }

    public void setSeatTypeId(int seatTypeId) {

        this.seatTypeId = seatTypeId;
    }

    public String getTypeName() {

        return typeName;
    }

    public void setTypeName(String typeName) {

        this.typeName = typeName;
    }

    public double getPrice() {

        return price;
    }

    public void setPrice(double price) {

        this.price = price;
    }
}
