package utils;

public class TestPasswordHash {
    public static void main(String[] args) {
        // Mật khẩu  muốn mã hóa
        String plainPassword = "123";

        // Gọi hàm mã hóa từ PasswordUtil
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);

        // In ra chuỗi hash để bạn copy vào CSDL
        System.out.println("Mật khẩu gốc: " + plainPassword);
        System.out.println("Mã hóa BCrypt: " + hashedPassword);
    }
}
// chạy xong copy dán đoạn mã hóa vào inset databse , hiện chưa làm tưj dộng