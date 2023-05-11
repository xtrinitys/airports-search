package com.xtrinity.services;

import com.xtrinity.dto.UserInputDto;
import com.xtrinity.entities.Airport;
import com.xtrinity.entities.search.SearchResult;
import com.xtrinity.exceptions.WrongInputException;

import java.util.Scanner;

public class IOService {
    public UserInputDto getUserInput() throws WrongInputException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Filters: ");
        String rawFilters = scanner.nextLine();

        if (rawFilters.equals("!quit")) {
            return null;
        }

        System.out.print("Airport name: ");
        String rawTitle = scanner.nextLine();

        if (rawTitle.equals("!quit")) {
            return null;
        }

        if (rawTitle.isBlank()) {
            throw new WrongInputException("Provide airport name");
        }

        System.out.println("-------------");

        return new UserInputDto(rawFilters, rawTitle);
    }

    public void printResults(SearchResult result, long searchDuration, int totalRows) {
        Airport[] airports = result.getAirports().toArray(new Airport[0]);
        for (var airport : airports) {
            if (airport != null) {
                System.out.println(airport);
            }
        }

        printLine();
        System.out.printf("Total rows: %d; Search time: %d ms\n", totalRows, searchDuration);
        printLine();
    }

    public void printLine() {
        System.out.println("-------------");
    }
}
