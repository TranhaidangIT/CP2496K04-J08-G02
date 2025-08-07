package models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Movie {
    private int movieId;
    private String title;
    private int duration;
    private String genre;
    private String description;
    private String directedBy;
    private String language;
    private String poster;
    private String ageRating;
    private LocalDateTime createdAt;
    private LocalDate releaseDate;

    public Movie() {

    }

    public Movie(int movieId, String title, int duration, String genre, String description,
                 String directedBy, String language, String poster, String ageRating, LocalDateTime createdAt, LocalDate releaseDate) {
        this.movieId = movieId;
        this.title = title;
        this.duration = duration;
        this.genre = genre;
        this.description = description;
        this.directedBy = directedBy;
        this.language = language;
        this.poster = poster;
        this.ageRating = ageRating;
        this.createdAt = createdAt;
        this.releaseDate = releaseDate;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDirectedBy() {
        return directedBy;
    }

    public void setDirectedBy(String directedBy) {
        this.directedBy = directedBy;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getReleasedDate() {
        return releaseDate;
    }

    public void setReleasedDate(LocalDate releasedDate) {
        this.releaseDate = releasedDate;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "movieId=" + movieId +
                ", title='" + title + '\'' +
                ", duration=" + duration +
                ", genre='" + genre + '\'' +
                ", description='" + description + '\'' +
                ", directedBy='" + directedBy + '\'' +
                ", language='" + language + '\'' +
                ", poster='" + poster + '\'' +
                ", ageRating='" + ageRating + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                '}';
    }
}
