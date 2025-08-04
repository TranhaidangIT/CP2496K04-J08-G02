package configs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=cinema_management;encrypt=true;trustServerCertificate=true;",
                    "sa",
                    "sqladmin");
            return conn;
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println("Connection Failed! Check output console");
        }

        return conn;
    }
}

