package com.example.nutritiontracker;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {
    @FXML
    private Button loginButton;
    @FXML
    private Button signupButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField usernameTextField;

    // Static variable to hold the current user's ID for global access
    public static int currentUserId = -1;
    public static String loggedInUserName = "";

    @FXML
    private void initialize() {
        setupLoginButtonAction();
    }
    @FXML
    private void onSignupButtonClick() {
        try {
            // Load Signup.fxml
            Parent signupRoot = FXMLLoader.load(getClass().getResource("Signup.fxml"));
            Scene signupScene = new Scene(signupRoot);

            // Get the current stage using the loginButton (or signupButton) reference
            Stage stage = (Stage) signupButton.getScene().getWindow();

            // Set the new scene on the current stage
            stage.setScene(signupScene);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an error dialog)
        }
    }
    @FXML
    private void setupLoginButtonAction() {
        loginButton.setOnAction(event -> {
            String email = usernameTextField.getText();
            String password = passwordField.getText();
            if (validateCredentials(email, password)) {
                //showAlertDialog(Alert.AlertType.INFORMATION, "Login Successful", "Welcome!" );
                switchToMainPane();
            } else {
                showAlertDialog(Alert.AlertType.ERROR, "Login Failed", "Incorrect email or password.");
            }
        });
    }

    private boolean validateCredentials(String email, String password) {
        try {
            String query = "SELECT ID, FirstName, Email, Password FROM User WHERE Email = ? AND Password = ?";
            PreparedStatement pst = Implementation.connection.prepareStatement(query);
            pst.setString(1, email);
            pst.setString(2, password);
            ResultSet resultSet = pst.executeQuery();

            if (resultSet.next()) {
                currentUserId = resultSet.getInt("ID");
                loggedInUserName = resultSet.getString("FirstName");
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlertDialog(Alert.AlertType.ERROR, "Database Error", "Error connecting to the database.");
            return false;
        }
    }



    public static int getCurrentUserId() {
        return currentUserId;
    }

    private void switchToMainPane() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/nutritiontracker/MainPane.fxml")); // check the path
            Parent mainPaneRoot = loader.load();
            NutritionDashboard controller = loader.getController();
            controller.setWelcomeMessage(loggedInUserName); // This line is now valid because the method exists in NutritionDashboard

            Scene mainPaneScene = new Scene(mainPaneRoot, 1400, 800);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(mainPaneScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlertDialog(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
