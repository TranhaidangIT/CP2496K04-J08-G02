package dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import configs.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LookupDAO {

    // Genre
    public static ObservableList<String> getAllGenres() {
        ObservableList<String> genres = FXCollections.observableArrayList();
        String sql = "SELECT genreName FROM genres ORDER BY genreName";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                genres.add(rs.getString("genreName"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return genres;
    }

    public static int getGenreIdByName(String genreName) {
        String sql = "SELECT genreId FROM genres WHERE genreName = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, genreName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("genreId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Language
    public static ObservableList<String> getAllLanguages() {
        ObservableList<String> languages = FXCollections.observableArrayList();
        String sql = "SELECT languageName FROM languages ORDER BY languageName";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                languages.add(rs.getString("languageName"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return languages;
    }

    public static int getLanguageIdByName(String languageName) {
        String sql = "SELECT languageId FROM languages WHERE languageName = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, languageName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("languageId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Age Rating
    public static ObservableList<String> getAllAgeRatings() {
        ObservableList<String> ageRatings = FXCollections.observableArrayList();
        String sql = "SELECT ratingCode FROM ageRatings ORDER BY ratingCode";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ageRatings.add(rs.getString("ratingCode"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ageRatings;
    }

    public static String getAgeRatingDescription(String ratingCode) {
        String sql = "SELECT description FROM ageRatings WHERE ratingCode = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ratingCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("description");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
}
