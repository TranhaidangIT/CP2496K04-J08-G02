package controller.controllerManager;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import models.Showtime;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public class MovieShowtimeItemController {

    @FXML
    private GridPane gridShowtime;

    @FXML
    private Label txtDate;

    @FXML
    private Label txtMovie;

    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    // Dùng Consumer để truyền sự kiện chọn showtime ra bên ngoài
    private Consumer<Showtime> onShowtimeSelected;

    // Danh sách showtimes gốc, để lấy showtime theo index khi click
    private List<Showtime> showtimes;

    /**
     * Thiết lập dữ liệu cho item: lấy showtime đầu để hiển thị thông tin phim và ngày,
     * và danh sách showtime đầy đủ để hiển thị các giờ chiếu.
     */
    public void setData(Showtime firstShowtime, List<Showtime> showtimes) {
        this.showtimes = showtimes;
        txtDate.setText(firstShowtime.getShowDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtMovie.setText(firstShowtime.getMovieTitle());

        List<java.time.LocalTime> times = showtimes.stream()
                .map(Showtime::getShowTime)
                .toList();

        setShowtimeList(times);
    }

    /**
     * Hiển thị danh sách giờ chiếu trong GridPane, tối đa 10 giờ mỗi hàng.
     * Tạo các box giờ chiếu có thể click được.
     */
    public void setShowtimeList(List<java.time.LocalTime> showtimesLocalTime) {
        gridShowtime.getChildren().clear();

        int maxPerRow = 10;
        int col = 0, row = 0;

        gridShowtime.getColumnConstraints().clear();
        gridShowtime.getRowConstraints().clear();

        for (int i = 0; i < maxPerRow; i++) {
            javafx.scene.layout.ColumnConstraints cc = new javafx.scene.layout.ColumnConstraints();
            cc.setPrefWidth(80);
            cc.setMinWidth(50);
            cc.setMaxWidth(100);
            gridShowtime.getColumnConstraints().add(cc);
        }

        int rowCount = (showtimesLocalTime.size() + maxPerRow - 1) / maxPerRow;
        for (int i = 0; i < rowCount; i++) {
            javafx.scene.layout.RowConstraints rc = new javafx.scene.layout.RowConstraints();
            rc.setPrefHeight(30);
            rc.setMinHeight(25);
            rc.setMaxHeight(35);
            gridShowtime.getRowConstraints().add(rc);
        }

        for (int i = 0; i < showtimesLocalTime.size(); i++) {
            java.time.LocalTime time = showtimesLocalTime.get(i);

            HBox box = new HBox();
            box.setSpacing(10);
            box.setPrefWidth(80);
            box.setPrefHeight(30);
            box.getStyleClass().add("showtime-box");

            Label timeLabel = new Label(time.format(timeFmt));
            timeLabel.getStyleClass().add("showtime-label");

            box.getChildren().add(timeLabel);

            // Đăng ký sự kiện click, truyền showtime tương ứng ra bên ngoài
            final int index = i;
            box.setOnMouseClicked((MouseEvent event) -> {
                if (onShowtimeSelected != null && showtimes != null && index < showtimes.size()) {
                    onShowtimeSelected.accept(showtimes.get(index));
                }
            });

            gridShowtime.add(box, col, row);

            col++;
            if (col >= maxPerRow) {
                col = 0;
                row++;
            }
        }
    }

    /**
     * Cho phép set listener bên ngoài lắng nghe sự kiện chọn showtime
     */
    public void setOnShowtimeSelected(Consumer<Showtime> listener) {
        this.onShowtimeSelected = listener;
    }
}
