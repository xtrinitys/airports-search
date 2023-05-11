package com.xtrinity;

import com.xtrinity.dto.UserInputDto;
import com.xtrinity.entities.search.SearchResult;
import com.xtrinity.exceptions.AppException;
import com.xtrinity.services.IOService;
import com.xtrinity.services.search.SearchService;

public class App {
    private final SearchService searchService;
    private final IOService ioService;

    public App(SearchService searchService, IOService ioService) {
        this.searchService = searchService;
        this.ioService = ioService;
    }

    public void run() {
        while (true) {
            try {
                UserInputDto userInput = ioService.getUserInput();

//                UserInput = "!quit"
                if (userInput == null) {
                    return;
                }

                SearchResult result = searchService.searchFor(userInput);

                printResults(result);

            } catch (AppException ex) {
                handleException(ex);
            }
        }
    }

    private void printResults(SearchResult result) {
        int totalRows = searchService.getLastSearchTotalRows();
        long searchDuration = searchService.getLastSearchTime();

        ioService.printSearchResults(result, searchDuration, totalRows);
    }

    private void handleException(Exception ex) {
        System.out.println("Error: " + ex.getMessage());
        ioService.printLine();
    }
}
