package controller.controllerAdmin;

import dao.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class ForgotPasswordController {

    // Panes for each step
    @FXML private AnchorPane step1Pane;
    @FXML private AnchorPane step2Pane;
    @FXML private AnchorPane step3Pane;

    // UI elements for Step 1
    @FXML private TextField usernameField;

    // UI elements for Step 2
    @FXML private Label questionLabel;
    @FXML private TextField answerField;

    // UI elements for Step 3
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    // State variables
    private String currentUsername;
    private String securityAnswer;

    @FXML
    public void initialize() {
        // Initially show only Step 1
        showStep(1);
    }

    private void showStep(int step) {
        step1Pane.setVisible(false);
        step2Pane.setVisible(false);
        step3Pane.setVisible(false);

        switch (step) {
            case 1:
                step1Pane.setVisible(true);
                break;
            case 2:
                step2Pane.setVisible(true);
                break;
            case 3:
                step3Pane.setVisible(true);
                break;
        }
    }

    @FXML
    private void handleVerifyUser(ActionEvent event) {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter your username.");
            return;
        }

        try {
            if (UserDAO.isUsernameExists(username)) {
                this.currentUsername = username;
                String[] questionAndAnswer = UserDAO.getSecurityQuestionAndAnswer(username);
                if (questionAndAnswer != null) {
                    questionLabel.setText(questionAndAnswer[0]);
                    this.securityAnswer = questionAndAnswer[1];
                    showStep(2); // Go to Step 2
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "No security question found for this user.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Username does not exist.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while checking user information.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSubmitAnswer(ActionEvent event) {
        String userAnswer = answerField.getText().trim();
        if (userAnswer.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter the answer.");
            return;
        }

        if (userAnswer.equals(this.securityAnswer)) {
            showStep(3); // Go to Step 3
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Incorrect answer. Please try again.");
        }
    }

    @FXML
    private void handleResetPassword(ActionEvent event) {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all password fields.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match.");
            return;
        }

        // TODO: Validate password strength if needed

        try {
            // Update password in the database
            UserDAO.updatePasswordByUsername(this.currentUsername, newPassword);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Password has been successfully reset.");
            loadLoginScene(event); // Return to login screen
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while updating the password.");
            e.printStackTrace();
        }
    }

    private void loadLoginScene(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Admin/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to load login page.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
