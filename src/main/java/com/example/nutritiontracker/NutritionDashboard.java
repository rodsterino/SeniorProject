package com.example.nutritiontracker;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class NutritionDashboard extends Application {
    @FXML
    BorderPane root = new BorderPane();
    @FXML
    BorderPane centerPane = new BorderPane();
    Pane pane;
    public static void main(String[] args) throws Exception {
        Implementation imp = new Implementation();
        imp.initialize();
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            Scene scene = new Scene(root,920,400);
            scene.getStylesheets().add(this.getClass().getResource("application.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }
    @FXML
    private Button homeButton;

    @FXML
    private Button recipeButton;
    @FXML
    private Button searchButton;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Button trackerButton;
    @FXML
    void changePane(ActionEvent event) {
        String buttonText = "";
        try {
            Button clickedButton = (Button) event.getSource();
            buttonText = clickedButton.getText().replace(" ", "");
            pane = FXMLLoader.load(getClass().getResource("/com/example/nutritiontracker/" + buttonText + "Pane.fxml"));
            root.setCenter(pane);
        } catch (IOException e) {
            System.err.println("Failed to load the FXML for: " + buttonText);
            e.printStackTrace();
        }
    }
    public void setWelcomeMessage(String userName) {
        welcomeLabel.setText("Welcome," + userName + "!\n" +
                "Eat Well, Live Well");


    }

}
