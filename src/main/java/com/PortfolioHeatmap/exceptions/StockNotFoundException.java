package com.PortfolioHeatmap.exceptions;

/**
 * Custom exception class for handling cases where a stock is not found in the
 * PortfolioHeatmap application.
 * This exception extends RuntimeException and is thrown when a stock cannot be
 * located by its identifier.
 *
 * @author Marvel Bana
 */
public class StockNotFoundException extends RuntimeException {
    // Constructor that takes a message and passes it to the superclass
    // (RuntimeException)
    public StockNotFoundException(String message) {
        super(message);
    }
}