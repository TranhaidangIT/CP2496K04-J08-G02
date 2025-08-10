package dao;

import models.Service;
import models.ServiceCategory;
import configs.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    // Get all services with category names and image
    public static List<Service> getAllServices() {
        List<Service> services = new ArrayList<>();
        String sql = """
            SELECT s.serviceId, s.serviceName, s.price, s.categoryId, s.img, s.createdAt, sc.categoryName
            FROM services s
            INNER JOIN serviceCategories sc ON s.categoryId = sc.categoryId
            ORDER BY s.serviceName
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Service service = new Service(
                        rs.getInt("serviceId"),
                        rs.getString("serviceName"),
                        rs.getBigDecimal("price"),
                        0, // quantity mặc định
                        rs.getInt("categoryId"),
                        rs.getString("categoryName"),
                        rs.getString("img") != null ? rs.getString("img").trim() : "",
                        rs.getTimestamp("createdAt").toLocalDateTime()
                );
                services.add(service);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching all services: " + e.getMessage());
            e.printStackTrace();
        }

        return services;
    }

    // Get services by category
    public static List<Service> getServicesByCategory(int categoryId) {
        List<Service> services = new ArrayList<>();
        String sql = """
            SELECT s.serviceId, s.serviceName, s.price, s.categoryId, s.img, s.createdAt, sc.categoryName
            FROM services s
            INNER JOIN serviceCategories sc ON s.categoryId = sc.categoryId
            WHERE s.categoryId = ?
            ORDER BY s.serviceName
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Service service = new Service(
                            rs.getInt("serviceId"),
                            rs.getString("serviceName"),
                            rs.getBigDecimal("price"),
                            0, // quantity mặc định
                            rs.getInt("categoryId"),
                            rs.getString("categoryName"),
                            rs.getString("img") != null ? rs.getString("img").trim() : "",
                            rs.getTimestamp("createdAt").toLocalDateTime()
                    );
                    services.add(service);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching services by category: " + e.getMessage());
            e.printStackTrace();
        }

        return services;
    }

    // Insert new service (with image)
    public static boolean insertService(Service service) {
        String sql = "INSERT INTO services (serviceName, price, categoryId, img) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, service.getServiceName());
            stmt.setBigDecimal(2, service.getPrice());
            stmt.setInt(3, service.getCategoryId());
            stmt.setString(4, service.getImg());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error inserting service: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Update service (with image)
    public static boolean updateService(Service service) {
        String sql = "UPDATE services SET serviceName = ?, price = ?, categoryId = ?, img = ? WHERE serviceId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, service.getServiceName());
            stmt.setBigDecimal(2, service.getPrice());
            stmt.setInt(3, service.getCategoryId());
            stmt.setString(4, service.getImg());
            stmt.setInt(5, service.getServiceId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating service: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Delete service
    public static boolean deleteServiceById(int serviceId) {
        String sql = "DELETE FROM services WHERE serviceId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, serviceId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting service: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Check if service ID exists
    public static boolean isServiceIdExists(int serviceId) {
        String sql = "SELECT COUNT(*) FROM services WHERE serviceId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, serviceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error checking service ID existence: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Get all service categories
    public static List<ServiceCategory> getAllServiceCategories() {
        List<ServiceCategory> categories = new ArrayList<>();
        String sql = "SELECT categoryId, categoryName FROM serviceCategories ORDER BY categoryName";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ServiceCategory category = new ServiceCategory(
                        rs.getInt("categoryId"),
                        rs.getString("categoryName")
                );
                categories.add(category);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching service categories: " + e.getMessage());
            e.printStackTrace();
        }

        return categories;
    }

    // Insert default categories if they don't exist
    public static void initializeDefaultCategories() {
        try (Connection conn = DBConnection.getConnection()) {

            String checkSql = "SELECT COUNT(*) FROM serviceCategories WHERE categoryName IN ('Drinks', 'Fast Food')";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                 ResultSet rs = checkStmt.executeQuery()) {

                if (rs.next() && rs.getInt(1) < 2) {
                    String insertSql = "INSERT INTO serviceCategories (categoryName) VALUES (?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

                        insertStmt.setString(1, "Drinks");
                        insertStmt.executeUpdate();

                        insertStmt.setString(1, "Fast Food");
                        insertStmt.executeUpdate();

                        System.out.println("Default service categories initialized.");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error initializing default categories: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
