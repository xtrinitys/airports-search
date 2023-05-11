package com.xtrinity;

import com.xtrinity.dto.UserInputDto;
import com.xtrinity.entities.search.SearchQuery;
import com.xtrinity.entities.search.SearchResult;
import com.xtrinity.exceptions.WrongFilterSyntaxException;
import com.xtrinity.exceptions.WrongInputException;
import com.xtrinity.services.IOService;
import com.xtrinity.services.search.SearchParserService;
import com.xtrinity.services.search.SearchService;

import java.io.File;

public class Main {
    private static final String FILE_PATH = System.getProperty("user.dir") + "/src/main/resources/data.csv";

    public static void main(String[] args) {
        if (!Main.dataExists()) {
            throw new RuntimeException("File not exists!");
        }

        IOService io = new IOService();
        SearchService searchService = new SearchService(FILE_PATH);

        while (true) {
            UserInputDto userInput;
            try {
                userInput = io.getUserInput();
            } catch (WrongInputException e) {
                System.out.println("Error: " + e.getMessage());
                io.printLine();
                continue;
            }

            if (userInput == null) {
                return;
            }

            SearchQuery query;
            try {
                query = SearchParserService.parseQuery(userInput);
            } catch (WrongFilterSyntaxException ex) {
                System.out.println("Error: Filters must be like \"column[2]>10&column[5]=’GKA’\", try again");
                io.printLine();
                continue;
            }

            SearchResult result = searchService.searchFor(query);
            int totalRows = searchService.getLastSearchTotalRows();
            long searchDuration = searchService.getLastSearchTime();

            io.printResults(result, searchDuration, totalRows);
        }
    }

    private static boolean dataExists() {
        File f = new File(FILE_PATH);
        return f.exists() && !f.isDirectory();
    }
}
