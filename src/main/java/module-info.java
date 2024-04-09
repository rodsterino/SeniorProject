module com.example.seniorproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires java.sql;
    opens com.example.nutritiontracker to javafx.fxml;
    exports com.example.nutritiontracker;
}