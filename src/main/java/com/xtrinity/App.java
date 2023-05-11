package com.xtrinity;

import com.xtrinity.dto.UserInputDto;
import com.xtrinity.entities.search.SearchQuery;
import com.xtrinity.entities.search.SearchResult;
import com.xtrinity.exceptions.AppException;
import com.xtrinity.exceptions.WrongFilterSyntaxException;
import com.xtrinity.exceptions.WrongInputException;
import com.xtrinity.services.IOService;
import com.xtrinity.services.search.SearchParserService;
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

                if (userInput == null) {
                    return;
                }

                SearchQuery query = SearchParserService.parseQuery(userInput);

                SearchResult result = searchService.searchFor(query);

                int totalRows = searchService.getLastSearchTotalRows();
                long searchDuration = searchService.getLastSearchTime();

                ioService.printResults(result, searchDuration, totalRows);
            } catch (AppException ex) {
                handleException(ex);
            }
        }
    }

    private void handleException(Exception ex) {
        System.out.println("Error: " + ex.getMessage());
        ioService.printLine();
    }
}
