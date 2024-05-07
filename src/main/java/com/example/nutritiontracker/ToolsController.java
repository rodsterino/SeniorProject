package com.example.nutritiontracker;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ToolsController {
    private Connection connection;

    @FXML
    private ToggleGroup Gendergroup;

    @FXML
    private ComboBox<String> activityComboBox;

    @FXML
    private Label bmilbl;

    @FXML
    private Button calculateButton;

    @FXML
    private TextField calorieageTextField;

    @FXML
    private TextField caloriefeetTextfield;

    @FXML
    private TextField calorieinchesTextField;

    @FXML
    private TextField feetTextfield;

    @FXML
    private RadioButton femaleRadio;

    @FXML
    private TextField inchesTextField;

    @FXML
    private RadioButton maleRadio;

    @FXML
    private Button searchbmiButton;

    @FXML
    private TextField weightTextfield;

    @FXML
    private GridPane calorieResultsGrid;
    @FXML
    private TextField calorieWeightTextField;
    @FXML
    private ComboBox<String> weightRangeComboBox;
    @FXML
    private DatePicker weightDatePicker;

    @FXML
    private TextField poundsTextField;
    @FXML
    private Button addWeightButton;
    @FXML
    private LineChart<String, Number> weightLineChart;



    @FXML
    private void initialize() {
        searchbmiButton.setOnAction(event -> calculateBMI());
        calculateButton.setOnAction(event -> calculateCalories());
        connectDB();
        configureComboBox();
        loadWeightData("1 Month"); // Default load for 1 month
        addWeightButton.setOnAction(event -> addWeight());
        weightRangeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> loadWeightData(newVal));
        weightDatePicker.setValue(LocalDate.now());
    }
    private void connectDB() {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            String dbURL = "jdbc:ucanaccess://NutritionTracker.accdb"; // Modify this with your actual database path
            connection = DriverManager.getConnection(dbURL);
        } catch (Exception e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void addWeight() {
        if (weightDatePicker.getValue() != null && !poundsTextField.getText().isEmpty()) {
            try {
                double pounds = Double.parseDouble(poundsTextField.getText());
                // Check if a weight entry for the selected date already exists
                String checkSql = "SELECT COUNT(*) FROM Weight WHERE ID = ? AND DateAdded = ?";
                try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, LoginController.currentUserId);
                    checkStmt.setDate(2, Date.valueOf(weightDatePicker.getValue()));
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) { // No entry for this date exists, so insert new one
                        String sql = "INSERT INTO Weight (ID, Pounds, DateAdded) VALUES (?, ?, ?)";
                        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                            pstmt.setInt(1, LoginController.currentUserId);
                            pstmt.setDouble(2, pounds);
                            pstmt.setDate(3, Date.valueOf(weightDatePicker.getValue()));
                            pstmt.executeUpdate();
                            loadWeightData(weightRangeComboBox.getValue()); // Refresh chart
                        }
                    } else {
                        // Provide user feedback about the existing entry
                    }
                }
            } catch (NumberFormatException e) {
                // Provide user feedback about invalid number format
            } catch (SQLException e) {
                // Provide user feedback about database error
            }
        }
    }
    private void loadWeightData(String range) {
        weightLineChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = getStartDateBasedOnRange(range, endDate);

        List<XYChart.Data<String, Number>> chartData = new ArrayList<>();
        double minWeight = Double.MAX_VALUE;
        double maxWeight = -Double.MAX_VALUE;

        String sql = "SELECT Pounds, DateAdded FROM Weight WHERE ID = ? AND DateAdded BETWEEN ? AND ? ORDER BY DateAdded ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, LoginController.currentUserId);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                LocalDate date = rs.getDate("DateAdded").toLocalDate();
                double weight = rs.getDouble("Pounds");
                chartData.add(new XYChart.Data<>(date.format(DateTimeFormatter.ofPattern("MM/dd")), weight));
                minWeight = Math.min(minWeight, weight);
                maxWeight = Math.max(maxWeight, weight);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle SQL exceptions here
        }

        if (chartData.isEmpty()) {
            series.getData().add(new XYChart.Data<>("No Data", 0)); // Placeholder for no data
        } else {
            series.getData().addAll(chartData);
            weightLineChart.getData().add(series);

            // Optionally configure axis limits based on minWeight and maxWeight
            NumberAxis yAxis = (NumberAxis) weightLineChart.getYAxis();
            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(minWeight - 5);  // Provide some padding around min and max values
            yAxis.setUpperBound(maxWeight + 5);
        }
    }


    private LocalDate getStartDateBasedOnRange(String range, LocalDate endDate) {
        switch (range) {
            case "3 Months":
                return endDate.minusMonths(3);
            case "6 Months":
                return endDate.minusMonths(6);
            case "12 Months":
                return endDate.minusMonths(12);
            default:
                return endDate.minusMonths(1); // Default to 1 month
        }
    }


    private void configureComboBox() {
        weightRangeComboBox.setItems(FXCollections.observableArrayList("1 Month", "3 Months", "6 Months", "12 Months").sorted());
        weightRangeComboBox.getSelectionModel().selectFirst();
    }
    @FXML
    private void handleDatePickAction() {
        // Logic to be executed when a date is picked, if any.
        LocalDate selectedDate = weightDatePicker.getValue();
        // Do something with the selected date...
    }
    @FXML
    private void handleAddWeightAction() {
        addWeight(); // This calls your existing method to add weight
    }

    @FXML
    private void handleWeightRangeAction() {
        String selectedRange = weightRangeComboBox.getValue();
        loadWeightData(selectedRange); // Load the chart data based on the selected range
    }
    @FXML
    private void calculateBMI() {
        try {
            double weightInPounds = Double.parseDouble(weightTextfield.getText());
            double weightInKg = weightInPounds * 0.453592;
            int feet = Integer.parseInt(feetTextfield.getText());
            int inches = Integer.parseInt(inchesTextField.getText());
            double heightInMeters = feet * 0.3048 + inches * 0.0254;

            double bmi = weightInKg / (heightInMeters * heightInMeters);
            bmilbl.setText(String.format("%.2f", bmi));
        } catch (NumberFormatException e) {
            bmilbl.setText("Please enter valid numbers.");
        }
    }

    private void calculateCalories() {
        // Ensure you have fields for age, weight, height (feet and inches), gender, and activity level
        int age = Integer.parseInt(calorieageTextField.getText());
        int weight = Integer.parseInt(calorieWeightTextField.getText());
        int heightFeet = Integer.parseInt(caloriefeetTextfield.getText());
        int heightInches = Integer.parseInt(calorieinchesTextField.getText());

        // Convert height to cm and weight to kg
        int heightInCm = (int) ((heightFeet * 12 + heightInches) * 2.54);
        double weightInKg = weight / 2.20462;

        // Calculate BMR
        double bmr = (maleRadio.isSelected())
                ? 88.362 + (13.397 * weightInKg) + (4.799 * heightInCm) - (5.677 * age)
                : 447.593 + (9.247 * weightInKg) + (3.098 * heightInCm) - (4.330 * age);

        // Get activity multiplier
        double activityMultiplier = getActivityMultiplier(activityComboBox.getValue());

        // Calculate maintenance calories
        double maintenanceCalories = bmr * activityMultiplier;

        // Calculate calories for weight loss goals
        double mildWeightLossCalories = maintenanceCalories - 250;  // 0.5 lb/week
        double weightLossCalories = maintenanceCalories - 500;      // 1 lb/week
        double extremeWeightLossCalories = maintenanceCalories - 1000; // 2 lb/week

        // Update the results grid
        updateResultsGrid(maintenanceCalories, mildWeightLossCalories, weightLossCalories, extremeWeightLossCalories);
    }

    private double getActivityMultiplier(String activityLevel) {
        switch (activityLevel) {
            case "Sedentary": return 1.2;
            case "Lightly active": return 1.375;
            case "Moderately active": return 1.55;
            case "Very active": return 1.725;
            case "Super active": return 1.9;
            default: return 1; // Default case, should not occur
        }
    }

    private void updateResultsGrid(double maintenance, double mildLoss, double loss, double extremeLoss) {
        calorieResultsGrid.getChildren().clear(); // Clear previous results

        calorieResultsGrid.add(new Label("Maintain weight"), 0, 0);
        calorieResultsGrid.add(new Label(String.format("%.0f Calories/day 100%%", maintenance)), 1, 0);

        calorieResultsGrid.add(new Label("Mild weight loss\n 0.5 lb/week"), 0, 1);
        calorieResultsGrid.add(new Label(String.format("%.0f Calories/day 91%%", mildLoss)), 1, 1);

        calorieResultsGrid.add(new Label("Weight loss\n 1 lb/week"), 0, 2);
        calorieResultsGrid.add(new Label(String.format("%.0f Calories/day 81%%", loss)), 1, 2);

        calorieResultsGrid.add(new Label("Extreme weight loss\n 2 lb/week"), 0, 3);
        calorieResultsGrid.add(new Label(String.format("%.0f Calories/day 62%%", extremeLoss)), 1, 3);
    }

}
