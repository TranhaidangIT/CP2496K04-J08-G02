package utils;

import models.User;

/**
 * Lưu thông tin người dùng đang đăng nhập để dùng trong toàn hệ thống.
 */
public class Session {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }
}
