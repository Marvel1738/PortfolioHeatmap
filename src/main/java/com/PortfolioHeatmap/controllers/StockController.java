package com.PortfolioHeatmap.controllers;

/**
 * Manages stock-related endpoints for CRUD operations, stock price retrieval, and price history updates.
 * This controller interacts with StockService for stock management, StockDataService for fetching stock prices,
 * and PriceHistoryRepository for updating historical price data.
 * 
 * @author [Your Name]
 */
import com.PortfolioHeatmap.models.PriceHistory;
import com.PortfolioHeatmap.models.Stock;
import com.PortfolioHeatmap.models.StockPrice;
import com.PortfolioHeatmap.repositories.PriceHistoryRepository;
import com.PortfolioHeatmap.services.StockDataService;
import com.PortfolioHeatmap.services.StockDataServiceFactory;
import com.PortfolioHeatmap.services.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/stocks")
public class StockController {
    // Logger for tracking requests, responses, and errors in this controller.
    private static final Logger log = LoggerFactory.getLogger(StockController.class);
    // Dependencies for stock management, stock price retrieval, and price history
    // updates.
    private final StockService stockService;
    private final StockDataService stockDataService;
    private final PriceHistoryRepository priceHistoryRepository;

    // Constructor for dependency injection of StockService,
    // StockDataServiceFactory, and PriceHistoryRepository.
    // Uses the factory to get the appropriate StockDataService implementation.
    public StockController(StockService stockService, StockDataServiceFactory factory,
            PriceHistoryRepository priceHistoryRepository) {
        this.stockService = stockService;
        this.stockDataService = factory.getService();
        this.priceHistoryRepository = priceHistoryRepository;
    }

    // Handles GET /stocks to retrieve a paginated list of all stocks.
    // Returns a Page of Stock objects based on the provided Pageable parameters.
    @GetMapping
    public Page<Stock> getAllStocks(Pageable pageable) {
        return stockService.getAllStocks(pageable);
    }

    // Handles GET /stocks/{id} to retrieve a specific stock by its ID.
    // Returns the Stock if found, or a 404 Not Found response if the stock doesn't
    // exist.
    @GetMapping("/{id}")
    public ResponseEntity<Stock> getStockById(@PathVariable String id) {
        Stock stock = stockService.getStockById(id);
        if (stock != null) {
            return ResponseEntity.ok(stock);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Handles POST /stocks to add a new stock.
    // Takes a Stock object in the request body and saves it using StockService.
    @PostMapping
    public Stock addStock(@RequestBody Stock stock) {
        return stockService.saveStock(stock);
    }

    // Handles DELETE /stocks/{id} to delete a stock by its ID.
    // Returns a 204 No Content response upon successful deletion.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable String id) {
        stockService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }

    // Handles GET /stocks/price/{symbol} to fetch the current price of a stock by
    // its symbol.
    // Logs the request and response, and returns the StockPrice if successful, or a
    // 500 error if an exception occurs.
    @GetMapping("/price/{symbol}")
    public ResponseEntity<StockPrice> getStockPrice(@PathVariable String symbol) {
        log.info("Fetching price for symbol: {}", symbol);
        try {
            StockPrice stockPrice = stockDataService.getStockPrice(symbol);
            log.info("Returning StockPrice: {}", stockPrice);
            return ResponseEntity.ok(stockPrice);
        } catch (RuntimeException e) {
            log.error("Error fetching stock price for {}: {}", symbol, e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    // Handles GET /stocks/batch-prices to fetch current prices for multiple stocks.
    // Takes a list of symbols as query parameters, logs the request and response,
    // and returns a list of StockPrice objects or a 500 error if an exception
    // occurs.
    @GetMapping("/batch-prices")
    public ResponseEntity<List<StockPrice>> getBatchStockPrices(@RequestParam List<String> symbols) {
        log.info("Fetching batch prices for symbols: {}", symbols);
        try {
            List<StockPrice> stockPrices = stockDataService.getBatchStockPrices(symbols);
            log.info("Returning batch prices: {}", stockPrices);
            return ResponseEntity.ok(stockPrices);
        } catch (RuntimeException e) {
            log.error("Error fetching batch prices: {}", symbols, e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    // Handles PUT /stocks/{id}/update-price to update the price of a stock in the
    // price history.
    // Fetches the latest price for the stock and updates the corresponding
    // PriceHistory entry.
    // Logs the request and response, and returns a success message or a 500 error
    // if an exception occurs.
    @PutMapping("/{id}/update-price")
    public ResponseEntity<String> updateStockPrice(@PathVariable Long id) {
        log.info("Updating price for price_history ID: {}", id);
        try {
            // Find the price history entry by ID, or throw an exception if not found.
            PriceHistory priceHistory = priceHistoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Price history not found for ID: " + id));

            // Get the ticker from the associated Stock entity.
            String ticker = priceHistory.getStock().getTicker();

            // Verify the stock exists (redundant check due to the relationship, but
            // included for safety).
            Stock stock = stockService.getStockById(ticker);
            if (stock == null) {
                throw new RuntimeException("Stock not found for ticker: " + ticker);
            }

            // Fetch the latest price using the factory-selected StockDataService.
            StockPrice stockPrice = stockDataService.getStockPrice(ticker);

            // Update the price history with the new closing price and current date.
            priceHistory.setClosingPrice(stockPrice.getPrice());
            priceHistory.setDate(LocalDate.now());
            priceHistoryRepository.save(priceHistory);

            log.info("Updated price for {}: closing_price={}", ticker, stockPrice.getPrice());
            return ResponseEntity.ok("Price updated for " + ticker);
        } catch (RuntimeException e) {
            log.error("Error updating price for ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body("Error updating price: " + e.getMessage());
        }
    }
}