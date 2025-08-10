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

    // Consumer callback to notify external classes when a showtime is clicked
    private Consumer<Showtime> onShowtimeSelected;

    // Original list of showtimes, used to retrieve the clicked showtime by index
    private List<Showtime> showtimes;

    /**
     * Sets the movie showtime item data.
     * Uses the first showtime to display the date and movie title,
     * while storing the full list to display all available showtimes.
     *
     * @param firstShowtime the first showtime (used for date and movie title)
     * @param showtimes     the full list of showtimes for this movie
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
     * Displays the list of showtimes in the GridPane.
     * Shows a maximum of 10 showtimes per row.
     * Each showtime is displayed as a clickable box.
     *
     * @param showtimesLocalTime list of showtime LocalTime objects
     */
    public void setShowtimeList(List<java.time.LocalTime> showtimesLocalTime) {
        gridShowtime.getChildren().clear();

        int maxPerRow = 10;
        int col = 0, row = 0;

        gridShowtime.getColumnConstraints().clear();
        gridShowtime.getRowConstraints().clear();

        // Configure column constraints
        for (int i = 0; i < maxPerRow; i++) {
            javafx.scene.layout.ColumnConstraints cc = new javafx.scene.layout.ColumnConstraints();
            cc.setPrefWidth(80);
            cc.setMinWidth(50);
            cc.setMaxWidth(100);
            gridShowtime.getColumnConstraints().add(cc);
        }

        // Configure row constraints
        int rowCount = (showtimesLocalTime.size() + maxPerRow - 1) / maxPerRow;
        for (int i = 0; i < rowCount; i++) {
            javafx.scene.layout.RowConstraints rc = new javafx.scene.layout.RowConstraints();
            rc.setPrefHeight(30);
            rc.setMinHeight(25);
            rc.setMaxHeight(35);
            gridShowtime.getRowConstraints().add(rc);
        }

        // Add clickable time boxes to the grid
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

            // Register click event to send the clicked showtime to the listener
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
     * Sets an external listener for showtime click events.
     *
     * @param listener a Consumer that handles the selected Showtime
     */
    public void setOnShowtimeSelected(Consumer<Showtime> listener) {
        this.onShowtimeSelected = listener;
    }
}
