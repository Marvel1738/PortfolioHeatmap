package com.PortfolioHeatmap.models;

/**
 * Represents the response structure for a historical price request from the Alpha Vantage API.
 * This class deserializes the JSON response into a map of daily price entries for a specific symbol.
 * 
 * @author [Your Name]
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlphaVantageHistoricalPriceResponse {
    // The time series data, mapped to the "Time Series (Daily)" JSON field.
    @JsonProperty("Time Series (Daily)")
    private Map<LocalDate, DailyPrice> timeSeries;

    // Getter for the time series data.
    public Map<LocalDate, DailyPrice> getTimeSeries() {
        return timeSeries;
    }

    // Setter for the time series data.
    public void setTimeSeries(Map<LocalDate, DailyPrice> timeSeries) {
        this.timeSeries = timeSeries;
    }

    // Provides a string representation of the AlphaVantageHistoricalPriceResponse
    // object for logging or debugging.
    @Override
    public String toString() {
        return "AlphaVantageHistoricalPriceResponse[timeSeries=" + timeSeries + "]";
    }

    // Nested class representing a single daily price entry in the response.
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DailyPrice {
        // The closing price on that date, mapped to the "4. close" JSON field.
        @JsonProperty("4. close")
        private String close;

        // Getter for the closing price.
        public String getClose() {
            return close;
        }

        // Setter for the closing price.
        public void setClose(String close) {
            this.close = close;
        }

        // Provides a string representation of the DailyPrice object for logging or
        // debugging.
        @Override
        public String toString() {
            return "DailyPrice[close=" + close + "]";
        }
    }
}