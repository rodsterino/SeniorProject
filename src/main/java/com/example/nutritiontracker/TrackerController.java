package com.example.nutritiontracker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TrackerController {

    @FXML
    private ListView<String> breakfastListView;
    @FXML
    private ListView<String> lunchListView;
    @FXML
    private ListView<String> dinnerListView;
    @FXML
    private DatePicker datePicker; // Ensure this is correctly linked with your FXML
    @FXML
    private TextField waterIntakeField;
    @FXML
    private Button logWaterIntakeButton;
    @FXML
    private ProgressBar waterProgressBar;

    private int dailyWaterGoal = 8; // Daily water intake goal (in glasses)
    private int totalWaterConsumed = 0; // Total glasses of water consumed

    @FXML
    private void initialize() {
        datePicker.setValue(LocalDate.now()); // Initialize the datePicker to today's date
        onDateChange(); // Load data based on the current date

        // Initialize water intake tracker
        waterProgressBar.setProgress(0); // Initialize progress bar
        waterIntakeField.setPromptText("Enter glasses of water consumed"); // Set prompt text
        logWaterIntakeButton.setOnAction(e -> logWaterIntake()); // Attach event handler
    }

    private void logWaterIntake() {
        try {
            int consumed = Integer.parseInt(waterIntakeField.getText());
            totalWaterConsumed += consumed;
            updateWaterProgressBar();
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number of glasses.");
        }
    }

    // Method to update the water intake progress bar
    private void updateWaterProgressBar() {
        double progress = (double) totalWaterConsumed / dailyWaterGoal;
        waterProgressBar.setProgress(progress);
    }

    private Connection connectDB() {
        Connection conn = null;
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            String msAccDB = "NutritionTracker.accdb";
            String dbURL = "jdbc:ucanaccess://" + msAccDB;
            conn = DriverManager.getConnection(dbURL);
        } catch (Exception e) {
            System.out.println("Error connecting to Tracker database");
            e.printStackTrace();
        }
        return conn;
    }

    public void onDateChange() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            loadMealData(selectedDate);
        }
    }

    public void loadMealData(LocalDate selectedDate) {
        ObservableList<String> breakfastItems = FXCollections.observableArrayList();
        ObservableList<String> lunchItems = FXCollections.observableArrayList();
        ObservableList<String> dinnerItems = FXCollections.observableArrayList();

        // Fetch all records for the current user, filtering by date
        String sql = "SELECT * FROM Tracker WHERE UserID = " + LoginController.currentUserId + " AND FORMAT(DateAdded, 'MM/dd/yyyy') = ?";

        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the selected date as parameter
            pstmt.setString(1, selectedDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String foodItem = rs.getString("FoodItem");
                    double weight = rs.getDouble("Weight");
                    double calories = rs.getDouble("Calories");
                    double protein = rs.getDouble("Protein");
                    double fat = rs.getDouble("Fat");
                    double carbs = rs.getDouble("Carbs");
                    String mealTime = rs.getString("MealTime");
                    String displayText = String.format("FoodItem: %s, Weight: %.2fg, Calories: %.2fkcal, Protein: %.2fg, Fat: %.2fg, Carbs: %.2fg",
                            foodItem, weight, calories, protein, fat, carbs);

                    switch (mealTime.toLowerCase()) {
                        case "breakfast":
                            breakfastItems.add(displayText);
                            break;
                        case "lunch":
                            lunchItems.add(displayText);
                            break;
                        case "dinner":
                            dinnerItems.add(displayText);
                            break;
                    }
                }
            }

            // Calculate the total water intake for the selected date
            int totalWaterConsumedForDay = calculateTotalWaterIntakeForDay(conn, selectedDate);
            totalWaterConsumed = totalWaterConsumedForDay;
            updateWaterProgressBar();
        } catch (SQLException e) {
            System.out.println("Error loading meal data: " + e.getMessage());
        }

        breakfastListView.setItems(breakfastItems);
        lunchListView.setItems(lunchItems);
        dinnerListView.setItems(dinnerItems);
    }

    private int calculateTotalWaterIntakeForDay(Connection conn, LocalDate selectedDate) throws SQLException {
        int totalWaterIntake = 0;
        String sql = "SELECT SUM(CAST(WaterIntake AS DOUBLE)) AS totalWaterIntake FROM Tracker WHERE UserID = ? AND FORMAT(DateAdded, 'MM/dd/yyyy') = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, LoginController.currentUserId);
            pstmt.setString(2, selectedDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalWaterIntake = rs.getInt("totalWaterIntake");
                }
            }
        }

        return totalWaterIntake;
    }
}
