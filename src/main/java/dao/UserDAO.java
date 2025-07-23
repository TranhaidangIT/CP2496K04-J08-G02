package dao;

import models.User; // Lớp mô hình User
import configs.DBConnection; // Lớp kết nối cơ sở dữ liệu
import utils.PasswordUtil; // Lớp tiện ích xử lý mật khẩu

import java.sql.*; // Các lớp JDBC để làm việc với cơ sở dữ liệu
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp này chứa các phương thức để thao tác với dữ liệu người dùng (User) trong cơ sở dữ liệu.
 */
public class UserDAO {

    /**
     /**
     * Kiểm tra thông tin đăng nhập của người dùng.
     * @param username Tên đăng nhập.
     * @param plainPassword Mật khẩu chưa mã hóa.
     * @return Đối tượng User nếu đăng nhập thành công, ngược lại là null.
     */
    public static User login(String username, String plainPassword) {
        String sql = "SELECT * FROM Users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("passwordHash");
                // Kiểm tra mật khẩu đã nhập với mật khẩu đã mã hóa trong DB
                if (PasswordUtil.checkPassword(plainPassword, hashedPassword)) {
                    // Tạo đối tượng User từ dữ liệu trong ResultSet
                    return new User(
                            rs.getInt("userId"),
                            rs.getString("employeeId"),
                            rs.getString("username"),
                            hashedPassword, // Lưu trữ mật khẩu đã hash trong đối tượng User
                            rs.getString("fullName"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("role"),
                            rs.getTimestamp("createdAt")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // In lỗi nếu có vấn đề về SQL
        }
        return null; // Trả về null nếu đăng nhập thất bại
    }

    /**
     * Lấy danh sách tất cả người dùng từ cơ sở dữ liệu.
     * @return Danh sách các đối tượng User.
     */
    public static List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM Users ORDER BY userId DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Tạo đối tượng User từ mỗi hàng trong ResultSet và thêm vào danh sách
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
                list.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Thêm một người dùng mới vào cơ sở dữ liệu.
     * @param u Đối tượng User cần thêm.
     * @return true nếu thêm thành công, ngược lại là false.
     */
    public static boolean insertUser(User u) {
        String sql = "INSERT INTO Users (employeeId, username, passwordHash, fullName, email, phone, role) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String hashed = PasswordUtil.hashPassword(u.getPassword()); // Mã hóa mật khẩu trước khi lưu

            ps.setString(1, u.getEmployeeId());
            ps.setString(2, u.getUsername());
            ps.setString(3, hashed);
            ps.setString(4, u.getFullName());
            ps.setString(5, u.getEmail());
            ps.setString(6, u.getPhone());
            ps.setString(7, u.getRole());

            return ps.executeUpdate() > 0; // Trả về true nếu có hàng nào bị ảnh hưởng (thêm thành công)

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật thông tin của một người dùng hiện có (không bao gồm mật khẩu).
     * @param u Đối tượng User chứa thông tin cập nhật.
     * @return true nếu cập nhật thành công, ngược lại là false.
     */
    public static boolean updateUser(User u) {
        String sql = "UPDATE Users SET fullName = ?, email = ?, phone = ?, role = ? WHERE userId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, u.getFullName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPhone());
            ps.setString(4, u.getRole());
            ps.setInt(5, u.getUserId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa một người dùng khỏi cơ sở dữ liệu dựa trên userId.
     * @param userId ID của người dùng cần xóa.
     * @return true nếu xóa thành công, ngược lại là false.
     */
    public static boolean deleteUser(int userId) {
        String sql = "DELETE FROM Users WHERE userId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật mật khẩu của người dùng dựa trên tên đăng nhập.
     * @param username Tên đăng nhập của người dùng.
     * @param newPlainPassword Mật khẩu mới (chưa mã hóa).
     * @return true nếu cập nhật thành công, ngược lại là false.
     */
    public static boolean updatePasswordByUsername(String username, String newPlainPassword) {
        String sql = "UPDATE Users SET passwordHash = ? WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String hashed = PasswordUtil.hashPassword(newPlainPassword); // Mã hóa mật khẩu mới

            ps.setString(1, hashed);
            ps.setString(2, username);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật mật khẩu của người dùng dựa trên email.
     * @param email Email của người dùng.
     * @param newPlainPassword Mật khẩu mới (chưa mã hóa).
     * @return true nếu cập nhật thành công, ngược lại là false.
     */
    public static boolean updatePasswordByEmail(String email, String newPlainPassword) {
        String sql = "UPDATE Users SET passwordHash = ? WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String hashedPassword = PasswordUtil.hashPassword(newPlainPassword); // Mã hóa mật khẩu mới

            ps.setString(1, hashedPassword);
            ps.setString(2, email);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0; // Trả về true nếu có hàng nào bị ảnh hưởng (cập nhật thành công)

        } catch (SQLException e) {
            System.err.println("Error updating password by email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}