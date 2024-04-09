package com.example.nutritiontracker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HomeController extends Implementation {
    @FXML
    private PieChart pieChart;
    @FXML
    private BarChart<String, Number> totalcaloriesChart;
    @FXML
    private Label totalcaloriesLabel;
    @FXML
    private Label totalproteinLabel;
    @FXML
    private Label totalfatLabel;
    @FXML
    private Label totalcarbsLabel;
    @FXML
    private Label nomacroLabel;

    private Connection connection;

    public void initialize() {
        loadMacroDataIntoPieChart();
        loadCaloriesBarChart();
    }

    public void connectDB() {
        // Assuming a method connectDB exists for connecting to the database
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            String dbURL = "jdbc:ucanaccess://NutritionTracker.accdb"; // Modify this with your actual database path
            connection = DriverManager.getConnection(dbURL);
        } catch (Exception e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMacroDataIntoPieChart() {
        connectDB();
        LocalDate currentDate = LocalDate.now();
        String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));

        String sql = "SELECT SUM(CAST(Calories AS DOUBLE)) AS totalCalories, " +
                "SUM(CAST(Protein AS DOUBLE)) AS totalProtein, " +
                "SUM(CAST(Fat AS DOUBLE)) AS totalFat, " +
                "SUM(CAST(Carbs AS DOUBLE)) AS totalCarbs " +
                "FROM Tracker WHERE UserID = ? AND FORMAT(DateAdded, 'MM/dd/yyyy') = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, LoginController.currentUserId); // Replace with actual current user ID
            pstmt.setString(2, formattedDate);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double totalCalories = rs.getDouble("totalCalories");
                double totalProtein = rs.getDouble("totalProtein");
                double totalFat = rs.getDouble("totalFat");
                double totalCarbs = rs.getDouble("totalCarbs");

                if (totalCalories + totalProtein + totalFat + totalCarbs > 0) {
                    ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                            new PieChart.Data("Protein", totalProtein),
                            new PieChart.Data("Fat", totalFat),
                            new PieChart.Data("Carbs", totalCarbs)
                    );
                    pieChart.setData(pieChartData);
                    pieChart.setTitle("Daily Macros");

                    totalcaloriesLabel.setText(String.format("%.2f", totalCalories));
                    totalproteinLabel.setText(String.format("%.2f g", totalProtein));
                    totalfatLabel.setText(String.format("%.2f g", totalFat));
                    totalcarbsLabel.setText(String.format("%.2f g", totalCarbs));
                } else {
                    pieChart.setData(FXCollections.observableArrayList());
                    nomacroLabel.setText("No macro data available for the current date.");
                }
            } else {
                pieChart.setData(FXCollections.observableArrayList());
                nomacroLabel.setText("No macro data available for the current date.");
            }
        } catch (SQLException e) {
            System.err.println("Error loading macro data into pie chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCaloriesBarChart() {
        totalcaloriesChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        LocalDate currentDate = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = currentDate.minusDays(i);
            double totalCalories = getTotalCaloriesForDate(date);
            series.getData().add(new XYChart.Data<>(date.format(DateTimeFormatter.ofPattern("MM/dd")), totalCalories));
        }
        totalcaloriesChart.getData().add(series);
    }

    private double getTotalCaloriesForDate(LocalDate date) {
        String sql = "SELECT SUM(CAST(Calories AS DOUBLE)) AS totalCalories " +
                "FROM Tracker WHERE UserID = ? AND FORMAT(DateAdded, 'MM/dd/yyyy') = ?";
        double totalCalories= 0;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, LoginController.currentUserId);
            pstmt.setString(2, date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                totalCalories = rs.getDouble("totalCalories");
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving total calories for " + date + ": " + e.getMessage());
            e.printStackTrace();
        }
        return totalCalories;
    }

}