package dao;

import models.Showtime;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShowtimeDAO {

    private static final List<Showtime> mockData = new ArrayList<>();

    static {
        Showtime s1 = new Showtime(1, 101, 201, "2025-08-01", "18:00", "2025-07-01 10:00");
        s1.setMovieTitle("Oppenheimer");
        s1.setRoomName("Room A");
        mockData.add(s1);

        Showtime s2 = new Showtime(2, 102, 202, "2025-08-01", "20:30", "2025-07-01 10:30");
        s2.setMovieTitle("Barbie");
        s2.setRoomName("Room B");
        mockData.add(s2);
    }

    public static List<Showtime> getAllShowtimes() {
        return new ArrayList<>(mockData);
    }

    public static List<Showtime> findShowtimes(String keyword) {
        return mockData.stream()
                .filter(s -> s.getMovieTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                        s.getRoomName().toLowerCase().contains(keyword.toLowerCase()) ||
                        s.getShowDate().contains(keyword))
                .collect(Collectors.toList());
    }

    public static boolean insertShowtime(Showtime showtime) {
        return mockData.add(showtime);
    }

    public static boolean updateShowtime(Showtime updated) {
        for (int i = 0; i < mockData.size(); i++) {
            if (mockData.get(i).getShowtimeId() == updated.getShowtimeId()) {
                mockData.set(i, updated);
                return true;
            }
        }
        return false;
    }

    public static boolean deleteShowtime(int showtimeId) {
        return mockData.removeIf(s -> s.getShowtimeId() == showtimeId);
    }

    public static Showtime getShowtimeById(int showtimeId) {
        return mockData.stream()
                .filter(s -> s.getShowtimeId() == showtimeId)
                .findFirst()
                .orElse(null);
    }

}
