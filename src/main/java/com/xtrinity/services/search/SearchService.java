package com.xtrinity.services.search;

import com.xtrinity.dto.UserInputDto;
import com.xtrinity.entities.airport.Airport;
import com.xtrinity.entities.airport.AirportApi;
import com.xtrinity.entities.search.SearchQuery;
import com.xtrinity.entities.search.SearchResult;
import com.xtrinity.exceptions.WrongFilterSyntaxException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchService {
    long lastSearchTime;
    int lastSearchTotalRows;
    private final String FILE_PATH;

    private final FilterService filterService;
    private final SearchParserService parserService;

    public SearchService(String file_path) {
        this.FILE_PATH = file_path;

        this.filterService = new FilterService();
        this.parserService = new SearchParserService();

        lastSearchTime = -1;
    }

    public SearchResult searchFor(UserInputDto userInput) throws WrongFilterSyntaxException {
        SearchQuery query = parserService.parseQuery(userInput);

        SearchResult result = new SearchResult();
        List<Airport> airports;

        Instant startTime, endTime;
        try (
                Stream<String> fileStream = Files.lines(Path.of(FILE_PATH)).parallel()
        ) {
            Method[]
                    setters = AirportApi.reachSetters(),
                    getters = AirportApi.reachGetters();

            startTime = Instant.now();

            airports = fileStream
                    .filter(s -> filterService.applyTitleFilter(query.getTitle(), s))
                    .map((String rawAirport) -> parserService.parseAirport(rawAirport, setters))
                    .filter(airport -> filterService.applyFilters(query.getFilters(), query.getFiltersIndexString(), airport, getters))
                    .collect(Collectors.toList());

            endTime = Instant.now();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.lastSearchTime = Duration.between(startTime, endTime).toMillis();
        this.lastSearchTotalRows = airports.size();

        airports.sort(Comparator.comparing(Airport::getCol2));
        result.setAirports(airports);

        return result;
    }


    public long getLastSearchTime() {
        return lastSearchTime;
    }

    public int getLastSearchTotalRows() {
        return lastSearchTotalRows;
    }
}
