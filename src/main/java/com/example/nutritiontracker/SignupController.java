package com.example.nutritiontracker;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SignupController extends Implementation {
    @FXML
    private TextField emailField;
    @FXML
    private TextField firstnameField;
    @FXML
    private TextField lastnameField;
    @FXML
    private TextField passwordField;
    @FXML
    private Button signupButton;
    @FXML
    private Button backButton;

    @FXML
    public void initialize() {
        // Initialization logic if needed
    }

    @FXML
    private void handleSignupAction() {
        Task<Boolean> signupTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                String email = emailField.getText();
                String firstname = firstnameField.getText();
                String lastname = lastnameField.getText();
                String password = passwordField.getText();

                if (!validateInput(email, firstname, lastname, password)) {
                    Platform.runLater(() -> showAlert("Validation Error", "Please ensure all fields are filled correctly."));
                    return false;
                }

                if (!validatePassword(password)) {
                    Platform.runLater(() -> showAlert("Validation Error", "Password must be:\n- At least 8 characters long\n- Have at least one number\n- Have at least one uppercase letter."));
                    return false;
                }

                if (isEmailUsed(email)) {
                    Platform.runLater(() -> showAlert("Signup Failed", "Email is already in use."));
                    return false;
                }

                boolean userCreated = createUser(firstname, lastname, email, password);
                if (userCreated) {
                    Platform.runLater(() -> {
                        showAlert("Signup Successful", "Your account has been created.");
                        clearForm();
                    });
                } else {
                    Platform.runLater(() -> showAlert("Signup Failed", "An error occurred. Please try again later."));
                }
                return userCreated;
            }
        };

        new Thread(signupTask).start();
    }

    private boolean validateInput(String email, String firstname, String lastname, String password) {
        return !email.isEmpty() && !firstname.isEmpty() && !lastname.isEmpty() && !password.isEmpty();
    }

    private boolean validatePassword(String password) {
        boolean lengthCheck = password.length() >= 8;
        boolean numberCheck = password.matches(".*\\d.*");
        boolean uppercaseCheck = !password.equals(password.toLowerCase());
        return lengthCheck && numberCheck && uppercaseCheck;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearForm() {
        emailField.clear();
        firstnameField.clear();
        lastnameField.clear();
        passwordField.clear();
    }

    public boolean isEmailUsed(String email) throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM User WHERE Email = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, email);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt("count") > 0;
        }
        return false;
    }

    public boolean createUser(String firstname, String lastname, String email, String password) throws SQLException {
        String query = "INSERT INTO User (Firstname, Lastname, Email, Password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, firstname);
            statement.setString(2, lastname);
            statement.setString(3, email);
            statement.setString(4, password);
            int result = statement.executeUpdate();
            return result > 0;
        }
    }

    @FXML
    private void handleBackAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
