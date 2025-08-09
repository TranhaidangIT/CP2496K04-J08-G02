package dao;

import configs.DBConnection;
import models.Locker;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LockerDAO {

    public static List<Locker> getAllLockers() {
        List<Locker> lockers = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT lockerId, lockerNumber, locationInfo, status FROM lockers ORDER BY lockerNumber";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Locker locker = new Locker(
                        rs.getInt("lockerId"),
                        rs.getString("lockerNumber"),
                        rs.getString("locationInfo"),
                        rs.getString("status")
                );
                lockers.add(locker);
            }
        } catch (SQLException e) {
            System.err.println("Error loading lockers: " + e.getMessage());
        }
        return lockers;
    }

    public static List<Locker> getLockersByStatus(String status) {
        List<Locker> lockers = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT lockerId, lockerNumber, locationInfo, status FROM lockers WHERE status = ? ORDER BY lockerNumber";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Locker locker = new Locker(
                        rs.getInt("lockerId"),
                        rs.getString("lockerNumber"),
                        rs.getString("locationInfo"),
                        rs.getString("status")
                );
                lockers.add(locker);
            }
        } catch (SQLException e) {
            System.err.println("Error loading lockers by status: " + e.getMessage());
        }
        return lockers;
    }

    // NEW: Get locker assignment information (PIN and item description)
    public static Map<String, String> getLockerAssignmentInfo(int lockerId) {
        Map<String, String> assignmentInfo = new HashMap<>();
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT pinCode, itemDescription, ticketCode, assignedAt " +
                    "FROM lockerAssignments " +
                    "WHERE lockerId = ? AND releasedAt IS NULL";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, lockerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                assignmentInfo.put("pinCode", rs.getString("pinCode"));
                assignmentInfo.put("itemDescription", rs.getString("itemDescription"));
                assignmentInfo.put("ticketCode", rs.getString("ticketCode"));
                assignmentInfo.put("assignedAt", rs.getTimestamp("assignedAt").toString());
            }
        } catch (SQLException e) {
            System.err.println("Error loading locker assignment info: " + e.getMessage());
        }
        return assignmentInfo;
    }

    // NEW: Get all lockers with their assignment information
    public static List<Map<String, Object>> getAllLockersWithAssignments() {
        List<Map<String, Object>> lockersWithInfo = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT l.lockerId, l.lockerNumber, l.locationInfo, l.status, " +
                    "la.pinCode, la.itemDescription, la.ticketCode, la.assignedAt " +
                    "FROM lockers l " +
                    "LEFT JOIN lockerAssignments la ON l.lockerId = la.lockerId AND la.releasedAt IS NULL " +
                    "ORDER BY l.lockerNumber";

            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> lockerInfo = new HashMap<>();

                // Locker basic info
                lockerInfo.put("lockerId", rs.getInt("lockerId"));
                lockerInfo.put("lockerNumber", rs.getString("lockerNumber"));
                lockerInfo.put("locationInfo", rs.getString("locationInfo"));
                lockerInfo.put("status", rs.getString("status"));

                // Assignment info (may be null for available lockers)
                lockerInfo.put("pinCode", rs.getString("pinCode"));
                lockerInfo.put("itemDescription", rs.getString("itemDescription"));
                lockerInfo.put("ticketCode", rs.getString("ticketCode"));
                lockerInfo.put("assignedAt", rs.getTimestamp("assignedAt"));

                lockersWithInfo.add(lockerInfo);
            }
        } catch (SQLException e) {
            System.err.println("Error loading lockers with assignments: " + e.getMessage());
        }
        return lockersWithInfo;
    }

    public static boolean insertLocker(Locker locker) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO lockers (lockerNumber, locationInfo, status) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, locker.getLockerNumber());
            stmt.setString(2, locker.getLocationInfo());
            stmt.setString(3, locker.getStatus());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting locker: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateLocker(Locker locker) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "UPDATE lockers SET lockerNumber = ?, locationInfo = ?, status = ? WHERE lockerId = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, locker.getLockerNumber());
            stmt.setString(2, locker.getLocationInfo());
            stmt.setString(3, locker.getStatus());
            stmt.setInt(4, locker.getLockerId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating locker: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteLockerById(int lockerId) {
        try (Connection conn = DBConnection.getConnection()) {
            // Check if locker has any assignments first
            String checkQuery = "SELECT COUNT(*) FROM lockerAssignments WHERE lockerId = ? AND releasedAt IS NULL";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, lockerId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                System.err.println("Cannot delete locker: has active assignments");
                return false;
            }

            String query = "DELETE FROM lockers WHERE lockerId = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, lockerId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting locker: " + e.getMessage());
            return false;
        }
    }

    public static boolean isLockerNumberExists(String lockerNumber) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT COUNT(*) FROM lockers WHERE lockerNumber = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, lockerNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking locker number: " + e.getMessage());
        }
        return false;
    }

    public static Locker getLockerById(int lockerId) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT lockerId, lockerNumber, locationInfo, status FROM lockers WHERE lockerId = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, lockerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Locker(
                        rs.getInt("lockerId"),
                        rs.getString("lockerNumber"),
                        rs.getString("locationInfo"),
                        rs.getString("status")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting locker by ID: " + e.getMessage());
        }
        return null;
    }
}