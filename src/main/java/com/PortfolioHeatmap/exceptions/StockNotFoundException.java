package com.PortfolioHeatmap.exceptions;

public class StockNotFoundException extends RuntimeException {
    public StockNotFoundException(String message) {
        super(message);
    }
}
