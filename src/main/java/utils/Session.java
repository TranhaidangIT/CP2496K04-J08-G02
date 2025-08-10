package utils;

import models.User;
import java.util.Map;

/**
 * Lưu thông tin người dùng đang đăng nhập và các dữ liệu tạm thời khác trong toàn hệ thống.
 */
public class Session {
    private static User currentUser;
    private static Map<String, Object> bookingData;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
        clearBookingData();
    }

    public static Map<String, Object> getBookingData() {
        return bookingData;
    }

    public static void setBookingData(Map<String, Object> data) {
        bookingData = data;
    }

    public static void clearBookingData() {
        bookingData = null;
    }
}