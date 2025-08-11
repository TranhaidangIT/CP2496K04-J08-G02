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
    private String securityQuestion;
    private String securityAnswer;

    /**
     * Constructor đầy đủ với tất cả các thuộc tính.
     */
    public User(int userId, String employeeId, String username, String password, String fullName, String email, String phone, String role, Timestamp createdAt, String securityQuestion, String securityAnswer) {
        this.userId = userId;
        this.employeeId = employeeId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.createdAt = createdAt;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }

    /**
     * Constructor dùng cho các trường hợp không có securityQuestion và securityAnswer.
     */
    public User(int userId, String employeeId, String username, String password, String fullName, String email, String phone, String role, Timestamp createdAt) {
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

    /**
     * Constructor mặc định (không có đối số).
     */
    public User() {}

    // Các phương thức Getter và Setter cho tất cả các thuộc tính
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(String securityQuestion) { this.securityQuestion = securityQuestion; }
    public String getSecurityAnswer() { return securityAnswer; }
    public void setSecurityAnswer(String securityAnswer) { this.securityAnswer = securityAnswer; }
}