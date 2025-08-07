package models;

import java.time.LocalDateTime;

public class ScreeningRoom {
    private int roomId;
    private String roomNumber;
    private String seatingLayout;
    private int totalCapacity;
    private int roomTypeId;
    private String typeName;
    private String equipment;
    private String roomStatus;
    private LocalDateTime createdAt;

    public ScreeningRoom() {

    }

    public ScreeningRoom(int roomId, String roomNumber, String seatingLayout, int totalCapacity,
                         int roomTypeId, String typeName, String equipment,
                         String roomStatus, LocalDateTime createdAt) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.seatingLayout = seatingLayout;
        this.totalCapacity = totalCapacity;
        this.roomTypeId = roomTypeId;
        this.typeName = typeName;
        this.equipment = equipment;
        this.roomStatus = roomStatus;
        this.createdAt = createdAt;
    }

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

    public int getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(int roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String TypeName) {
        this.typeName = TypeName;
    }


    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipmentList) {
        this.equipment = equipmentList;
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

}
