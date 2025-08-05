package dao;

import configs.DBConnection;
import models.RoomType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomTypeDAO {

    public static List<RoomType> getAllRoomTypes() {
        List<RoomType> list = new ArrayList<>();
        String sql = "SELECT roomTypeId, typeName, description, maxRows, maxColumns FROM roomTypes";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new RoomType(
                        rs.getInt("roomTypeId"),
                        rs.getString("typeName"),
                        rs.getString("description"),
                        rs.getInt("maxRows"),
                        rs.getInt("maxColumns")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String getRoomTypeNameById(int roomTypeId) {
        String sql = "SELECT typeName FROM roomTypes WHERE roomTypeId = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomTypeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("typeName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static RoomType getRoomTypeById(int roomTypeId) {
        RoomType roomType = null;
        String sql = "SELECT roomTypeId, typeName, description, maxRows, maxColumns FROM roomTypes WHERE roomTypeId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomTypeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                roomType = new RoomType(
                        rs.getInt("roomTypeId"),
                        rs.getString("typeName"),
                        rs.getString("description"),
                        rs.getInt("maxRows"),
                        rs.getInt("maxColumns")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return roomType;
    }
}
