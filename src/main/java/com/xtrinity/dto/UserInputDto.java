package com.xtrinity.dto;

public class UserInputDto {
    private String rawFilters;
    private String rawTitle;

    public UserInputDto(String rawFilters, String rawTitle) {
        this.rawFilters = rawFilters;
        this.rawTitle = rawTitle;
    }

    public String getRawFilters() {
        return rawFilters;
    }

    public void setRawFilters(String rawFilters) {
        this.rawFilters = rawFilters;
    }

    public String getRawTitle() {
        return rawTitle;
    }

    public void setRawTitle(String rawTitle) {
        this.rawTitle = rawTitle;
    }
}
