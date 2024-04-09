package com.example.nutritiontracker;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeController {

    @FXML
    private ListView<String> ingredientsListView;
    @FXML
    private ListView<String> macroListView;
    @FXML
    private ListView<String> recipeListView;
    @FXML
    private TextField searchTextField;

    private final String appId = "3d89810f";
    private final String appKey = "f7fd4eaaa92edc69f339f796ccdf0217";
    private final String recipeSearchUrl = "https://api.edamam.com/search";
    private final Map<String, String> recipeUriMap = new HashMap<>();

    @FXML
    public void initialize() {
        recipeListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, recipeTitle) -> {
            if (recipeTitle != null) {
                fetchRecipeDetails(recipeUriMap.get(recipeTitle));
            }
        });
    }

    @FXML
    private void onSearchButtonClick() {
        String query = searchTextField.getText().trim();
        if (!query.isEmpty()) {
            searchRecipes(query);
        }
    }

    private void searchRecipes(String query) {
        new Thread(() -> {
            try {
                URL url = new URL(recipeSearchUrl + "?q=" + URLEncoder.encode(query, "UTF-8") + "&app_id=" + appId + "&app_key=" + appKey);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String response = readResponse(connection.getInputStream());
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray hits = jsonResponse.getJSONArray("hits");
                    List<String> recipeTitles = new ArrayList<>();
                    recipeUriMap.clear();
                    for (int i = 0; i < hits.length(); i++) {
                        JSONObject recipe = hits.getJSONObject(i).getJSONObject("recipe");
                        String title = recipe.getString("label");
                        String uri = recipe.getString("uri");
                        recipeTitles.add(title);
                        recipeUriMap.put(title, uri);
                    }
                    Platform.runLater(() -> recipeListView.getItems().setAll(recipeTitles));
                } else {
                    System.out.println("Search request failed with response code: " + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void fetchRecipeDetails(String uri) {
        if (uri != null && !uri.isEmpty()) {
            new Thread(() -> {
                try {

                    URL detailUrl = new URL("https://api.edamam.com/api/recipes/v2/" + uri.substring(uri.lastIndexOf("_") + 1) + "?type=public&app_id=" + appId + "&app_key=" + appKey);
                    HttpURLConnection detailConnection = (HttpURLConnection) detailUrl.openConnection();
                    detailConnection.setRequestMethod("GET");

                    int responseCode = detailConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        String response = readResponse(detailConnection.getInputStream());
                        JSONObject recipeDetail = new JSONObject(response).getJSONObject("recipe");

                        // Extract ingredients and display
                        JSONArray ingredients = recipeDetail.getJSONArray("ingredients");
                        List<String> ingredientList = new ArrayList<>();
                        for (int i = 0; i < ingredients.length(); i++) {
                            ingredientList.add(ingredients.getJSONObject(i).getString("text"));
                        }

                        // Extract nutrients and display
                        JSONObject nutrients = recipeDetail.getJSONObject("totalNutrients");
                        List<String> nutrientList = new ArrayList<>();
                        if (nutrients.has("ENERC_KCAL")) {
                            nutrientList.add("Calories: " + nutrients.getJSONObject("ENERC_KCAL").getDouble("quantity") + " kcal");
                            nutrientList.add("Protein: " + nutrients.getJSONObject("PROCNT").getDouble("quantity") + " g");
                            nutrientList.add("Fat: " + nutrients.getJSONObject("FAT").getDouble("quantity") + " g");
                            nutrientList.add("Carbs: " + nutrients.getJSONObject("CHOCDF").getDouble("quantity") + " g");
                        }


                        Platform.runLater(() -> {
                            ingredientsListView.getItems().setAll(ingredientList);
                            macroListView.getItems().setAll(nutrientList);
                        });
                    } else {
                        System.out.println("Failed to fetch recipe details with response code: " + responseCode);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            System.out.println("Invalid URI for fetching recipe details.");
        }
    }

    private String readResponse(InputStream stream) throws Exception {
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(stream))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }
        return response.toString();
    }
}
