package com.xtrinity.services.search;

import com.xtrinity.Utils;
import com.xtrinity.entities.Airport;
import com.xtrinity.entities.search.SearchFilter;
import com.xtrinity.entities.search.SearchQuery;
import com.xtrinity.entities.search.SearchResult;
import org.mvel2.MVEL;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchService {
    long lastSearchTime;
    int lastSearchTotalRows;
    private final String FILE_PATH;

    public SearchService(String file_path) {
        this.FILE_PATH = file_path;
        lastSearchTime = -1;
    }

    public SearchResult searchFor(SearchQuery query) {
        SearchResult result = new SearchResult();

        List<Airport> airports;

        Instant startTime, endTime;
        try (
                Stream<String> fileStream = Files.lines(Path.of(FILE_PATH)).parallel()
        ) {
            startTime = Instant.now();

            airports = fileStream
                    .filter(s -> applyTitleFilter(query.getTitle(), s))
                    .map(this::parseAirport)
                    .filter(airport -> applyFilters(query.getFilters(), query.getFiltersIndexString(), airport))
                    .collect(Collectors.toList());

            endTime = Instant.now();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.lastSearchTime = Duration.between(startTime, endTime).toMillis();
        this.lastSearchTotalRows = airports.size();
        result.setAirports(airports);

//        TODO: Bower 30ms
//        try {
//            InputStream inputStream = new FileInputStream(FILE_PATH);
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//
//            Stream<String> linesStream = bufferedReader.lines();
//
//            Instant startTime = Instant.now();
//            linesStream
//                    .filter(s -> s.toLowerCase().contains(query.getTitle().toLowerCase()))
//                    .forEach(rawLine -> {
//                if (applyTitleFilter(query.getTitle(), rawLine)) {
//                   airports.add(this.parseAirport(rawLine));
//                }
//            });
//
//            this.lastSearchTime = Duration.between(startTime, Instant.now()).toMillis();
//            this.lastSearchTotalRows = airports.size();
//            result.setAirports(airports);
//
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }

        return result;
    }

    private boolean applyFilters(List<SearchFilter> filters, String filtersIndexString, Airport airport) {
        if (filters == null) {
            return true;
        }

        Boolean[] filtersResults = filters
                .stream()
                .map(filter -> applyFilter(filter, airport))
                .toArray(Boolean[]::new);

        String resultLogicalExpression = filtersIndexString;

        for (int i = 0; i < filtersResults.length; i++) {
            boolean result = filtersResults[i];

            resultLogicalExpression =
                    resultLogicalExpression.replaceAll(String.valueOf(i), String.valueOf(result));
        }

        return (boolean) MVEL.eval(resultLogicalExpression);
    }

    private boolean applyFilter(SearchFilter filter, Airport airport) {
        Method[] airportGetters =
            Arrays.stream(Airport.class.getDeclaredMethods())
                    .filter(m -> m.getName().startsWith("get"))
                    .toArray(Method[]::new);

        int column = filter.getColumn() - 1;
        try {
            Method getter = airportGetters[column];
            Class<?> fieldType = getter.getReturnType();
            String sign = filter.getSign();

            if (fieldType == Integer.class) {
                Integer fieldValue = (Integer) getter.invoke(airport);
                Integer filterValue = filter.getNumValue().intValue();

                return Utils.evaluateBySign(fieldValue, filterValue, sign);
            } else if (fieldType == Double.class) {
                Double fieldValue = (Double) getter.invoke(airport);
                Double filterValue = filter.getNumValue().doubleValue();

                return Utils.evaluateBySign(fieldValue, filterValue, sign);
            } else if (fieldType == String.class) {
                String fieldValue = (String) getter.invoke(airport);
                String filterValue = filter.getStrValue();

                return Utils.evaluateBySign(fieldValue, filterValue, sign);
            } else {
                return false;
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Airport parseAirport(String rawAirport) {
        Airport airport = new Airport();

        String[] columns = rawAirport.split(",(?=([^\"]|\"[^\"]*\")*$)");

        columns = cleanDoubleQuotes(columns);

        Method[] airportSetters =
                Arrays.stream(Airport.class.getDeclaredMethods())
                        .filter(m -> m.getName().startsWith("set"))
                        .toArray(Method[]::new);

        for (int i = 0; i < airportSetters.length; i++) {
            String strColumn = columns[i];

            Number numColumn = null;

            if (Utils.isNumeric(strColumn)) {
                numColumn = Utils.parseNumber(strColumn);
            }

            try {
                if (strColumn.equals("\\N")) {
                    airportSetters[i].invoke(airport, (Object) null);
                } else if (numColumn == null) {
//                TODO: Still sometimes throws IllegalArgumentException for example on "a" idk
                    airportSetters[i].invoke(airport, strColumn);
                } else {
//                    Determine if setter accepts int or double
                    Class<?> setterNumberType = airportSetters[i].getParameterTypes()[0];
                    if (setterNumberType.getName().equals(Integer.class.getName())) {
                        airportSetters[i].invoke(airport, numColumn.intValue());
                    } else {
                        airportSetters[i].invoke(airport, numColumn.doubleValue());
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        return airport;
    }

    private static String[] cleanDoubleQuotes(String[] strings) {
        return Arrays.stream(strings).map(s -> {
            if (s.startsWith("\"") && s.endsWith("\"")) {
                return s.substring(1, s.length() - 1);
            }
            return s;
        }).toArray(String[]::new);
    }

    private boolean applyTitleFilter(String queryTitle, String rawAirport) {
        Matcher matcher = Pattern.compile("\"(.+?)\"").matcher(rawAirport);
        matcher.find();
        return matcher.group(1).toLowerCase().startsWith(queryTitle.toLowerCase());
    }

    public long getLastSearchTime() {
        return lastSearchTime;
    }

    public int getLastSearchTotalRows() {
        return lastSearchTotalRows;
    }
}
