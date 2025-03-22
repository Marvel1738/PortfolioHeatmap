package com.PortfolioHeatmap.models;

/**
 * Represents the response structure for a stock quote request from the Financial Modeling Prep (FMP) API.
 * This class deserializes the JSON response into a stock quote object, used by the application to fetch
 * and process stock price data for a specific symbol.
 * 
 * @author [Marvel Bana]
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Ignores unknown properties in the JSON response to prevent deserialization errors.
@JsonIgnoreProperties(ignoreUnknown = true)
public class FMPQuoteResponse {
    // Stock symbol, mapped to the "symbol" JSON field.
    @JsonProperty("symbol")
    private String symbol;
    // Current price, mapped to the "price" JSON field.
    @JsonProperty("price")
    private double price;
    // Opening price, mapped to the "open" JSON field.
    @JsonProperty("open")
    private double open;
    // Highest price of the day, mapped to the "dayHigh" JSON field.
    @JsonProperty("dayHigh") // Changed from "high" to "dayHigh"
    private double high;
    // Lowest price of the day, mapped to the "dayLow" JSON field.
    @JsonProperty("dayLow") // Changed from "low" to "dayLow"
    private double low;
    // Previous day's closing price, mapped to the "previousClose" JSON field.
    @JsonProperty("previousClose")
    private double previousClose;

    // Getters and Setters

    // Getter for the stock symbol.
    public String getSymbol() {
        return symbol;
    }

    // Setter for the stock symbol.
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    // Getter for the current price.
    public double getPrice() {
        return price;
    }

    // Setter for the current price.
    public void setPrice(double price) {
        this.price = price;
    }

    // Getter for the opening price.
    public double getOpen() {
        return open;
    }

    // Setter for the opening price.
    public void setOpen(double open) {
        this.open = open;
    }

    // Getter for the highest price of the day.
    public double getHigh() {
        return high;
    }

    // Setter for the highest price of the day.
    public void setHigh(double high) {
        this.high = high;
    }

    // Getter for the lowest price of the day.
    public double getLow() {
        return low;
    }

    // Setter for the lowest price of the day.
    public void setLow(double low) {
        this.low = low;
    }

    // Getter for the previous day's closing price.
    public double getPreviousClose() {
        return previousClose;
    }

    // Setter for the previous day's closing price.
    public void setPreviousClose(double previousClose) {
        this.previousClose = previousClose;
    }
}