package models;

public class RoomType {
    private int roomTypeId;
    private String typeName;
    private String description;
    private int maxRows;
    private int maxColumns;

    public RoomType() {
    }

    public RoomType(int roomTypeId, String roomName, String description,
                    int maxRows, int maxColumns) {
        this.roomTypeId = roomTypeId;
        this.typeName = roomName;
        this.description = description;
        this.maxRows = maxRows;
        this.maxColumns = maxColumns;
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

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    public int getMaxColumns() {
        return maxColumns;
    }

    public void setMaxColumns(int maxColumns) {
        this.maxColumns = maxColumns;
    }


    @Override
    public String toString() {
        return typeName;
    }

}
