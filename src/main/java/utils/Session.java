package utils;

import models.User;
import java.util.Map;

/**
 * Lưu thông tin người dùng đang đăng nhập để dùng trong toàn hệ thống.
 */
public class Session {
    private static User currentUser;
    private static Map<String, Object> bookingData; // Thêm để lưu thông tin booking tạm thời

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
        clearBookingData(); // Xóa cả booking data khi clear session
    }

    // Thêm các method mới để quản lý booking data
    public static void setBookingData(Map<String, Object> data) {
        bookingData = data;
    }

    public static Map<String, Object> getBookingData() {
        return bookingData;
    }

    public static void clearBookingData() {
        bookingData = null;
    }
}