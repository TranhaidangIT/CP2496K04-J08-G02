package controller.controllerManager;

import dao.ShowtimeDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.ScreeningRoom;
import models.Showtime;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoomShowtimeBlockController {

    @FXML
    private Label lblRoomNumber;

    @FXML
    private VBox vboxListShowtime;

    @FXML
    private Button btnEditShowtime;

    @FXML
    private Button btnDeleteShowtime;

    private ScreeningRoom currentRoom;
    private LocalDate currentDate;

    /**
     * Thiết lập phòng và danh sách suất chiếu cho block này.
     * Nhóm theo phim + ngày để tạo nhiều MovieShowtimeItem.
     */
    public void setRoomAndShowtimes(ScreeningRoom room, List<Showtime> showtimes) {
        this.currentRoom = room;

        lblRoomNumber.setText(room.getRoomNumber());
        vboxListShowtime.getChildren().clear();

        if (showtimes.isEmpty()) return;

        // Nhóm showtime theo phim rồi theo ngày
        Map<String, Map<LocalDate, List<Showtime>>> groupedByMovieAndDate = showtimes.stream()
                .collect(Collectors.groupingBy(
                        Showtime::getMovieTitle,
                        Collectors.groupingBy(Showtime::getShowDate)
                ));

        currentDate = showtimes.stream()
                .map(Showtime::getShowDate)
                .sorted()
                .findFirst()
                .orElse(null);

        groupedByMovieAndDate.forEach((movieTitle, dateMap) -> {
            dateMap.forEach((date, stList) -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Manager/Showtime/MovieShowtimeItem.fxml"));
                    Parent node = loader.load();

                    MovieShowtimeItemController controller = loader.getController();
                    controller.setData(stList.get(0), stList); // truyền showtime đầu và cả list

                    vboxListShowtime.getChildren().add(node);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    /**
     * Bấm nút edit để chỉnh sửa suất chiếu của phòng này trong ngày currentDate.
     */
    @FXML
    private void handleEditClickShowtime() {
        if (currentRoom == null || currentDate == null) {
            showAlert(Alert.AlertType.WARNING, "Không có dữ liệu phòng hoặc ngày để chỉnh sửa.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Manager/Showtime/EditShowtime.fxml"));
            Parent root = loader.load();

            EditShowtimeController controller = loader.getController();

            // Chỉ truyền roomId và ngày, controller load toàn bộ showtime phòng đó trong ngày
            controller.loadShowtimeToEdit(currentRoom.getRoomId(), currentDate);

            Stage stage = new Stage();
            stage.setTitle("Chỉnh sửa suất chiếu phòng " + currentRoom.getRoomNumber() + " ngày " + currentDate);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Sau khi sửa xong, reload lại showtime cho block
            reloadShowtimes();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi khi mở cửa sổ chỉnh sửa suất chiếu.");
        }
    }

    /**
     * Xóa tất cả suất chiếu của phòng này trong ngày currentDate.
     */
    @FXML
    public void handleDeleteClickShowtime(ActionEvent actionEvent) {
        if (currentRoom == null || currentDate == null) {
            showAlert(Alert.AlertType.WARNING, "Không có dữ liệu phòng hoặc ngày để xóa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xóa tất cả suất chiếu của phòng "
                + currentRoom.getRoomNumber() + " vào ngày " + currentDate + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ShowtimeDAO showtimeDAO = new ShowtimeDAO();

                boolean success = showtimeDAO.deleteShowtimesByRoomAndDate(currentRoom.getRoomId(), currentDate);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Đã xóa thành công suất chiếu.");
                    vboxListShowtime.getChildren().clear();
                    currentDate = null;
                } else {
                    showAlert(Alert.AlertType.ERROR, "Xóa suất chiếu thất bại.");
                }
            }
        });
    }

    /**
     * Reload lại danh sách suất chiếu cho phòng và ngày hiện tại (có thể gọi sau sửa hoặc thêm).
     */
    private void reloadShowtimes() {
        if (currentRoom == null || currentDate == null) return;

        ShowtimeDAO showtimeDAO = new ShowtimeDAO();
        List<Showtime> showtimes = showtimeDAO.getShowtimesByRoomDate(currentRoom.getRoomId(), currentDate);

        setRoomAndShowtimes(currentRoom, showtimes);
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
