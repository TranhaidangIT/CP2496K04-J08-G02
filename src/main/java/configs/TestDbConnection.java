package configs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDbConnection {
    public static void main(String[] args) {
        String url = "jdbc:mysql://sql12.freesqldatabase.com:3306/sql12793875?useSSL=false&serverTimezone=UTC";
        String user = "sql12793875";
        String pass = "3vZ24wMNpF";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Connect Successfully!");
        } catch (SQLException e) {
            System.err.println("Connect Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}