package com.xtrinity.entities.search;

import com.xtrinity.entities.airport.Airport;

import java.util.List;

public class SearchResult {
    List<Airport> airports;

    public SearchResult() {
        this.airports = null;
    }

    public SearchResult(List<Airport> airports) {
        this.airports = airports;
    }

    public List<Airport> getAirports() {
        return airports;
    }

    public void setAirports(List<Airport> airports) {
        this.airports = airports;
    }

    public void addAirport(Airport airport) {
        this.airports.add(airport);
    }
}
