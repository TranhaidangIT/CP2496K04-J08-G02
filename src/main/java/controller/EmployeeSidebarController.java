package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import controller.controllerEmployees.ListMoviesController;
import controller.controllerEmployees.TotalController;
import controller.controllerEmployees.InvoiceHistoryController;
import java.io.IOException;
import java.net.URL;

public class EmployeeSidebarController {

    @FXML
    private AnchorPane contentArea;

    private void setContent(String fxmlFile) {
        try {
            String path = "/views/fxml_Employees/" + fxmlFile;
            URL fxmlUrl = getClass().getResource(path);
            if (fxmlUrl == null) {
                System.err.println("FXML file not found: " + path);
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent fxml = loader.load();
            contentArea.getChildren().setAll(fxml);
            AnchorPane.setTopAnchor(fxml, 0.0);
            AnchorPane.setBottomAnchor(fxml, 0.0);
            AnchorPane.setLeftAnchor(fxml, 0.0);
            AnchorPane.setRightAnchor(fxml, 0.0);

            // Truyền contentArea cho các controller cần thiết
            Object controller = loader.getController();

            if (controller instanceof ListMoviesController) {
                ((ListMoviesController) controller).setContentArea(contentArea);
            } else if (controller instanceof TotalController) {
                ((TotalController) controller).setContentArea(contentArea);
            } else if (controller instanceof InvoiceHistoryController) {
                ((InvoiceHistoryController) controller).setContentArea(contentArea);
            }

        } catch (IOException e) {
            System.err.println("Lỗi khi tải " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        loadListMovies(null);
    }

    @FXML
    private void loadListMovies(ActionEvent event) {
        setContent("ListMovies.fxml");
    }

    @FXML
    private void loadSeatSelection(ActionEvent event) {
        try {
            String path = "/views/fxml_Employees/SeatDemo.fxml";
            URL fxmlUrl = getClass().getResource(path);
            if (fxmlUrl == null) {
                System.err.println("FXML file not found: " + path);
                return;
            }
            System.out.println("Tải SeatDemo.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent fxml = loader.load();
            contentArea.getChildren().setAll(fxml);
            AnchorPane.setTopAnchor(fxml, 0.0);
            AnchorPane.setBottomAnchor(fxml, 0.0);
            AnchorPane.setLeftAnchor(fxml, 0.0);
            AnchorPane.setRightAnchor(fxml, 0.0);
        } catch (IOException e) {
            System.err.println("Lỗi khi tải SeatDemo.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void loadSellAddons(ActionEvent event) {
        setContent("SellAddons.fxml");
    }

    @FXML
    private void loadLocker(ActionEvent event) {
        setContent("Locker.fxml");
    }

    @FXML
    private void loadTotal(ActionEvent event) {
        setContent("Total.fxml");
    }

    @FXML
    private void onLogoutClicked(ActionEvent event) {
        try {
            Node source = (Node) event.getSource();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml_Login/Login.fxml"));
            source.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}