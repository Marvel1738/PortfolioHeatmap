package com.PortfolioHeatmap.models;

/**
 * Represents the response structure for a historical price request from the Financial Modeling Prep (FMP) API.
 * This class deserializes the JSON response into a list of historical price entries for a specific symbol.
 * 
 * @author [Your Name]
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FMPHistoricalPriceResponse {
    // The stock symbol, mapped to the "symbol" JSON field.
    @JsonProperty("symbol")
    private String symbol;
    // List of historical price entries, mapped to the "historical" JSON field.
    @JsonProperty("historical")
    private List<HistoricalEntry> historical;

    // Getter for the stock symbol.
    public String getSymbol() {
        return symbol;
    }

    // Setter for the stock symbol.
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    // Getter for the list of historical price entries.
    public List<HistoricalEntry> getHistorical() {
        return historical;
    }

    // Setter for the list of historical price entries.
    public void setHistorical(List<HistoricalEntry> historical) {
        this.historical = historical;
    }

    // Provides a string representation of the FMPHistoricalPriceResponse object for
    // logging or debugging.
    @Override
    public String toString() {
        return "FMPHistoricalPriceResponse[symbol=" + symbol + ", historical=" + historical + "]";
    }

    // Nested class representing a single historical price entry in the response.
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HistoricalEntry {
        // The date of the historical price entry, mapped to the "date" JSON field.
        @JsonProperty("date")
        private LocalDate date;
        // The closing price on that date, mapped to the "close" JSON field.
        @JsonProperty("close")
        private double close;

        // Getter for the date.
        public LocalDate getDate() {
            return date;
        }

        // Setter for the date.
        public void setDate(LocalDate date) {
            this.date = date;
        }

        // Getter for the closing price.
        public double getClose() {
            return close;
        }

        // Setter for the closing price.
        public void setClose(double close) {
            this.close = close;
        }

        // Provides a string representation of the HistoricalEntry object for logging or
        // debugging.
        @Override
        public String toString() {
            return "HistoricalEntry[date=" + date + ", close=" + close + "]";
        }
    }
}