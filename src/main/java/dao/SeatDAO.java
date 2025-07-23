package dao;

import configs.DBConnection;
import models.Seat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeatDAO {

    public static List<Seat> getSeatsByRoomId(int roomId) {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT * FROM seats WHERE roomId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Seat seat = new Seat(
                        rs.getInt("seatId"),
                        rs.getInt("roomId"),
                        rs.getInt("seatRow"),
                        rs.getInt("seatColumn"),
                        rs.getString("seatType"),
                        rs.getString("isActive")
                );
                seats.add(seat);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return seats;
    }

    public static boolean updateSeat(Seat seat) {
        String sql = "UPDATE seats SET seatRow = ?, seatColumn = ?, seatType = ?, isActive = ? WHERE seatId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, seat.getSeatRow());
            stmt.setInt(2, seat.getSeatColumn());
            stmt.setString(3, seat.getSeatType());
            stmt.setString(4, seat.getIsActive());
            stmt.setInt(5, seat.getSeatId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteSeatsByRoomId(int roomId) {
        String sql = "DELETE FROM seats WHERE roomId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void deleteSeatsByRoomNumber(String roomNumber) {
    }
}
