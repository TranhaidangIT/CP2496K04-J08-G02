package models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Showtime {
    private int showtimeId;
    private int movieId;
    private int roomId;
    private LocalDate showDate;
    private LocalTime showTime;
    private LocalTime endTime;
    private LocalDateTime createdAt;
    private String movieTitle;
    private String roomName;

    public Showtime() {
    }

    public Showtime(int showtimeId, int movieId, int roomId,
                    LocalDate showDate, LocalTime showTime,
                    LocalTime endTime, LocalDateTime createdAt,
                    String movieTitle, String roomName) {
        this.showtimeId = showtimeId;
        this.movieId = movieId;
        this.roomId = roomId;
        this.showDate = showDate;
        this.showTime = showTime;
        this.endTime = endTime;
        this.createdAt = createdAt;
        this.movieTitle = movieTitle;
        this.roomName = roomName;
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

    public LocalDate getShowDate() {
        return showDate;
    }

    public void setShowDate(LocalDate showDate) {
        this.showDate = showDate;
    }

    public LocalTime getShowTime() {
        return showTime;
    }

    public void setShowTime(LocalTime showTime) {
        this.showTime = showTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
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

    @Override
    public String toString() {
        return "Showtime{" +
                "showtimeId=" + showtimeId +
                ", movieId=" + movieId +
                ", roomId=" + roomId +
                ", showDate=" + showDate +
                ", showTime=" + showTime +
                ", endTime=" + endTime +
                ", createdAt=" + createdAt +
                ", movieTitle='" + movieTitle + '\'' +
                ", roomName='" + roomName + '\'' +
                '}';
    }
}
