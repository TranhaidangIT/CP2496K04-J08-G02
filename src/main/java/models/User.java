package models;

import java.sql.Timestamp;

public class User {
    private int userId;
    private String employeeId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private Timestamp createdAt;

    public User() {}

    public User(int userId, String employeeId, String username, String password, String fullName,
                String email, String phone, String role, Timestamp createdAt) {
        this.userId = userId;
        this.employeeId = employeeId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.createdAt = createdAt;
    }

    public int getUserId() { return userId; }
    public String getEmployeeId() { return employeeId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public Timestamp getCreatedAt() { return createdAt; }

    public void setUserId(int userId) { this.userId = userId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRole(String role) { this.role = role; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
