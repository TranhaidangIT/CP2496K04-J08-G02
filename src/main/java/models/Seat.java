package models;

public class Seat {
    private int seatId;
    private int roomId;
    private int seatRow;
    private int seatColumn;
    private String seatType;
    private String isActive;

    public Seat(int seatId, int roomId, int seatRow, int seatColumn, String seatType, String isActive) {
        this.seatId = seatId;
        this.roomId = roomId;
        this.seatRow = seatRow;
        this.seatColumn = seatColumn;
        this.seatType = seatType;
        this.isActive = isActive;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getSeatRow() {
        return seatRow;
    }

    public void setSeatRow(int seatRow) {
        this.seatRow = seatRow;
    }

    public int getSeatColumn() {
        return seatColumn;
    }

    public void setSeatColumn(int seatColumn) {
        this.seatColumn = seatColumn;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }


}
