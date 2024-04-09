package com.example.nutritiontracker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;

import java.sql.*;
import java.time.LocalDate;

public class TrackerController {

    @FXML
    private ListView<String> breakfastListView;
    @FXML
    private ListView<String> lunchListView;
    @FXML
    private ListView<String> dinnerListView;
    @FXML
    private DatePicker datePicker; // Ensure this is correctly linked with your FXML

    public void initialize() {
        datePicker.setValue(LocalDate.now()); // Initialize the datePicker to today's date
        onDateChange(); // Load data based on the current date
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

        // Fetch all records for the current user, without filtering by date in SQL
        String sql = "SELECT * FROM Tracker WHERE UserID = " + LoginController.currentUserId;

        try (Connection conn = connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Extract the DateAdded field from the database
                Timestamp timestamp = rs.getTimestamp("DateAdded");
                LocalDate dateAdded = null;
                if (timestamp != null) { // Add this null check
                    dateAdded = timestamp.toLocalDateTime().toLocalDate();
                    // The rest of your code
                }

                // Continue only if the dateAdded matches the selectedDate
                if (dateAdded.equals(selectedDate)) {
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
        } catch (SQLException e) {
            System.out.println("Error loading meal data: " + e.getMessage());
        }

        breakfastListView.setItems(breakfastItems);
        lunchListView.setItems(lunchItems);
        dinnerListView.setItems(dinnerItems);
    }
}
