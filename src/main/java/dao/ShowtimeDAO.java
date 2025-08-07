package dao;

import configs.DBConnection;
import models.Showtime;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ShowtimeDAO {

    public static boolean deleteShowtime(int showtimeId) {
        String sql = "DELETE FROM showtimes WHERE showtimeId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, showtimeId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error while deleting showtime: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateShowtime(Showtime showtime) {
        String sql = "UPDATE showtimes SET movieId = ?, roomId = ?, showDate = ?, showTime = ?, endTime = ? " +
                "WHERE showtimeId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, showtime.getMovieId());
            stmt.setInt(2, showtime.getRoomId());
            stmt.setDate(3, Date.valueOf(showtime.getShowDate()));
            stmt.setTime(4, Time.valueOf(showtime.getShowTime()));
            stmt.setTime(5, Time.valueOf(showtime.getEndTime()));
            stmt.setInt(6, showtime.getShowtimeId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error while updating showtime: " + e.getMessage());
            return false;
        }
    }


    public static List<Showtime> getAllShowtimes() {
        List<Showtime> showtimes = new ArrayList<>();

        String query = """
            SELECT 
                s.showtimeId,
                s.movieId,
                s.roomId,
                s.showDate,
                s.showTime,
                s.endTime,
                s.createdAt,
                m.title AS movieTitle,
                r.roomNumber AS roomName
            FROM showtimes s
            JOIN movies m ON s.movieId = m.movieId
            JOIN screeningRooms r ON s.roomId = r.roomId
            ORDER BY s.showDate, s.showTime;
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Showtime showtime = new Showtime();
                showtime.setShowtimeId(rs.getInt("showtimeId"));
                showtime.setMovieId(rs.getInt("movieId"));
                showtime.setRoomId(rs.getInt("roomId"));
                showtime.setShowDate(rs.getDate("showDate").toLocalDate());
                showtime.setShowTime(rs.getTime("showTime").toLocalTime());
                showtime.setEndTime(rs.getTime("endTime").toLocalTime());
                showtime.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
                showtime.setMovieTitle(rs.getString("movieTitle"));
                showtime.setRoomName(rs.getString("roomName"));
                showtimes.add(showtime);
            }

        } catch (SQLException e) {
            System.err.println("Error while fetching showtimes: " + e.getMessage());
        }

        return showtimes;
    }

    public static boolean insertShowtime(int movieId, int roomId, LocalDate showDate, LocalTime showTime, LocalTime endTime) {
        String sql = """
            INSERT INTO showtimes (movieId, roomId, showDate, showTime, endTime, createdAt)
            VALUES (?, ?, ?, ?, ?, GETDATE())
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);
            stmt.setInt(2, roomId);
            stmt.setDate(3, Date.valueOf(showDate));
            stmt.setTime(4, Time.valueOf(showTime));
            stmt.setTime(5, Time.valueOf(endTime));

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error while inserting showtime: " + e.getMessage());
            return false;
        }
    }


    public static boolean deleteShowtimeById(int showtimeId) {
        String deleteTicketSeats = "DELETE FROM ticketSeats WHERE ticketId IN (SELECT ticketId FROM tickets WHERE showtimeId = ?)";
        String deleteTickets = "DELETE FROM tickets WHERE showtimeId = ?";
        String deleteShowtime = "DELETE FROM showtimes WHERE showtimeId = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt1 = conn.prepareStatement(deleteTicketSeats);
                 PreparedStatement stmt2 = conn.prepareStatement(deleteTickets);
                 PreparedStatement stmt3 = conn.prepareStatement(deleteShowtime)) {

                stmt1.setInt(1, showtimeId);
                stmt1.executeUpdate();

                stmt2.setInt(1, showtimeId);
                stmt2.executeUpdate();

                stmt3.setInt(1, showtimeId);
                stmt3.executeUpdate();

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Transaction failed: " + e.getMessage());
                return false;
            }

        } catch (SQLException ex) {
            System.err.println("DB connection error: " + ex.getMessage());
            return false;
        }
    }

}
