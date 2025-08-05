package dao;

import models.Showtime;
import configs.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ShowtimeDAO {

    public static boolean deleteShowtime(int showtimeId) {
        return false;
    }

    public static boolean updateShowtime(Showtime selectedShowtime) {
        return false;
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

        } catch (Exception e) {
            e.printStackTrace();
        }

        return showtimes;
    }

    public static boolean insertShowtime(int movieId, int roomId, LocalDate showDate, LocalTime showTime, LocalTime endTime) {
        String sql = "INSERT INTO showtimes (movieId, roomId, showDate, showTime, endTime, createdAt) " +
                "VALUES (?, ?, ?, ?, ?, GETDATE())";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);
            stmt.setInt(2, roomId);
            stmt.setDate(3, java.sql.Date.valueOf(showDate));
            stmt.setTime(4, java.sql.Time.valueOf(showTime));
            stmt.setTime(5, java.sql.Time.valueOf(endTime));

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void deleteShowtimeById(int showtimeId) {
        String sql = "DELETE FROM showtimes WHERE showtimeId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, showtimeId);
            stmt.executeUpdate();

            System.out.println("Deleted showtime with ID: " + showtimeId);

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to delete showtime with ID: " + showtimeId);
        }
    }

}
