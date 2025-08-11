package configs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // MySQL connection parameters
    private static final String URL = "jdbc:mysql://sql12.freesqldatabase.com:3306/sql12793875?useSSL=false&serverTimezone=UTC";
    private static final String USER = "sql12793875";
    private static final String PASS = "3vZ24wMNpF";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish the connection
            conn = DriverManager.getConnection(URL, USER, PASS);
            return conn;

        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }

        return conn;
    }
}
