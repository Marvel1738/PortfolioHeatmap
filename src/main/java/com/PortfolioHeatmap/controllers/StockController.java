package com.PortfolioHeatmap.controllers;

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
    private static final Logger log = LoggerFactory.getLogger(StockController.class);
    private final StockService stockService;
    private final StockDataService stockDataService;
    private final PriceHistoryRepository priceHistoryRepository;

    public StockController(StockService stockService, StockDataServiceFactory factory,
            PriceHistoryRepository priceHistoryRepository) {
        this.stockService = stockService;
        this.stockDataService = factory.getService();
        this.priceHistoryRepository = priceHistoryRepository;
    }

    // Existing CRUD Endpoints
    @GetMapping
    public Page<Stock> getAllStocks(Pageable pageable) {
        return stockService.getAllStocks(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Stock> getStockById(@PathVariable String id) {
        Stock stock = stockService.getStockById(id);
        if (stock != null) {
            return ResponseEntity.ok(stock);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public Stock addStock(@RequestBody Stock stock) {
        return stockService.saveStock(stock);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable String id) {
        stockService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }

    // Stock Price Endpoints
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

    // Update Price Endpoint
    @PutMapping("/{id}/update-price")
    public ResponseEntity<String> updateStockPrice(@PathVariable Long id) {
        log.info("Updating price for price_history ID: {}", id);
        try {
            // Find the price history entry
            PriceHistory priceHistory = priceHistoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Price history not found for ID: " + id));

            // Get the ticker from the associated Stock entity
            String ticker = priceHistory.getStock().getTicker();

            // Verify the stock exists (this step might be redundant since the relationship
            // ensures it exists)
            Stock stock = stockService.getStockById(ticker);
            if (stock == null) {
                throw new RuntimeException("Stock not found for ticker: " + ticker);
            }

            // Fetch the latest price using the factory-selected service
            StockPrice stockPrice = stockDataService.getStockPrice(ticker);

            // Update the price history
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