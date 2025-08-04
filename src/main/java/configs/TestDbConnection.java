package configs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDbConnection {

    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=cinema_management;encrypt=true;trustServerCertificate=true;";
        String user = "sa";
        String password = "sqladmin";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            System.out.println("Kết nối thành công!");
        } catch (SQLException e) {
            System.err.println("Kết nối thất bại!");
            e.printStackTrace();
        }
    }
}