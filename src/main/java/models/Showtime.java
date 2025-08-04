package models;

public class Showtime {
    private int showtimeId;
    private int movieId;
    private int roomId;
    private String showDate;
    private String showTime;
    private String createdAt;
    private String movieTitle;
    private String roomName;
    private String endTime;

    public Showtime() {
        this.showtimeId = 0;
        this.movieId = 0;
        this.roomId = 0;
        this.showDate = null;
        this.showTime = null;
        this.endTime = null;
        this.createdAt = null;
    }

    public Showtime(int showtimeId,
                    int movieId,
                    int roomId,
                    String showDate,
                    String showTime,
                    String createdAt) {
        this.showtimeId = showtimeId;
        this.movieId = movieId;
        this.roomId = roomId;
        this.showDate = showDate;
        this.showTime = showTime;
        this.createdAt = createdAt;
    }

    public int getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(int showtimeId) {
        this.showtimeId = showtimeId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getShowDate() {
        return showDate;
    }

    public void setShowDate(String showDate) {
        this.showDate = showDate;
    }

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "Showtime{" +
                "showtimeId=" + showtimeId +
                ", movieId=" + movieId +
                ", roomId=" + roomId +
                ", showDate='" + showDate + '\'' +
                ", showTime='" + showTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", movieTitle='" + movieTitle + '\'' +
                ", roomName='" + roomName + '\'' +
                '}';
    }
}
