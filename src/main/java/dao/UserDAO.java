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
        String sql = "INSERT INTO Users (employeeId, username, passwordHash, fullName, email, phone, role, securityQuestion, securityAnswer, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                String hashedPassword = PasswordUtil.hashPassword(u.getPassword());
                String hashedSecurityAnswer = PasswordUtil.hashPassword(u.getSecurityAnswer()); // Hash security answer

                ps.setString(1, u.getEmployeeId());
                ps.setString(2, u.getUsername());
                ps.setString(3, hashedPassword);
                ps.setString(4, u.getFullName());
                ps.setString(5, u.getEmail());
                ps.setString(6, u.getPhone());
                ps.setString(7, u.getRole());
                ps.setString(8, u.getSecurityQuestion());
                ps.setString(9, hashedSecurityAnswer);
                // createdAt is set to NOW() in the query

                int affectedRows = ps.executeUpdate();
                if (affectedRows > 0) {
                    conn.commit(); // Commit transaction
                    return true;
                }
                conn.rollback(); // Rollback on failure
                return false;
            } catch (SQLException e) {
                conn.rollback(); // Rollback on exception
                throw e;
            } finally {
                conn.setAutoCommit(true); // Restore auto-commit
            }
        }
    }

    public static boolean updateUser(User u) throws SQLException {
        String sql = "UPDATE Users SET employeeId = ?, username = ?, passwordHash = ?, fullName = ?, email = ?, phone = ?, role = ?, securityQuestion = ?, securityAnswer = ? WHERE userId = ?";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, u.getEmployeeId());
                ps.setString(2, u.getUsername());
                ps.setString(3, u.getPassword()); // Assuming password is already hashed
                ps.setString(4, u.getFullName());
                ps.setString(5, u.getEmail());
                ps.setString(6, u.getPhone());
                ps.setString(7, u.getRole());
                ps.setString(8, u.getSecurityQuestion());
                ps.setString(9, PasswordUtil.hashPassword(u.getSecurityAnswer())); // Hash security answer
                ps.setInt(10, u.getUserId());

                int affectedRows = ps.executeUpdate();
                if (affectedRows > 0) {
                    conn.commit(); // Commit transaction
                    return true;
                }
                conn.rollback(); // Rollback on failure
                return false;
            } catch (SQLException e) {
                conn.rollback(); // Rollback on exception
                throw e;
            } finally {
                conn.setAutoCommit(true); // Restore auto-commit
            }
        }
    }

    public static boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM Users WHERE userId = ?";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                int affectedRows = ps.executeUpdate();
                if (affectedRows > 0) {
                    conn.commit(); // Commit transaction
                    return true;
                }
                conn.rollback(); // Rollback on failure
                return false;
            } catch (SQLException e) {
                conn.rollback(); // Rollback on exception
                throw e;
            } finally {
                conn.setAutoCommit(true); // Restore auto-commit
            }
        }
    }

    public static boolean updatePasswordByUsername(String username, String newPlainPassword) throws SQLException {
        String sql = "UPDATE Users SET passwordHash = ? WHERE username = ?";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                String hashed = PasswordUtil.hashPassword(newPlainPassword);
                ps.setString(1, hashed);
                ps.setString(2, username);
                int affectedRows = ps.executeUpdate();
                if (affectedRows > 0) {
                    conn.commit(); // Commit transaction
                    return true;
                }
                conn.rollback(); // Rollback on failure
                return false;
            } catch (SQLException e) {
                conn.rollback(); // Rollback on exception
                throw e;
            } finally {
                conn.setAutoCommit(true); // Restore auto-commit
            }
        }
    }

    public static boolean updatePasswordByEmail(String email, String newPlainPassword) throws SQLException {
        String sql = "UPDATE Users SET passwordHash = ? WHERE email = ?";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                String hashedPassword = PasswordUtil.hashPassword(newPlainPassword);
                ps.setString(1, hashedPassword);
                ps.setString(2, email);
                int affectedRows = ps.executeUpdate();
                if (affectedRows > 0) {
                    conn.commit(); // Commit transaction
                    return true;
                }
                conn.rollback(); // Rollback on failure
                return false;
            } catch (SQLException e) {
                conn.rollback(); // Rollback on exception
                throw e;
            } finally {
                conn.setAutoCommit(true); // Restore auto-commit
            }
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
        } catch (SQLException e) {
            System.err.println("Error getting total users count: " + e.getMessage());
            throw e;
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