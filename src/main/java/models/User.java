package models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Đại diện cho một đối tượng người dùng với các thuộc tính phù hợp để liên kết dữ liệu JavaFX.
 * Mỗi thuộc tính được định nghĩa bằng cách sử dụng các lớp Property của JavaFX để cho phép
 * cập nhật UI tự động khi dữ liệu thay đổi và ngược lại.
 */
public class User {
    private final IntegerProperty id;
    private final StringProperty username;
    private final StringProperty password;
    private final StringProperty fullName;
    private final StringProperty role;

    /**
     * Xây dựng một đối tượng Người dùng mới.
     * @param id ID duy nhất của người dùng.
     * @param username Tên đăng nhập.
     * @param password Mật khẩu của người dùng (nên được mã hóa trong ứng dụng thực tế).
     * @param fullName Tên đầy đủ của người dùng.
     * @param role Vai trò của người dùng (ví dụ: "admin", "staff").
     */
    public User(int id, String username, String password, String fullName, String role) {
        this.id = new SimpleIntegerProperty(id);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.fullName = new SimpleStringProperty(fullName);
        this.role = new SimpleStringProperty(role);
    }

    // --- Getter thuộc tính cho liên kết dữ liệu JavaFX ---
    // Các phương thức này rất quan trọng để PropertyValueFactory truy cập các thuộc tính.
    // Chúng phải tuân theo quy ước đặt tên: [propertyName]Property()

    public IntegerProperty idProperty() { return id; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty passwordProperty() { return password; }
    public StringProperty fullNameProperty() { return fullName; }
    public StringProperty roleProperty() { return role; }

    // --- Getter tiêu chuẩn cho các giá trị thuộc tính ---
    // Các phương thức này truy xuất giá trị hiện tại của mỗi thuộc tính.

    public int getId() { return id.get(); }
    public String getUsername() { return username.get(); }
    public String getPassword() { return password.get(); }
    public String getFullName() { return fullName.get(); }
    public String getRole() { return role.get(); }

    // --- Setter tiêu chuẩn cho các giá trị thuộc tính ---
    // Các phương thức này cập nhật giá trị của mỗi thuộc tính.

    public void setId(int id) { this.id.set(id); }
    public void setUsername(String username) { this.username.set(username); }
    public void setPassword(String password) { this.password.set(password); }
    public void setFullName(String fullName) { this.fullName.set(fullName); }
    public void setRole(String role) { this.role.set(role); }
}