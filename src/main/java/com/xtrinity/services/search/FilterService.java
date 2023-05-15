package com.xtrinity.services.search;

import com.xtrinity.Utils;
import com.xtrinity.entities.airport.Airport;
import com.xtrinity.entities.search.SearchFilter;
import org.mvel2.MVEL;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class FilterService {
    public boolean applyFilters(List<SearchFilter> filters, String filtersIndexString, Airport airport, Method[] getters) {
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
}
