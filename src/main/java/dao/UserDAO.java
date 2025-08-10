package dao;

import models.User;
import configs.DBConnection;
import utils.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public static User login(String username, String plainPassword) throws SQLException {
        String sql = "SELECT * FROM Users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("passwordHash");
                    if (PasswordUtil.checkPassword(plainPassword, hashedPassword)) {
                        User user = new User(
                                rs.getInt("userId"),
                                rs.getString("employeeId"),
                                rs.getString("username"),
                                hashedPassword,
                                rs.getString("fullName"),
                                rs.getString("email"),
                                rs.getString("phone"),
                                rs.getString("role"),
                                rs.getTimestamp("createdAt")
                        );
                        user.setSecurityQuestion(rs.getString("securityQuestion"));
                        user.setSecurityAnswer(rs.getString("securityAnswer"));
                        return user;
                    }
                }
            }
        }
        return null;
    }

    public static List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM Users ORDER BY userId DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User u = new User(
                        rs.getInt("userId"),
                        rs.getString("employeeId"),
                        rs.getString("username"),
                        rs.getString("passwordHash"),
                        rs.getString("fullName"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("role"),
                        rs.getTimestamp("createdAt")
                );
                u.setSecurityQuestion(rs.getString("securityQuestion"));
                u.setSecurityAnswer(rs.getString("securityAnswer"));
                list.add(u);
            }
        }
        return list;
    }

    public static boolean addUser(User u) throws SQLException {
        String sql = "INSERT INTO Users (employeeId, username, passwordHash, fullName, email, phone, role, securityQuestion, securityAnswer) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String hashedPassword = PasswordUtil.hashPassword(u.getPassword());
            // It's a good practice to hash the security answer as well for better security.
            // For now, we'll store it as plain text as per your original code, but hashing is recommended.
            String securityAnswer = u.getSecurityAnswer();

            ps.setString(1, u.getEmployeeId());
            ps.setString(2, u.getUsername());
            ps.setString(3, hashedPassword);
            ps.setString(4, u.getFullName());
            ps.setString(5, u.getEmail());
            ps.setString(6, u.getPhone());
            ps.setString(7, u.getRole());
            ps.setString(8, u.getSecurityQuestion());
            ps.setString(9, securityAnswer);
            return ps.executeUpdate() > 0;
        }
    }

    // Updated updateUser to include securityQuestion and securityAnswer
    public static boolean updateUser(User u) throws SQLException {
        String sql = "UPDATE Users SET fullName = ?, email = ?, phone = ?, role = ?, securityQuestion = ?, securityAnswer = ? WHERE userId = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPhone());
            ps.setString(4, u.getRole());
            ps.setString(5, u.getSecurityQuestion());
            ps.setString(6, u.getSecurityAnswer());
            ps.setInt(7, u.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM Users WHERE userId = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean updatePasswordByUsername(String username, String newPlainPassword) throws SQLException {
        String sql = "UPDATE Users SET passwordHash = ? WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String hashed = PasswordUtil.hashPassword(newPlainPassword);
            ps.setString(1, hashed);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean updatePasswordByEmail(String email, String newPlainPassword) throws SQLException {
        String sql = "UPDATE Users SET passwordHash = ? WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String hashedPassword = PasswordUtil.hashPassword(newPlainPassword);
            ps.setString(1, hashedPassword);
            ps.setString(2, email);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        }
    }

    public static boolean isEmailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public static boolean isEmployeeIdExists(String employeeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE employeeId = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public static boolean isUsernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public static boolean isUserExists(String employeeId, String username, String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE employeeId = ? AND username = ? AND email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            ps.setString(2, username);
            ps.setString(3, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public static int getTotalUsersCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM Users";
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public static String[] getSecurityQuestionAndAnswer(String username) throws SQLException {
        String sql = "SELECT securityQuestion, securityAnswer FROM Users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{rs.getString("securityQuestion"), rs.getString("securityAnswer")};
                }
            }
        }
        return null;
    }
}