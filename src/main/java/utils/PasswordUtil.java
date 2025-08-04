package utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Hỗ trợ mã hóa và kiểm tra mật khẩu bằng BCrypt.
 */
public class PasswordUtil {
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
