package models;

public class Movie {
    private int id;
    private String movieName;
    private String genre;
    private String duration;
    private String releaseDate; // Ngày chiếu (hoặc ngày phát hành)
    private String imagePath;
    private String synopsis; // Tóm tắt
    private String cast;     // Diễn viên
    private String showtime; // Thời gian chiếu (ví dụ: "10:00, 14:00")

    // Constructor với 9 tham số
    public Movie(int id, String movieName, String genre, String duration, String releaseDate, String imagePath, String synopsis, String cast, String showtime) {
        this.id = id;
        this.movieName = movieName;
        this.genre = genre;
        this.duration = duration;
        this.releaseDate = releaseDate;
        this.imagePath = imagePath;
        this.synopsis = synopsis;
        this.cast = cast;
        this.showtime = showtime;
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getMovieName() { return movieName; }
    public String getGenre() { return genre; }
    public String getDuration() { return duration; }
    public String getReleaseDate() { return releaseDate; }
    public String getImagePath() { return imagePath; }
    public String getSynopsis() { return synopsis; }
    public String getCast() { return cast; }
    public String getShowtime() { return showtime; }

    // --- Setters ---
    public void setId(int id) { this.id = id; }
    public void setMovieName(String movieName) { this.movieName = movieName; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setSynopsis(String synopsis) { this.synopsis = synopsis; }
    public void setCast(String cast) { this.cast = cast; }
    public void setShowtime(String showtime) { this.showtime = showtime; }
}