package dao;

import configs.DBConnection;
import models.ScreeningRoom;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScreeningRoomDAO {

    public static List<ScreeningRoom> getAllRooms() {
        List<ScreeningRoom> roomList = new ArrayList<>();
        String sql = "SELECT * FROM screeningRooms ORDER BY createdAt DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ScreeningRoom room = new ScreeningRoom(
                        rs.getInt("roomId"),
                        rs.getString("roomNumber"),
                        rs.getString("roomType"),
                        rs.getString("roomStatus"),
                        rs.getString("seatingLayout"),
                        rs.getInt("totalCapacity"),
                        rs.getString("equipment"),
                        rs.getTimestamp("createdAt").toLocalDateTime()
                );
                roomList.add(room);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return roomList;
    }

    public static boolean isRoomNumberExists(String roomNumber) {
        String sql = "SELECT 1 FROM screeningRooms WHERE LOWER(roomNumber) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, roomNumber);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking room number exists: " + e.getMessage());
            return false;
        }
    }

    public static boolean insertRoom(ScreeningRoom room) {
        String sql = "INSERT INTO screeningRooms (roomNumber, roomType, roomStatus, seatLayout, totalCapacity, equipment, createdAt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, room.getRoomNumber());
            stmt.setString(2, room.getRoomType());
            stmt.setString(3, room.getRoomStatus());
            stmt.setString(4, room.getSeatingLayout());
            stmt.setInt(5, room.getTotalCapacity());
            stmt.setString(6, room.getEquipment());
            stmt.setTimestamp(7, Timestamp.valueOf(room.getCreatedAt()));

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error inserting room: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateRoom(ScreeningRoom room) {
        String sql = "UPDATE screeningRooms SET roomNumber = ?, roomType = ?, roomStatus = ?, " +
                "seatingLayout = ?, totalCapacity = ?, equipment = ? WHERE roomId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, room.getRoomNumber());
            stmt.setString(2, room.getRoomType());
            stmt.setString(3, room.getRoomStatus());
            stmt.setString(4, room.getSeatingLayout());
            stmt.setInt(5, room.getTotalCapacity());
            stmt.setString(6, room.getEquipment());
            stmt.setInt(7, room.getRoomId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean deleteRoom(int roomId) {
        String sql = "DELETE FROM screeningRooms WHERE roomId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static ScreeningRoom getRoomById(int roomId) {
        String sql = "SELECT * FROM screeningRooms WHERE roomId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new ScreeningRoom(
                        rs.getInt("roomId"),
                        rs.getString("roomNumber"),
                        rs.getString("roomType"),
                        rs.getString("roomStatus"),
                        rs.getString("seatingLayout"),
                        rs.getInt("totalCapacity"),
                        rs.getString("equipment"),
                        rs.getTimestamp("createdAt").toLocalDateTime()
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
