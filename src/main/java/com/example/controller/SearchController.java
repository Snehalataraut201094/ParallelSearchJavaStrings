package com.example.controller;

import com.example.service.ParallelSearchService;
import com.example.service.SearchService;
import javafx.fxml.FXML;
import com.example.data.DataLoader;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    private static final String DATA_LOAD_SUCCESS_MESSAGE = "Loaded %d strings";
    private static final String DATA_LOAD_FAILURE_MESSAGE = "Failed to load data: %s";
    private static final String SEARCH_SERVICE_UNAVAILABLE_MESSAGE = "Search service unavailable";
    private static final String SEARCH_FAILED_MESSAGE = "Search failed";

    @FXML
    private Label statusLabel;

    @FXML
    private TextField searchField;

    @FXML
    private ListView<String> resultList;

    private SearchService searchService;

    /**
     * Initialize the controller by loading data and setting up the search service.
     * Loads strings from a CSV file and initializes the ParallelSearchService with the loaded data.
     * Updates the label to show how many strings were loaded or an error message if loading fails.
     * This method is called automatically by JavaFX after the FXML components are injected.
     */
    @FXML
    public void initialize() {
        try {
            List<String> stringData = DataLoader.loadStringsFromCSV("src/main/resources/strings.csv");
            searchService = new ParallelSearchService(stringData);
            statusLabel.setText(String.format(DATA_LOAD_SUCCESS_MESSAGE, stringData.size()));
        } catch (Exception e) {
            statusLabel.setText(String.format(DATA_LOAD_FAILURE_MESSAGE, e.getMessage()));
        }
    }

    /**
     * Handle the search action when the user clicks the search button.
     * Performs the search in a background thread using a JavaFX Task to avoid blocking the UI.
     * Updates the label with the search results and time taken once the search is complete.
     * If the search service is unavailable, it updates the label with an error message.
     * This method is called when the user clicks the search button, as defined in the FXML file.
     * The search query is taken from the text field, and the results are displayed in the list view.
     * The search is performed asynchronously, and the UI is updated on the JavaFX Application Thread
     * when the search completes or fails.
     * Note: The actual search logic is implemented in the ParallelSearchService, which is not shown here.
     */
    @FXML
    private void handleSearch() {

        if (searchService == null) {
            statusLabel.setText(SEARCH_SERVICE_UNAVAILABLE_MESSAGE);
            return;
        }
        Task<List<String>> task = createSearchTask();
        statusLabel.textProperty().bind(task.messageProperty());

        task.setOnSucceeded(succeedEvent -> handleSearchSuccess(task));
        task.setOnFailed(failedEvent -> handleSearchFailure(task));

        new Thread(task).start();
    }

    private Task<List<String>> createSearchTask() {
        return new Task<>() {
            @Override
            protected List<String> call() {
                long start = System.currentTimeMillis();
                String query = searchField.getText();
                List<String> results = searchService.search(query);
                long end = System.currentTimeMillis();
                updateMessage("Found " + results.size() + " results in " + (end - start) + " ms");
                return results;
            }
        };
    }

    private void handleSearchSuccess(Task<List<String>> task) {
        List<String> results = task.getValue();
        resultList.getItems().clear();
        if (results != null && !results.isEmpty()) {
            resultList.getItems().addAll(results);
        }
        statusLabel.textProperty().unbind();
        statusLabel.setText(task.getMessage());
    }

    private void handleSearchFailure(Task<List<String>> task) {
        statusLabel.textProperty().unbind();
        Throwable ex = task.getException();
        if (ex != null) {
            logger.error("Search task failed :{}", ex.getMessage());
        }
        statusLabel.setText(SEARCH_FAILED_MESSAGE);
    }
}