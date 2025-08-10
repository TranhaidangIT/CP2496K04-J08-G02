package dao;

import configs.DBConnection;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import models.ScreeningRoom;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScreeningRoomDAO {

    public static List<ScreeningRoom> getAllRooms() {
        List<ScreeningRoom> roomList = new ArrayList<>();
        String sql = """
            SELECT sr.*, rt.typeName
            FROM screeningRooms sr
            JOIN roomTypes rt ON sr.roomTypeId = rt.roomTypeId
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ScreeningRoom room = new ScreeningRoom();
                room.setRoomId(rs.getInt("roomId"));
                room.setRoomNumber(rs.getString("roomNumber"));
                room.setRoomTypeId(rs.getInt("roomTypeId"));
                room.setSeatingLayout(rs.getString("seatingLayout"));
                room.setTotalCapacity(rs.getInt("totalCapacity"));
                room.setEquipment(rs.getString("equipment"));
                room.setRoomStatus(rs.getString("roomStatus"));
                room.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
                room.setTypeName(rs.getString("typeName"));
                roomList.add(room);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roomList;
    }

    public static boolean isRoomNumberExists(String roomNumber) {
        String sql = "SELECT 1 FROM screeningRooms WHERE roomNumber = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomNumber);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true nếu tồn tại
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean insertRoom(ScreeningRoom room) {
        String sql = """
            INSERT INTO screeningRooms (roomNumber, roomTypeId, seatingLayout, totalCapacity, equipment, roomStatus, createdAt)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, room.getRoomNumber());
            stmt.setInt(2, room.getRoomTypeId());
            stmt.setString(3, room.getSeatingLayout());
            stmt.setInt(4, room.getTotalCapacity());
            stmt.setString(5, room.getEquipment());
            stmt.setString(6, room.getRoomStatus());
            stmt.setTimestamp(7, Timestamp.valueOf(room.getCreatedAt()));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getRoomIdByRoomNumber(String roomNumber) {
        String sql = "SELECT roomId FROM screeningRooms WHERE roomNumber = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("roomId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean deleteRoomById(int roomId) {
        String deleteSeatsSQL = "DELETE FROM seats WHERE roomId = ?";
        String deleteRoomSQL = "DELETE FROM screeningRooms WHERE roomId = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (
                    PreparedStatement deleteSeatsStmt = conn.prepareStatement(deleteSeatsSQL);
                    PreparedStatement deleteRoomStmt = conn.prepareStatement(deleteRoomSQL)
            ) {
                // Delete seats first
                deleteSeatsStmt.setInt(1, roomId);
                deleteSeatsStmt.executeUpdate();

                // Delete room
                deleteRoomStmt.setInt(1, roomId);
                int deletedRooms = deleteRoomStmt.executeUpdate();

                conn.commit();
                return deletedRooms > 0;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }




    public static boolean updateRoom(ScreeningRoom currentRoom) {
        String sql = """
        UPDATE screeningRooms
        SET roomNumber = ?, 
            roomTypeId = ?, 
            seatingLayout = ?, 
            totalCapacity = ?, 
            equipment = ?, 
            roomStatus = ?, 
            createdAt = ?
        WHERE roomId = ?
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, currentRoom.getRoomNumber());
            stmt.setInt(2, currentRoom.getRoomTypeId());
            stmt.setString(3, currentRoom.getSeatingLayout());
            stmt.setInt(4, currentRoom.getTotalCapacity());
            stmt.setString(5, currentRoom.getEquipment());
            stmt.setString(6, currentRoom.getRoomStatus());
            stmt.setTimestamp(7, Timestamp.valueOf(currentRoom.getCreatedAt()));
            stmt.setInt(8, currentRoom.getRoomId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void updateRoomCapacity(int roomId, int newCapacity) {
        String sql = "UPDATE screeningRooms SET totalCapacity = ? WHERE roomId = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newCapacity);
            stmt.setInt(2, roomId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static ScreeningRoom getRoomByName(String roomName) {
        String sql = """
        SELECT sr.*, rt.typeName
        FROM screeningRooms sr
        JOIN roomTypes rt ON sr.roomTypeId = rt.roomTypeId
        WHERE sr.roomNumber = ?
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ScreeningRoom room = new ScreeningRoom();
                    room.setRoomId(rs.getInt("roomId"));
                    room.setRoomNumber(rs.getString("roomNumber"));
                    room.setRoomTypeId(rs.getInt("roomTypeId"));
                    room.setSeatingLayout(rs.getString("seatingLayout"));
                    room.setTotalCapacity(rs.getInt("totalCapacity"));
                    room.setEquipment(rs.getString("equipment"));
                    room.setRoomStatus(rs.getString("roomStatus"));
                    room.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
                    room.setTypeName(rs.getString("typeName"));
                    return room;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ScreeningRoom getRoomById(int roomId) {
        String sql = """
        SELECT sr.*, rt.typeName
        FROM screeningRooms sr
        JOIN roomTypes rt ON sr.roomTypeId = rt.roomTypeId
        WHERE sr.roomId = ?
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ScreeningRoom room = new ScreeningRoom();
                    room.setRoomId(rs.getInt("roomId"));
                    room.setRoomNumber(rs.getString("roomNumber"));
                    room.setRoomTypeId(rs.getInt("roomTypeId"));
                    room.setSeatingLayout(rs.getString("seatingLayout"));
                    room.setTotalCapacity(rs.getInt("totalCapacity"));
                    room.setEquipment(rs.getString("equipment"));
                    room.setRoomStatus(rs.getString("roomStatus"));
                    room.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
                    room.setTypeName(rs.getString("typeName"));
                    return room;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
