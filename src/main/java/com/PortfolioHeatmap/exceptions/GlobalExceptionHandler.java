package com.PortfolioHeatmap.exceptions;

/**
 * Global exception handler for the PortfolioHeatmap application.
 * This class provides centralized exception handling across all controllers,
 * mapping specific exceptions to appropriate HTTP responses.
 *
 * @author Marvel Bana
 */
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handles StockNotFoundException by returning a 404 Not Found response with the
    // exception message
    @ExceptionHandler(StockNotFoundException.class)
    public ResponseEntity<String> handleStockNotFound(StockNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}