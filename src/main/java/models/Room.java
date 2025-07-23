package models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Đại diện cho một đối tượng phòng chiếu với các thuộc tính phù hợp để liên kết dữ liệu JavaFX.
 * Mỗi thuộc tính được định nghĩa bằng cách sử dụng các lớp Property của JavaFX để cho phép
 * cập nhật UI tự động khi dữ liệu thay đổi và ngược lại.
 */
public class Room {
    private final StringProperty id;
    private final StringProperty roomName;
    private final StringProperty roomType;
    private final IntegerProperty numSeats;
    private final StringProperty status;

    /**
     * Xây dựng một đối tượng Phòng chiếu mới.
     * @param id ID duy nhất của phòng chiếu.
     * @param roomName Tên của phòng (ví dụ: "Room 01").
     * @param roomType Loại phòng (ví dụ: "2D", "3D", "IMAX").
     * @param numSeats Tổng số ghế trong phòng.
     * @param status Trạng thái của phòng (ví dụ: "Active", "Maintenance").
     */
    public Room(String id, String roomName, String roomType, int numSeats, String status) {
        this.id = new SimpleStringProperty(id);
        this.roomName = new SimpleStringProperty(roomName);
        this.roomType = new SimpleStringProperty(roomType);
        this.numSeats = new SimpleIntegerProperty(numSeats);
        this.status = new SimpleStringProperty(status);
    }

    // --- Getter thuộc tính cho liên kết dữ liệu JavaFX ---
    // Các phương thức này rất quan trọng để PropertyValueFactory truy cập các thuộc tính.

    public StringProperty idProperty() { return id; }
    public StringProperty roomNameProperty() { return roomName; }
    public StringProperty roomTypeProperty() { return roomType; }
    public IntegerProperty numSeatsProperty() { return numSeats; }
    public StringProperty statusProperty() { return status; }

    // --- Standard Getters for Property Values ---

    public String getId() { return id.get(); }
    public String getRoomName() { return roomName.get(); }
    public String getRoomType() { return roomType.get(); }
    public int getNumSeats() { return numSeats.get(); }
    public String getStatus() { return status.get(); }

    // --- Standard Setters for Property Values ---

    public void setId(String id) { this.id.set(id); }
    public void setRoomName(String roomName) { this.roomName.set(roomName); }
    public void setRoomType(String roomType) { this.roomType.set(roomType); }
    public void setNumSeats(int numSeats) { this.numSeats.set(numSeats); }
    public void setStatus(String status) { this.status.set(status); }
}