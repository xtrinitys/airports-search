package com.xtrinity.services.search;

import com.xtrinity.Utils;
import com.xtrinity.dto.UserInputDto;
import com.xtrinity.entities.airport.Airport;
import com.xtrinity.entities.airport.AirportApi;
import com.xtrinity.entities.search.SearchFilter;
import com.xtrinity.entities.search.SearchQuery;
import com.xtrinity.exceptions.WrongFilterSyntaxException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchParserService {
    private final static String STRING_FILTER_REGEX = "(\\(*column\\[([1-9]\\d*)](<|=|>|<>)(?:(\\d+)|('\\w+'))(?:&|\\|\\|)*\\)*(?:&|\\|\\|)*)+";
    private final static String SINGLE_FILTER_REGEX = "(?:column\\[([1-9]\\d*)](<|=|>|<>)(?:(\\d+)|('\\w+')))+?";

    public SearchQuery parseQuery(UserInputDto userInput) throws WrongFilterSyntaxException {
        SearchQuery query = new SearchQuery(userInput.getRawTitle());

        if (!userInput.getRawFilters().isBlank()) {
            List<SearchFilter> filters = this.parseFilters(
                    userInput.getRawFilters()
            );
            String filtersIndexString = this.getIndexedFilters(
                    userInput.getRawFilters()
            );

            query.setFiltersIndexString(filtersIndexString);
            query.setFilters(filters);
        }

        return query;
    }

    public Airport parseAirport(String rawAirport) {
        Airport airport = new Airport();
        Method[] airportSetters = AirportApi.reachSetters();

        String[] columns = rawAirport.split(",(?=([^\"]|\"[^\"]*\")*$)");

        columns = Utils.cleanDoubleQuotes(columns);

        for (int i = 0; i < airportSetters.length; i++) {
            Method setter = airportSetters[i];
            Class<?> setterParameterType = setter.getParameterTypes()[0];

            String strColumn = columns[i];

            Number numColumn = null;

            if (Utils.isNumeric(strColumn)) {
                numColumn = Utils.parseNumber(strColumn);
            }

            try {
                if (strColumn.equals("\\N")) {
                    setter.invoke(airport, (Object) null);
                } else if (numColumn == null) {
                    setter.invoke(airport, strColumn);
                } else {
//                    Determine if setter accepts int or double
                    if (setterParameterType.getName().equals(Integer.class.getName())) {
                        setter.invoke(airport, numColumn.intValue());
                    } else {
                        setter.invoke(airport, numColumn.doubleValue());
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        return airport;
    }

    private String getIndexedFilters(String rawFilters) {
        String formatted = rawFilters.replaceAll("&", "&&");

        Pattern singleFilter = Pattern.compile(SINGLE_FILTER_REGEX);
        Matcher matcher = singleFilter.matcher(formatted);

        AtomicInteger index = new AtomicInteger();

        return matcher.replaceAll(matchResult -> String.valueOf(index.getAndIncrement()));
    }

    private List<SearchFilter> parseFilters(String rawFilters) throws WrongFilterSyntaxException {
        if (!rawFilters.matches(STRING_FILTER_REGEX)) {
            throw new WrongFilterSyntaxException("Error: Filters must be like \\\"column[2]>10&column[5]=’GKA’\\\", try again");
        }

        rawFilters = formatRawFilters(rawFilters);

        String[] strFilters = rawFilters.split("&|\\|\\|");

        List<SearchFilter> list = new ArrayList<>();
        for (int i = 0; i < strFilters.length; i++) {
            String strFilter = strFilters[i];
            SearchFilter searchFilter = parseFilter(strFilter, i);
            list.add(searchFilter);
        }
        return list;
    }

    private String formatRawFilters(String rawFilters) {
        String formatted = rawFilters.strip();

        if (formatted.endsWith("&")) {
            formatted = formatted.substring(0, formatted.length() - 1);
        }

        return formatted;
    }

    private SearchFilter parseFilter(String strFilter, int index) throws WrongFilterSyntaxException {
        SearchFilter filter = new SearchFilter(index);

        Pattern singleFilter = Pattern.compile(SINGLE_FILTER_REGEX);
        Matcher matcher = singleFilter.matcher(strFilter);

        while (matcher.find()) {
            int column = Integer.parseInt(matcher.group(1));

            if (column > 14) {
                throw new WrongFilterSyntaxException("Error: Illegal column " + column);
            }
            String sign = matcher.group(2);
            String numStrValue = matcher.group(3);
            String strValue = matcher.group(4);

            Number numValue = null;
            if (numStrValue != null) {
                numValue = Utils.parseNumber(numStrValue);
                filter.setNumValue(numValue);
            } else {
                strValue = strValue.replaceAll("'", "");
                filter.setStrValue(strValue);
            }

            Class<?> fieldType = Airport.class.getDeclaredFields()[column - 1].getType();
            if (numStrValue != null && fieldType == String.class) {
                throw new WrongFilterSyntaxException(
                        "Inappropriate types of columns, \"" + numValue + "\" to String"
                );
            }
            if (numStrValue == null && (fieldType == Integer.class || fieldType == Double.class)) {
                throw new WrongFilterSyntaxException(
                        "Inappropriate types of columns, \"" + strValue + "\" to Number"

                );
            }

            filter.setColumn(column);
            filter.setSign(sign);
        }

        return filter;
    }
}
