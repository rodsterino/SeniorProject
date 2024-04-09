package com.example.nutritiontracker;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FoodDatabaseController extends Implementation {



    private final String baseUrl = "https://api.edamam.com/api/food-database/v2/parser";
    private final String appId = "2779cfe2";
    private final String appKey = "66be6e191fb200b10f484134e8be2b23";

    @FXML
    private TextField searchTextField;
    @FXML
    private TextField weightTextField; // TextField for weight input
    @FXML
    private ListView<String> foodItemList;
    @FXML
    private TextArea macronutrientDetailsTextArea;
    @FXML
    private Button addButton;
    @FXML
    private Label confirmationLabel;
    @FXML
    private ToggleGroup mealToggleGroup;

    @FXML
    private RadioButton radioBreakfast;

    @FXML
    private RadioButton radioDinner;

    @FXML
    private RadioButton radioLunch;
    @FXML
    public void initialize() {
        foodItemList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                fetchMacronutrients(newValue);
            }
        });
    }

    @FXML
    private void handleSearchAction() {
        foodItemList.getItems().clear();
        new Thread(() -> {
            String query = searchTextField.getText();
            searchFoodItem(query);
        }).start();
    }
    @FXML
    private void handleAddAction() {
        FoodMacro foodMacro = parseMacroDetails(macronutrientDetailsTextArea.getText());
        RadioButton selectedMealButton = (RadioButton) mealToggleGroup.getSelectedToggle();
        String mealTime = selectedMealButton != null ? selectedMealButton.getText() : null;

        int userID = LoginController.currentUserId;
        if (userID > 0) { // Ensures that a user is logged in
            insertIntoDatabase(userID, foodMacro);
        } else {
            // Handle the case where no user is logged in, maybe show an error message
            Platform.runLater(() -> {
                showAlertDialog(Alert.AlertType.ERROR, "No User Logged In", "Please log in to add food items.");
            });
        }
    }
    private void searchFoodItem(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            URL url = new URL(baseUrl + "?ingr=" + encodedQuery + "&app_id=" + appId + "&app_key=" + appKey);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray hints = jsonResponse.getJSONArray("hints");
                Platform.runLater(() -> {
                    for (int i = 0; i < hints.length(); i++) {
                        JSONObject food = hints.getJSONObject(i).getJSONObject("food");
                        String label = food.getString("label");
                        foodItemList.getItems().add(label);
                    }
                });
            } else {
                Platform.runLater(() -> foodItemList.getItems().add("Request failed. Response Code: " + responseCode));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> foodItemList.getItems().add("Error occurred while fetching data: " + e.getMessage()));
        }
    }

    private void fetchMacronutrients(String foodLabel) {
        new Thread(() -> {
            try {
                String encodedLabel = URLEncoder.encode(foodLabel, "UTF-8");
                URL url = new URL(baseUrl + "?ingr=" + encodedLabel + "&app_id=" + appId + "&app_key=" + appKey);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray hints = jsonResponse.getJSONArray("hints");
                    if (hints.length() > 0) {
                        JSONObject food = hints.getJSONObject(0).getJSONObject("food");
                        JSONObject nutrients = food.getJSONObject("nutrients");

                        double weight = Double.parseDouble(weightTextField.getText()); // Parse the weight
                        double calories = nutrients.optDouble("ENERC_KCAL", 0) * weight / 100;
                        double protein = nutrients.optDouble("PROCNT", 0) * weight / 100;
                        double fat = nutrients.optDouble("FAT", 0) * weight / 100;
                        double carbs = nutrients.optDouble("CHOCDF", 0) * weight / 100;

                        String nutritionFacts = String.format(
                                "Nutrition facts: Serving Size (%.2f Gram)\nFood Item: %s\nCalories: %.2f kcal\nProtein: %.2f g\nFat: %.2f g\nCarbs: %.2f g",
                                weight, foodLabel, calories, protein, fat, carbs
                        );


                        Platform.runLater(() -> macronutrientDetailsTextArea.setText(nutritionFacts));
                    } else {
                        Platform.runLater(() -> macronutrientDetailsTextArea.setText("No results found."));
                    }
                } else {
                    Platform.runLater(() -> macronutrientDetailsTextArea.setText("GET request not worked"));
                }
            } catch (Exception e) {
                Platform.runLater(() -> macronutrientDetailsTextArea.setText("Error occurred while fetching data."));
                e.printStackTrace();
            }
        }).start();
    }
    private void showAlertDialog(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header
        alert.setContentText(content);
        Platform.runLater(alert::showAndWait);
    }
    private FoodMacro parseMacroDetails(String details) {
        String[] lines = details.split("\\n");
        String foodItem = lines[1].substring(lines[1].indexOf(":") + 1).trim();
        double weight = Double.parseDouble(lines[0].substring(lines[0].indexOf("(") + 1, lines[0].indexOf("G")).trim()); // Parsing weight as double
        double calories = Double.parseDouble(lines[2].substring(lines[2].indexOf(":") + 1, lines[2].indexOf("kcal")).trim());
        double protein = Double.parseDouble(lines[3].substring(lines[3].indexOf(":") + 1, lines[3].indexOf("g")).trim());
        double fat = Double.parseDouble(lines[4].substring(lines[4].indexOf(":") + 1, lines[4].indexOf("g")).trim());
        double carbs = Double.parseDouble(lines[5].substring(lines[5].indexOf(":") + 1, lines[5].indexOf("g")).trim());
        String mealTime = getSelectedMealTime();
        String dateAdded = getCurrentDate(); // This method needs to be implemented to get the current date in a format like "yyyy-MM-dd"

        return new FoodMacro(foodItem, weight, calories, protein, fat, carbs, mealTime, dateAdded);
    }

    private String getCurrentDate() {
        // Implementation to return the current date in the desired format
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }
    private String getSelectedMealTime() {
        RadioButton selectedRadioButton = (RadioButton) mealToggleGroup.getSelectedToggle();
        return selectedRadioButton.getText();
    }

    private void insertIntoDatabase(int userID, FoodMacro foodMacro) {
        connectDB(); // Ensure the connection is established
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO Tracker (UserID, FoodItem, Weight, Calories, Protein, Fat, Carbs, MealTime, DateAdded) VALUES (?, ?, ?, ?, ?, ?, ?, ?,?)")) {
            preparedStatement.setInt(1, userID);
            preparedStatement.setString(2, foodMacro.getFoodItem());
            preparedStatement.setDouble(3, foodMacro.getWeight());
            preparedStatement.setDouble(4, foodMacro.getCalories());
            preparedStatement.setDouble(5, foodMacro.getProtein());
            preparedStatement.setDouble(6, foodMacro.getFat());
            preparedStatement.setDouble(7, foodMacro.getCarbs());
            preparedStatement.setString(8, foodMacro.getMealTime()); // Add mealTime to the prepared statement
            preparedStatement.setTimestamp(9, new java.sql.Timestamp(System.currentTimeMillis())); // Set current timestamp


            // Execute the update
            int result = preparedStatement.executeUpdate();

            // Check if the update was successful
            if (result > 0) {
                Platform.runLater(() -> confirmationLabel.setText("\u2705 Added food item successfully"));
                PauseTransition pause = new PauseTransition(Duration.seconds(1));
                pause.setOnFinished(event -> confirmationLabel.setText(""));
                pause.play();
            }
        } catch (SQLException e) {
            System.out.println("Error executing insert into database");
            e.printStackTrace();
        }

    }





}
