package com.PortfolioHeatmap.services;

/**
 * Provides business logic for managing Stock entities, including CRUD operations.
 * This service interacts with the StockRepository to perform database operations and handles
 * stock-related requests from the StockController.
 * 
 * @author [Marvel Bana]
 */
import com.PortfolioHeatmap.exceptions.StockNotFoundException;
import com.PortfolioHeatmap.models.Stock;
import com.PortfolioHeatmap.repositories.StockRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class StockService {

    // Logger for tracking operations and errors in this service.
    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    // Repository for performing database operations on Stock entities.
    @Autowired
    private final StockRepository stockRepository;

    // Constructor for dependency injection of the StockRepository.
    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    // Retrieves a paginated list of all stocks from the database.
    // Uses the provided Pageable to determine the page size and number.
    public Page<Stock> getAllStocks(Pageable pageable) {
        return stockRepository.findAll(pageable);
    }

    // Retrieves a stock by its ID (ticker).
    // Throws a StockNotFoundException if the stock is not found.
    public Stock getStockById(String id) {
        logger.info("Fetching stock with ID: {}", id);
        return stockRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Stock with ID {} not found", id);
                    return new StockNotFoundException("Stock with ID " + id + " not found");
                });
    }

    // Saves a stock to the database.
    // Used for both creating new stocks and updating existing ones.
    public Stock saveStock(Stock stock) {
        return stockRepository.save(stock);
    }

    // Deletes a stock from the database by its ID (ticker).
    public void deleteStock(String id) {
        stockRepository.deleteById(id);
    }
}