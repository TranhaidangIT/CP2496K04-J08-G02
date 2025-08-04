package models;

import java.time.LocalDateTime;

public class ScreeningRoom {
    private int roomId;
    private String roomNumber;
    private String seatingLayout;
    private int totalCapacity;
    private String roomType;
    private String equipment;
    private String roomStatus;
    private LocalDateTime createdAt;

    public ScreeningRoom(int roomId, String roomNumber, String roomType, String roomStatus,
                         String seatingLayout, int totalCapacity, String equipment, LocalDateTime createdAt) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.roomStatus = roomStatus;
        this.seatingLayout = seatingLayout;
        this.totalCapacity = totalCapacity;
        this.equipment = equipment;
        this.createdAt = createdAt;
    }

    public ScreeningRoom(String roomNumber, String roomType, String roomStatus,
                         String seatingLayout, int totalCapacity, String equipment) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.roomStatus = roomStatus;
        this.seatingLayout = seatingLayout;
        this.totalCapacity = totalCapacity;
        this.equipment = equipment;
    }

    // Getters & Setters
    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getSeatingLayout() {
        return seatingLayout;
    }

    public void setSeatingLayout(String seatingLayout) {
        this.seatingLayout = seatingLayout;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(String roomStatus) {
        this.roomStatus = roomStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ScreeningRoom{" +
                "roomId=" + roomId +
                ", roomNumber='" + roomNumber + '\'' +
                ", seatingLayout='" + seatingLayout + '\'' +
                ", totalCapacity=" + totalCapacity +
                ", roomType='" + roomType + '\'' +
                ", equipment='" + equipment + '\'' +
                ", roomStatus='" + roomStatus + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
