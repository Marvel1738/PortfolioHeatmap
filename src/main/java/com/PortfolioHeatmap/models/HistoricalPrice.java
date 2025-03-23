package com.PortfolioHeatmap.models;

/**
 * Represents a single historical price entry for a stock, containing the date and closing price.
 * This class is used to deserialize historical price data from external APIs like FMP or Alpha Vantage.
 * 
 * @author [Your Name]
 */
import java.time.LocalDate;

public class HistoricalPrice {
    // The date of the historical price entry.
    private LocalDate date;
    // The closing price of the stock on that date.
    private double closingPrice;

    // Default constructor for deserialization.
    public HistoricalPrice() {
    }

    // Constructor for creating a HistoricalPrice object with a date and closing
    // price.
    public HistoricalPrice(LocalDate date, double closingPrice) {
        this.date = date;
        this.closingPrice = closingPrice;
    }

    // Getter for the date.
    public LocalDate getDate() {
        return date;
    }

    // Setter for the date.
    public void setDate(LocalDate date) {
        this.date = date;
    }

    // Getter for the closing price.
    public double getClosingPrice() {
        return closingPrice;
    }

    // Setter for the closing price.
    public void setClosingPrice(double closingPrice) {
        this.closingPrice = closingPrice;
    }

    // Provides a string representation of the HistoricalPrice object for logging or
    // debugging.
    @Override
    public String toString() {
        return "HistoricalPrice[date=" + date + ", closingPrice=" + closingPrice + "]";
    }
}