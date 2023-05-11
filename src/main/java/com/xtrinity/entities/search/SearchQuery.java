package com.xtrinity.entities.search;

import java.util.List;

public class SearchQuery {
    private List<SearchFilter> filters;
    // Line contains positions and operations for filters e.g. (0 && 1) || (2 && 3)
    private String filtersIndexString;
    private String title;

    public SearchQuery(String title) {
        this.title = title;
    }

    public SearchQuery(String title, List<SearchFilter> filters, String filtersIndexString) {
        this(title);
        this.filters = filters;
        this.filtersIndexString = filtersIndexString;
    }

    public List<SearchFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<SearchFilter> filters) {
        this.filters = filters;
    }

    public String getTitle() {
        return title;
    }

    public String getFiltersIndexString() {
        return filtersIndexString;
    }

    public void setFiltersIndexString(String filtersIndexString) {
        this.filtersIndexString = filtersIndexString;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
