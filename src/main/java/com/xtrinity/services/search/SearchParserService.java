package com.xtrinity.services.search;

import com.xtrinity.Utils;
import com.xtrinity.dto.UserInputDto;
import com.xtrinity.entities.search.SearchFilter;
import com.xtrinity.entities.search.SearchQuery;
import com.xtrinity.exceptions.WrongFilterSyntaxException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchParserService {
    private final static String STRING_FILTER_REGEX = "(\\(*column\\[([1-9]\\d*)](<|=|>|<>)(?:(\\d+)|('\\w+'))(?:&|\\|\\|)*\\)*(?:&|\\|\\|)*)+";
    private final static String SINGLE_FILTER_REGEX = "(?:column\\[([1-9]\\d*)](<|=|>|<>)(?:(\\d+)|('\\w+')))+?";

    public static SearchQuery parseQuery(UserInputDto userInput) throws WrongFilterSyntaxException {
        SearchQuery query = new SearchQuery(userInput.getRawTitle());

        if (!userInput.getRawFilters().isBlank()) {
            List<SearchFilter> filters = SearchParserService.parseFilters(
                    userInput.getRawFilters()
            );
            String filtersIndexString = SearchParserService.getIndexedFilters(
                    userInput.getRawFilters()
            );

            query.setFiltersIndexString(filtersIndexString);
            query.setFilters(filters);
        }

        return query;
    }

    private static String getIndexedFilters(String rawFilters) {
        String formatted = rawFilters.replaceAll("&", "&&");

        Pattern singleFilter = Pattern.compile(SINGLE_FILTER_REGEX);
        Matcher matcher = singleFilter.matcher(formatted);

        AtomicInteger index = new AtomicInteger();

        return matcher.replaceAll(matchResult -> String.valueOf(index.getAndIncrement()));
    }

    private static List<SearchFilter> parseFilters(String rawFilters) throws WrongFilterSyntaxException {
        if (!rawFilters.matches(STRING_FILTER_REGEX)) {
            throw new WrongFilterSyntaxException();
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

    private static String formatRawFilters(String rawFilters) {
        String formatted = rawFilters.strip();

        if (formatted.endsWith("&")) {
            formatted = formatted.substring(0, formatted.length() - 1);
        }


        return formatted;
    }

    private static SearchFilter parseFilter(String strFilter, int index) {
        SearchFilter filter = new SearchFilter(index);

        Pattern singleFilter = Pattern.compile(SINGLE_FILTER_REGEX);
        Matcher matcher = singleFilter.matcher(strFilter);

        while (matcher.find()) {
            int column = Integer.parseInt(matcher.group(1));
            String sign = matcher.group(2);
            String numStrValue = matcher.group(3);
            String strValue = matcher.group(4);

            Number numValue;
            if (numStrValue != null) {
                numValue = Utils.parseNumber(numStrValue);
                filter.setNumValue(numValue);
            } else {
                strValue = strValue.replaceAll("'", "");
                filter.setStrValue(strValue);
            }

            filter.setColumn(column);
            filter.setSign(sign);
        }

        return filter;
    }
}