package dao;

import configs.DBConnection;
import models.Ticket;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketDAO {

    public List<Ticket> getAllPaidTickets() throws SQLException {
        List<Ticket> tickets = new ArrayList<>();
        String query = "SELECT t.ticketId, t.totalPrice, " +
                "m.title AS movieTitle, " +
                "u.username AS customerUsername, " +
                "GROUP_CONCAT(CONCAT(s.seatRow, s.seatColumn) SEPARATOR ', ') AS seatInfo, " +
                "sh.showDate, sh.showTime, t.soldAt " +
                "FROM tickets t " +
                "JOIN showtimes sh ON t.showtimeId = sh.showtimeId " +
                "JOIN movies m ON sh.movieId = m.movieId " +
                "JOIN ticketSeats ts ON t.ticketId = ts.ticketId " +
                "JOIN seats s ON ts.seatId = s.seatId " +
                "JOIN Users u ON t.soldBy = u.userId " +
                "GROUP BY t.ticketId, t.totalPrice, m.title, u.username, sh.showDate, sh.showTime, t.soldAt " +
                "ORDER BY t.ticketId DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Ticket ticket = new Ticket();
                ticket.setTicketId(rs.getInt("ticketId"));
                ticket.setMovieTitle(rs.getString("movieTitle"));
                ticket.setCustomerUsername(rs.getString("customerUsername"));
                ticket.setSeatInfo(rs.getString("seatInfo"));
                ticket.setShowtimeInfo(rs.getString("showDate") + " " + rs.getString("showTime"));
                ticket.setTotalPrice(rs.getDouble("totalPrice") != 0 ? rs.getDouble("totalPrice") : 0.0); // Handle NULL
                tickets.add(ticket);
            }
        }
        return tickets;
    }

    public List<Map<String, Object>> getTicketStatistics(Date startDate, Date endDate) throws SQLException {
        List<Map<String, Object>> stats = new ArrayList<>();
        String query = "SELECT m.title AS movieTitle, COUNT(t.ticketId) AS ticketCount " +
                "FROM tickets t " +
                "JOIN showtimes sh ON t.showtimeId = sh.showtimeId " +
                "JOIN movies m ON sh.movieId = m.movieId " +
                "WHERE sh.showDate BETWEEN ? AND ? " +
                "GROUP BY m.title " +
                "ORDER BY ticketCount DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("movieTitle", rs.getString("movieTitle"));
                    stat.put("ticketCount", rs.getLong("ticketCount"));
                    stats.add(stat);
                }
            }
        }
        return stats;
    }

    public List<Map<String, Object>> getMostBookedMovies() throws SQLException {
        List<Map<String, Object>> mostBookedMovies = new ArrayList<>();
        String query = "SELECT m.title AS movieTitle, COUNT(t.ticketId) AS ticketCount " +
                "FROM tickets t " +
                "JOIN showtimes sh ON t.showtimeId = sh.showtimeId " +
                "JOIN movies m ON sh.movieId = m.movieId " +
                "GROUP BY m.title " +
                "ORDER BY ticketCount DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> movieStat = new HashMap<>();
                movieStat.put("movieTitle", rs.getString("movieTitle"));
                movieStat.put("ticketCount", rs.getLong("ticketCount"));
                mostBookedMovies.add(movieStat);
            }
        }
        return mostBookedMovies;
    }

    public List<Map<String, Object>> getMostPopularShowtimes() throws SQLException {
        List<Map<String, Object>> mostPopularShowtimes = new ArrayList<>();
        String query = "SELECT sh.showTime AS showtime, COUNT(t.ticketId) AS ticketCount " +
                "FROM tickets t " +
                "JOIN showtimes sh ON t.showtimeId = sh.showtimeId " +
                "GROUP BY sh.showTime " +
                "ORDER BY ticketCount DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> showtimeStat = new HashMap<>();
                showtimeStat.put("showtime", rs.getString("showtime"));
                showtimeStat.put("ticketCount", rs.getLong("ticketCount"));
                mostPopularShowtimes.add(showtimeStat);
            }
        }
        return mostPopularShowtimes;
    }

    public int getTotalTicketsCount() throws SQLException {
        String query = "SELECT COUNT(*) AS totalTickets FROM tickets";
        int count = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt("totalTickets");
            }
        } catch (SQLException e) {
            System.err.println("Error getting total tickets count: " + e.getMessage());
            throw e; // Re-throw to handle upstream
        }
        return count;
    }

    public double getTotalRevenueToday() throws SQLException {
        String query = "SELECT SUM(totalPrice) AS totalRevenue FROM tickets WHERE DATE(soldAt) = CURDATE()";
        double revenue = 0.0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                revenue = rs.getDouble("totalRevenue") != 0 ? rs.getDouble("totalRevenue") : 0.0; // Handle NULL
            }
        } catch (SQLException e) {
            System.err.println("Error getting total revenue today: " + e.getMessage());
            throw e;
        }
        return revenue;
    }

    public List<Map<String, Object>> getDailyRevenueForLast7Days() throws SQLException {
        List<Map<String, Object>> dailyRevenue = new ArrayList<>();
        String query = "SELECT DATE(soldAt) AS day, SUM(totalPrice) AS revenue " +
                "FROM tickets " +
                "WHERE soldAt >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                "GROUP BY DATE(soldAt) " +
                "ORDER BY day ASC";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // Updated for clarity

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> data = new HashMap<>();
                LocalDate date = rs.getDate("day").toLocalDate();
                data.put("day", date.format(formatter));
                data.put("revenue", rs.getDouble("revenue") != 0 ? rs.getDouble("revenue") : 0.0); // Handle NULL
                dailyRevenue.add(data);
            }
        } catch (SQLException e) {
            System.err.println("Error getting daily revenue for last 7 days: " + e.getMessage());
            throw e;
        }
        return dailyRevenue;
    }
}