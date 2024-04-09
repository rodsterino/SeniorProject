package com.example.nutritiontracker;

public class FoodMacro {

    private String foodItem;
    private double weight;
    private double calories;
    private double protein;
    private double fat;
    private double carbs;
    private String mealTime;
    private String dateAdded;


    public FoodMacro(String foodItem,double weight, double calories, double protein, double fat, double carbs,String mealTime,String dateAdded) {
        super();
        this.weight = weight;
        this.foodItem = foodItem;
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
        this.mealTime = mealTime;
        this.dateAdded= dateAdded;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getFoodItem() {
        return foodItem;
    }

    public void setFoodItem(String foodItem) {
        this.foodItem = foodItem;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public String getMealTime() {
        return mealTime;
    }

    public void setMealTime(String mealTime) {
        this.mealTime = mealTime;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    @Override
    public String toString() {
        return String.format("FoodItem: %s\tWeight: %.2fg\tCalories: %.2f\tProtein: %.2fg\tFat: %.2fg\tCarbs: %.2fg",
                foodItem, weight, calories, protein, fat, carbs);
    }


}
