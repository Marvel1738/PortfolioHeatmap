package com.PortfolioHeatmap.models;

/**
 * Represents the response structure for a single stock quote request from the Alpha Vantage API.
 * This class deserializes the JSON response into a global quote object, used by the application
 * to fetch and process stock price data for a specific symbol.
 * 
 * @author [Marvel Bana]
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AlphaVantageQuoteResponse {
    // The global quote object returned by the Alpha Vantage API, mapped to the
    // "Global Quote" JSON field.
    @JsonProperty("Global Quote")
    private GlobalQuote globalQuote;

    // Getter for the global quote object.
    public GlobalQuote getGlobalQuote() {
        return globalQuote;
    }

    // Setter for the global quote object.
    public void setGlobalQuote(GlobalQuote globalQuote) {
        this.globalQuote = globalQuote;
    }

    // Provides a string representation of the AlphaVantageQuoteResponse object for
    // logging or debugging.
    @Override
    public String toString() {
        return "AlphaVantageQuoteResponse[globalQuote=" + globalQuote + "]";
    }

    // Nested class representing a single global quote in the response.
    // Ignores unknown properties in the JSON response to prevent deserialization
    // errors.
    @JsonIgnoreProperties(ignoreUnknown = true) // Ignore extra fields
    public static class GlobalQuote {
        // Stock symbol, mapped to the "01. symbol" JSON field.
        @JsonProperty("01. symbol")
        private String symbol;
        // Current price, mapped to the "05. price" JSON field.
        @JsonProperty("05. price")
        private String price;
        // Opening price, mapped to the "02. open" JSON field.
        @JsonProperty("02. open")
        private String open;
        // Highest price of the day, mapped to the "03. high" JSON field.
        @JsonProperty("03. high")
        private String high;
        // Lowest price of the day, mapped to the "04. low" JSON field.
        @JsonProperty("04. low")
        private String low;
        // Previous day's closing price, mapped to the "08. previous close" JSON field.
        @JsonProperty("08. previous close")
        private String previousClose;

        // Getter for the stock symbol.
        public String getSymbol() {
            return symbol;
        }

        // Getter for the current price.
        public String getPrice() {
            return price;
        }

        // Getter for the opening price.
        public String getOpen() {
            return open;
        }

        // Getter for the highest price of the day.
        public String getHigh() {
            return high;
        }

        // Getter for the lowest price of the day.
        public String getLow() {
            return low;
        }

        // Getter for the previous day's closing price.
        public String getPreviousClose() {
            return previousClose;
        }

        // Setter for the stock symbol.
        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        // Setter for the current price.
        public void setPrice(String price) {
            this.price = price;
        }

        // Setter for the opening price.
        public void setOpen(String open) {
            this.open = open;
        }

        // Setter for the highest price of the day.
        public void setHigh(String high) {
            this.high = high;
        }

        // Setter for the lowest price of the day.
        public void setLow(String low) {
            this.low = low;
        }

        // Setter for the previous day's closing price.
        public void setPreviousClose(String previousClose) {
            this.previousClose = previousClose;
        }

        // Provides a string representation of the GlobalQuote object for logging or
        // debugging.
        // Simplified to avoid excessive verbosity in the output.
        @Override
        public String toString() {
            return "GlobalQuote[symbol=" + symbol + ", price=" + price + ", ...]";
        }
    }
}