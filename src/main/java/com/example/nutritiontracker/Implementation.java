package com.example.nutritiontracker;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;
import java.util.Vector;

public class Implementation {

    public static Connection connection;
    Statement statement;
    ResultSet resultSet;
    Scanner keyboard;
    static Vector<User> users;
    int loggedinuser;
    public Implementation() {
        // Initialize the DS for users
        users = new Vector<>();
        keyboard = new Scanner(System.in);

    }
    public void initialize() throws Exception {
        connectDB();
        loadUsers();
    }
    private void loadUsers() {

        try {
            String query = "SELECT * FROM User";
            ResultSet resultSet;
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                users.add(new User(loggedinuser, resultSet.getString("FirstName"), resultSet.getString("LastName"),
                        resultSet.getString("Email"), resultSet.getString("Password")
                        ));

//				i++;
            }
            resultSet.close();
            Thread.sleep(250);

            System.out.println("Users Loaded...");
            Thread.sleep(500);
        } catch (Exception e) {

        }
    }
    void connectDB() {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            String msAccDB = "NutritionTracker.accdb";
            String dbURL = "jdbc:ucanaccess://" + msAccDB;
            connection = DriverManager.getConnection(dbURL);
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            System.out.println("Database Connected...");
            Thread.sleep(300);
        } catch (Exception e) {
            System.out.println("Error!");
            e.printStackTrace();
        }
    }

}
