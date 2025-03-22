package com.PortfolioHeatmap.models;

/**
 * Represents the response structure for batch stock price requests from the Alpha Vantage API.
 * This class deserializes the JSON response into a list of stock quotes, used by the application
 * to fetch and process batch stock prices.
 * 
 * @author [Marvel Bana]
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AlphaVantageBatchResponse {
    // List of stock quotes returned by the Alpha Vantage API, mapped to the "Stock
    // Quotes" JSON field.
    @JsonProperty("Stock Quotes")
    private List<StockQuote> stockQuotes;

    // Getter for the list of stock quotes.
    public List<StockQuote> getStockQuotes() {
        return stockQuotes;
    }

    // Setter for the list of stock quotes.
    public void setStockQuotes(List<StockQuote> stockQuotes) {
        this.stockQuotes = stockQuotes;
    }

    // Provides a string representation of the AlphaVantageBatchResponse object for
    // logging or debugging.
    @Override
    public String toString() {
        return "AlphaVantageBatchResponse[stockQuotes=" + stockQuotes + "]";
    }

    // Nested class representing a single stock quote in the batch response.
    // Ignores unknown properties in the JSON response to prevent deserialization
    // errors.
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StockQuote {
        // Stock symbol, mapped to the "1. symbol" JSON field.
        @JsonProperty("1. symbol")
        private String symbol;
        // Stock price, mapped to the "2. price" JSON field.
        @JsonProperty("2. price")
        private String price;

        // Getter for the stock symbol.
        public String getSymbol() {
            return symbol;
        }

        // Getter for the stock price.
        public String getPrice() {
            return price;
        }

        // Setter for the stock symbol.
        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        // Setter for the stock price.
        public void setPrice(String price) {
            this.price = price;
        }

        // Provides a string representation of the StockQuote object for logging or
        // debugging.
        @Override
        public String toString() {
            return "StockQuote[symbol=" + symbol + ", price=" + price + "]";
        }
    }
}