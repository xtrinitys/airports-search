package com.xtrinity.services.search;

import com.xtrinity.Utils;
import com.xtrinity.entities.airport.Airport;
import com.xtrinity.entities.airport.AirportApi;
import com.xtrinity.dto.FiltersDto;
import com.xtrinity.entities.search.SearchFilter;
import org.mvel2.MVEL;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterService {
    public boolean applyFilters(FiltersDto filtersDto, Airport airport) {
        List<SearchFilter> filters = filtersDto.getFilters();
        String filtersIndexString = filtersDto.getFiltersIndexString();

        Method[] getters = AirportApi.reachGetters();

        if (filters == null) {
            return true;
        }

        Boolean[] filtersResults = filters
                .stream()
                .map(filter -> applyFilter(filter, airport, getters))
                .toArray(Boolean[]::new);

        String resultStatement = filtersIndexString;

        for (int i = 0; i < filtersResults.length; i++) {
            boolean result = filtersResults[i];

            resultStatement = resultStatement.replaceAll(String.valueOf(i), String.valueOf(result));
        }

        return (boolean) MVEL.eval(resultStatement);
    }

    private boolean applyFilter(SearchFilter filter, Airport airport, Method[] airportGetters) {
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

    public boolean applyTitleFilter(String queryTitle, String rawAirport) {
        Matcher matcher = Pattern.compile("\"(.+?)\"").matcher(rawAirport);

        //noinspection ResultOfMethodCallIgnored
        matcher.find();

        return matcher.group(1).toLowerCase().startsWith(queryTitle.toLowerCase());
    }
}
