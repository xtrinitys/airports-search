package com.xtrinity.dto;

import com.xtrinity.entities.search.SearchFilter;

import java.util.List;

public class FiltersDto {
    private List<SearchFilter> filters;
    private String filtersIndexString;

    public FiltersDto(List<SearchFilter> filters, String filtersIndexString) {
        this.filters = filters;
        this.filtersIndexString = filtersIndexString;
    }

    public List<SearchFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<SearchFilter> filters) {
        this.filters = filters;
    }

    public String getFiltersIndexString() {
        return filtersIndexString;
    }

    public void setFiltersIndexString(String filtersIndexString) {
        this.filtersIndexString = filtersIndexString;
    }
}
