package com.PortfolioHeatmap.models;

/**
 * Represents the current price and related data for a stock, used as a data
 * transfer object (DTO)
 * to hold stock price information fetched from external APIs like FMP or Alpha
 * Vantage.
 * 
 * @author [Marvel Bana]
 */
public class StockPrice {
    // The stock symbol (e.g., "AAPL").
    private String symbol;
    // The current price of the stock.
    private double price;
    // The opening price of the stock for the day.
    private double open;
    // The highest price of the stock for the day.
    private double high;
    // The lowest price of the stock for the day.
    private double low;
    // The previous day's closing price of the stock.
    private double previousClose;

    // Constructors

    // Default constructor for creating an empty StockPrice object.
    public StockPrice() {
    }

    // Constructor for creating a StockPrice object with a symbol and price.
    public StockPrice(String symbol, double price) {
        this.symbol = symbol;
        this.price = price;
    }

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