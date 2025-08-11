package dao;

import configs.DBConnection;
import java.util.Map;
import java.util.HashMap;
import models.Seat;
import models.SeatType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeatDAO {

    public static boolean insertSeatsForRoom(int roomId, int rows, int cols, int roomTypeId) {
        String seatInsertSQL = "INSERT INTO seats (roomId, seatRow, seatColumn, seatTypeId, isActive) VALUES (?, ?, ?, ?, ?)";
        Map<String, Integer> seatTypeMap = getSeatTypeMap(); // Get mapping from seat type names to IDs

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(seatInsertSQL)) {

            // Loop through each row
            for (int r = 0; r < rows; r++) {
                char seatRowChar = (char) ('A' + r); // Convert row number to letter (A, B, C...)

                String seatTypeKey = getSeatTypeForRow(r, rows, roomTypeId); // Get seat type for current row
                int seatTypeId = seatTypeMap.getOrDefault(seatTypeKey, seatTypeMap.get("Standard"));

                // Loop through each column
                for (int c = 1; c <= cols; c++) {
                    stmt.setInt(1, roomId);
                    stmt.setString(2, String.valueOf(seatRowChar));
                    stmt.setInt(3, c);
                    stmt.setInt(4, seatTypeId);
                    stmt.setBoolean(5, true); // default active
                    stmt.addBatch();
                }
            }

            stmt.executeBatch(); // Execute all inserts as a batch
            return true;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private static Map<String, Integer> getSeatTypeMap() {
        Map<String, Integer> seatTypeMap = new HashMap<>();
        String sql = "SELECT seatTypeId, seatTypeName FROM seatTypes";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("seatTypeId");
                String name = rs.getString("seatTypeName");
                seatTypeMap.put(name, id);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return seatTypeMap;
    }

    private static String getSeatTypeForRow(int r, int rows, int roomTypeId) {
        if (roomTypeId == 2) {
            return "Gold";
        } else {
            int standardRows = 3;
            int sweetboxRows = 1;
            int vipStart = standardRows;
            int vipEnd = rows - sweetboxRows - 1;

            if (r < standardRows) {
                return "Standard";
            } else if (r > vipEnd) {
                return "Sweetbox";
            } else {
                return "VIP";
            }
        }
    }

    public static List<Seat> getSeatsByRoomId(int roomId) {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT s.*, st.seatTypeId, st.seatTypeName, st.price " +
                "FROM seats s " +
                "JOIN seatTypes st ON s.seatTypeId = st.seatTypeId " +
                "WHERE s.roomId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int seatTypeId = rs.getInt("seatTypeId");
                String typeName = rs.getString("seatTypeName");
                double price = rs.getDouble("price");
                SeatType seatType = new SeatType(seatTypeId, typeName, price);

                int seatId = rs.getInt("seatId");
                char seatRow = rs.getString("seatRow").charAt(0);
                int seatColumn = rs.getInt("seatColumn");
                boolean isActive = rs.getBoolean("isActive");

                Seat seat = new Seat(seatId, roomId, seatRow, seatColumn, seatTypeId, isActive);
                seat.setSeatType(seatType);
                seat.setActive(isActive);

                seats.add(seat);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return seats;
    }

    //Delete old seat layout
    public static boolean deleteSeatsByRoomId(int roomId) {
        String sql = "DELETE FROM seats WHERE roomId = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    //Add new seat layout
    public static boolean updateSeatsInRoom(int roomId, List<Seat> seatsInRoom) {
        String sql = """
            UPDATE seats
            SET seatRow = ?, seatColumn = ?, seatTypeId = ?, isActive = ?
            WHERE seatId = ? AND roomId = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Seat seat : seatsInRoom) {
                pstmt.setString(1, String.valueOf(seat.getSeatRow()));
                pstmt.setInt(2, seat.getSeatColumn());
                pstmt.setInt(3, seat.getSeatTypeId());
                pstmt.setBoolean(4, seat.isActive());
                pstmt.setInt(5, seat.getSeatId());
                pstmt.setInt(6, roomId);
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            for (int result : results) {
                if (result == PreparedStatement.EXECUTE_FAILED) {
                    return false;
                }
            }

            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}
