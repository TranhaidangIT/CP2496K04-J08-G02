package dao;

import configs.DBConnection;
import models.Movie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovieDAO {

    /**
     * Retrieve all movies from the database, ordered by creation time (newest first).
     * @return List of Movie objects
     */
    public static List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies ORDER BY createdAt DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Loop through the result set and build movie objects
            while (rs.next()) {
                Movie movie = new Movie();
                movie.setMovieId(rs.getInt("movieId"));
                movie.setTitle(rs.getString("title"));
                movie.setDuration(rs.getInt("duration"));
                movie.setGenre(rs.getString("genre"));
                movie.setDescription(rs.getString("description"));
                movie.setDirectedBy(rs.getString("directedBy"));
                movie.setLanguage(rs.getString("language"));
                movie.setPoster(rs.getString("poster"));
                movie.setAgeRating(rs.getString("ageRating"));
                movie.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
                movie.setReleasedDate(rs.getDate("releaseDate").toLocalDate());

                movies.add(movie);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return movies;
    }

    /**
     * Insert a new movie into the database.
     * @param movie the movie object to insert
     * @return true if insert was successful, false otherwise
     */
    public static boolean insertMovie(Movie movie) {
        String sql = "INSERT INTO movies (title, duration, genre, description, directedBy, language, poster, ageRating, releaseDate, createdAt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, movie.getTitle());
            stmt.setInt(2, movie.getDuration());
            stmt.setString(3, movie.getGenre());
            stmt.setString(4, movie.getDescription());
            stmt.setString(5, movie.getDirectedBy());
            stmt.setString(6, movie.getLanguage());
            stmt.setString(7, movie.getPoster());
            stmt.setString(8, movie.getAgeRating());
            stmt.setDate(9, Date.valueOf(movie.getReleasedDate()));
            stmt.setTimestamp(10, Timestamp.valueOf(movie.getCreatedAt()));

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    /**
     * Check whether a movie with the given ID exists in the database.
     * @param movieId the movie ID to check
     * @return true if exists, false otherwise
     */
    public static boolean isMovieIdExists(int movieId) {
        String sql = "SELECT 1 FROM movies WHERE movieId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Returns true if a record exists
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    /**
     * Update an existing movie in the database.
     * @param movie the updated movie object
     * @return true if update was successful, false otherwise
     */
    public static boolean updateMovie(Movie movie) {
        String sql = "UPDATE movies SET title = ?, duration = ?, genre = ?, description = ?, directedBy = ?, language = ?, poster = ?, ageRating = ?, releaseDate = ? " +
                "WHERE movieId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, movie.getTitle());
            stmt.setInt(2, movie.getDuration());
            stmt.setString(3, movie.getGenre());
            stmt.setString(4, movie.getDescription());
            stmt.setString(5, movie.getDirectedBy());
            stmt.setString(6, movie.getLanguage());
            stmt.setString(7, movie.getPoster());
            stmt.setString(8, movie.getAgeRating());
            stmt.setDate(9, Date.valueOf(movie.getReleasedDate()));
            stmt.setInt(10, movie.getMovieId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    /**
     * Delete a movie from the database by its ID.
     * @param movieId the ID of the movie to delete
     * @return true if delete was successful, false otherwise
     */
    public static boolean deleteMovieById(int movieId) {
        String sql = "DELETE FROM movies WHERE movieId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }
}
