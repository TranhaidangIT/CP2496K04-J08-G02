package dao;

import configs.DBConnection;
import models.Showtime;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ShowtimeDAO {

    public List<Showtime> getShowtimesByMovieRoomDate(int movieId, int roomId, LocalDate date) {
        List<Showtime> showtimes = new ArrayList<>();
        String sql = """
            SELECT s.showtimeId, s.movieId, s.roomId, s.showDate, s.showTime, s.endTime, s.createdAt,
                   m.title AS movieTitle, r.roomNumber AS roomName, m.poster
            FROM showtimes s
            JOIN movies m ON s.movieId = m.movieId
            JOIN screeningRooms r ON s.roomId = r.roomId
            WHERE s.movieId = ? AND s.roomId = ? AND s.showDate = ?
            ORDER BY s.showTime
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, movieId);
            ps.setInt(2, roomId);
            ps.setDate(3, Date.valueOf(date));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Showtime st = new Showtime();
                st.setShowtimeId(rs.getInt("showtimeId"));
                st.setMovieId(rs.getInt("movieId"));
                st.setRoomId(rs.getInt("roomId"));
                st.setShowDate(rs.getDate("showDate").toLocalDate());
                st.setShowTime(rs.getTime("showTime").toLocalTime());
                st.setEndTime(rs.getTime("endTime").toLocalTime());
                Timestamp ts = rs.getTimestamp("createdAt");
                if (ts != null) {
                    st.setCreatedAt(ts.toLocalDateTime());
                }
                st.setMovieTitle(rs.getString("movieTitle"));
                st.setRoomName(rs.getString("roomName"));
                st.setPoster(rs.getString("poster"));

                showtimes.add(st);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return showtimes;
    }

    public List<Showtime> getShowtimesByRoomDate(int roomId, LocalDate selectedDate) {
        List<Showtime> showtimes = new ArrayList<>();
        String sql = """
        SELECT s.showtimeId, s.movieId, s.roomId, s.showDate, s.showTime, s.endTime, s.createdAt,
               m.title AS movieTitle, r.roomNumber AS roomName, m.poster
        FROM showtimes s
        JOIN movies m ON s.movieId = m.movieId
        JOIN screeningRooms r ON s.roomId = r.roomId
        WHERE s.roomId = ? AND s.showDate = ?
        ORDER BY s.showTime
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            ps.setDate(2, Date.valueOf(selectedDate));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Showtime st = new Showtime();
                st.setShowtimeId(rs.getInt("showtimeId"));
                st.setMovieId(rs.getInt("movieId"));
                st.setRoomId(rs.getInt("roomId"));
                st.setShowDate(rs.getDate("showDate").toLocalDate());
                st.setShowTime(rs.getTime("showTime").toLocalTime());
                st.setEndTime(rs.getTime("endTime").toLocalTime());
                Timestamp ts = rs.getTimestamp("createdAt");
                if (ts != null) {
                    st.setCreatedAt(ts.toLocalDateTime());
                }
                st.setMovieTitle(rs.getString("movieTitle"));
                st.setRoomName(rs.getString("roomName"));
                st.setPoster(rs.getString("poster"));

                showtimes.add(st);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return showtimes;
    }


    public boolean insertShowtimes(List<Showtime> showtimesToInsert) {
        String sql = """
            INSERT INTO showtimes (movieId, roomId, showDate, showTime, endTime, createdAt)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (Showtime st : showtimesToInsert) {
                ps.setInt(1, st.getMovieId());
                ps.setInt(2, st.getRoomId());
                ps.setDate(3, Date.valueOf(st.getShowDate()));
                ps.setTime(4, Time.valueOf(st.getShowTime()));
                ps.setTime(5, Time.valueOf(st.getEndTime()));
                ps.setTimestamp(6, Timestamp.valueOf(st.getCreatedAt() != null ? st.getCreatedAt() : java.time.LocalDateTime.now()));
                ps.addBatch();
            }

            ps.executeBatch();
            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteShowtime(int showtimeId) {
        String sql = "DELETE FROM showtimes WHERE showtimeId = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, showtimeId);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateShowtime(Showtime showtime) {
        String sql = """
            UPDATE showtimes
            SET movieId = ?, roomId = ?, showDate = ?, showTime = ?, endTime = ?
            WHERE showtimeId = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, showtime.getMovieId());
            ps.setInt(2, showtime.getRoomId());
            ps.setDate(3, Date.valueOf(showtime.getShowDate()));
            ps.setTime(4, Time.valueOf(showtime.getShowTime()));
            ps.setTime(5, Time.valueOf(showtime.getEndTime()));
            ps.setInt(6, showtime.getShowtimeId());

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteShowtimesByMovieRoomDate(int movieId, int roomId, LocalDate date) {
        String sql = "DELETE FROM showtimes WHERE movieId = ? AND roomId = ? AND showDate = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, movieId);
            stmt.setInt(2, roomId);
            stmt.setDate(3, Date.valueOf(date));
            int deleted = stmt.executeUpdate();
            return deleted >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteShowtimesByRoomAndDate(int roomId, LocalDate currentDate) {
        String sql = "DELETE FROM showtimes WHERE roomId = ? AND showDate = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            ps.setDate(2, java.sql.Date.valueOf(currentDate));

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean deleteShowtimesByRoomDateMovie(int roomId, LocalDate date, int movieId) {
        String sql = "DELETE FROM showtimes WHERE roomId = ? AND showDate = ? AND movieId = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            ps.setInt(3, movieId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void insertShowtime(Showtime newShowtime) {
        String sql = """
        INSERT INTO showtimes (movieId, roomId, showDate, showTime, endTime, createdAt)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newShowtime.getMovieId());
            ps.setInt(2, newShowtime.getRoomId());
            ps.setDate(3, Date.valueOf(newShowtime.getShowDate()));
            ps.setTime(4, Time.valueOf(newShowtime.getShowTime()));
            ps.setTime(5, Time.valueOf(newShowtime.getEndTime()));
            ps.setTimestamp(6, Timestamp.valueOf(newShowtime.getCreatedAt() != null ? newShowtime.getCreatedAt() : java.time.LocalDateTime.now()));

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Showtime> getShowtimesByRoomAndDate(int roomId, LocalDate selectedDate) {
        List<Showtime> showtimes = new ArrayList<>();
        String sql = """
        SELECT s.showtimeId, s.movieId, s.roomId, s.showDate, s.showTime, s.endTime, s.createdAt,
               m.title AS movieTitle, r.roomNumber AS roomName, m.poster
        FROM showtimes s
        JOIN movies m ON s.movieId = m.movieId
        JOIN screeningRooms r ON s.roomId = r.roomId
        WHERE s.roomId = ? AND s.showDate = ?
        ORDER BY s.showTime
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            ps.setDate(2, Date.valueOf(selectedDate));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Showtime st = new Showtime();
                st.setShowtimeId(rs.getInt("showtimeId"));
                st.setMovieId(rs.getInt("movieId"));
                st.setRoomId(rs.getInt("roomId"));
                st.setShowDate(rs.getDate("showDate").toLocalDate());
                st.setShowTime(rs.getTime("showTime").toLocalTime());
                st.setEndTime(rs.getTime("endTime").toLocalTime());
                Timestamp ts = rs.getTimestamp("createdAt");
                if (ts != null) {
                    st.setCreatedAt(ts.toLocalDateTime());
                }
                st.setMovieTitle(rs.getString("movieTitle"));
                st.setRoomName(rs.getString("roomName"));

                showtimes.add(st);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return showtimes;
    }

}
