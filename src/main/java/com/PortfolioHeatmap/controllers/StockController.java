package com.PortfolioHeatmap.controllers;

/**
 * Manages stock-related endpoints for CRUD operations, stock price retrieval, and price history updates.
 * This controller interacts with StockService for stock management, StockDataService for fetching stock prices,
 * and PriceHistoryRepository for updating historical price data.
 * 
 * @author Marvel Bana
 */
import com.PortfolioHeatmap.models.PriceHistory;
import com.PortfolioHeatmap.models.Stock;
import com.PortfolioHeatmap.models.StockPrice;
import com.PortfolioHeatmap.models.FMPSP500ConstituentResponse;
import com.PortfolioHeatmap.models.HistoricalPrice;
import com.PortfolioHeatmap.repositories.PriceHistoryRepository;
import com.PortfolioHeatmap.services.StockDataService;
import com.PortfolioHeatmap.services.StockDataServiceFactory;
import com.PortfolioHeatmap.services.StockService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Iterator;

@RestController
@RequestMapping("/stocks")
public class StockController {
    // Logger for tracking requests, responses, and errors in this controller
    private static final Logger log = LoggerFactory.getLogger(StockController.class);
    // Dependencies for stock management, stock price retrieval, and price history
    // updates
    private final StockService stockService;
    private final StockDataService stockDataService;
    private final PriceHistoryRepository priceHistoryRepository;

    // Constructor for dependency injection of StockService,
    // StockDataServiceFactory, and PriceHistoryRepository
    // Uses the factory to get the appropriate StockDataService implementation
    public StockController(StockService stockService, StockDataServiceFactory factory,
            PriceHistoryRepository priceHistoryRepository) {
        this.stockService = stockService;
        this.stockDataService = factory.getService();
        this.priceHistoryRepository = priceHistoryRepository;
    }

    // Handles GET /stocks to retrieve a paginated list of all stocks
    // Returns a Page of Stock objects based on the provided Pageable parameters
    @GetMapping
    public Page<Stock> getAllStocks(Pageable pageable) {
        return stockService.getAllStocks(pageable);
    }

    // Handles GET /stocks/{id} to retrieve a specific stock by its ID
    // Returns the Stock if found, or a 404 Not Found response if the stock doesn't
    // exist
    @GetMapping("/{id}")
    public ResponseEntity<Stock> getStockById(@PathVariable String id) {
        Stock stock = stockService.getStockById(id);
        if (stock != null) {
            return ResponseEntity.ok(stock);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Handles POST /stocks to add a new stock
    // Takes a Stock object in the request body and saves it using StockService
    @PostMapping
    public Stock addStock(@RequestBody Stock stock) {
        return stockService.saveStock(stock);
    }

    // Handles DELETE /stocks/{id} to delete a stock by its ID
    // Returns a 204 No Content response upon successful deletion
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable String id) {
        stockService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }

    // Handles GET /stocks/price/{symbol} to fetch the current price of a stock by
    // its symbol
    // Logs the request and response, returns the StockPrice if successful, or a 500
    // error if an exception occurs
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

    // Handles GET /stocks/batch-prices to fetch current prices for multiple stocks
    // Takes a list of symbols as query parameters, logs the request and response
    // Returns a list of StockPrice objects or a 500 error if an exception occurs
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

    /**
     * Handles GET requests to search stocks by ticker prefix.
     * Returns a list of stocks with ticker, company name, and market cap, sorted by
     * market cap in descending order, limited to 10 results.
     * 
     * @param prefix The ticker prefix to search for (e.g., "A")
     * @return ResponseEntity containing the list of stock details or error status
     */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchStocks(@RequestParam String prefix) {
        log.info("Received stock search request for prefix: {}", prefix);
        try {
            List<Map<String, Object>> stocks = stockService.searchStocksByPrefix(prefix);
            return ResponseEntity.ok(stocks);
        } catch (Exception e) {
            log.error("Error searching stocks: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null); // 500 Internal Server Error
        }
    }

    // Handles PUT /stocks/{id}/update-price to update the price of a stock in the
    // price history
    // Fetches the latest price for the stock and updates the corresponding
    // PriceHistory entry
    // Logs the request and response, returns a success message or a 500 error if an
    // exception occurs
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

    // Handles POST /stocks/price-history/populate/{symbol} to populate the
    // price_history table with historical prices
    // Fetches historical prices for the past year and saves them to the database
    private List<PriceHistory> populatePriceHistoryForStock(Stock stock, LocalDate from, LocalDate to) {
        String symbol = stock.getTicker();
        log.info("Populating price history for symbol: {}", symbol);

        // Fetch historical prices
        List<HistoricalPrice> historicalPrices = stockDataService.getHistoricalPrices(symbol, from, to);
        if (historicalPrices.isEmpty()) {
            log.warn("No historical prices found for symbol: {}", symbol);
            return List.of();
        }

        // Fetch current market cap and PE ratio using /quote endpoint
        StockPrice stockPrice = stockDataService.getStockPrice(symbol);
        Long marketCap = stockPrice.getMarketCap();
        Double peRatio = stockPrice.getPeRatio();
        log.info("Fetched current data for {}: marketCap={}, peRatio={}", symbol, marketCap, peRatio);

        // Map historical prices to PriceHistory entities
        List<PriceHistory> priceHistories = historicalPrices.stream()
                .filter(hp -> {
                    boolean exists = priceHistoryRepository.existsByStockAndDate(stock, LocalDate.parse(hp.getDate()));
                    if (exists) {
                        log.info("Skipping existing price history entry for {} on {}", symbol, hp.getDate());
                        return false;
                    }
                    return true;
                })
                .map(hp -> {
                    PriceHistory priceHistory = new PriceHistory();
                    priceHistory.setStock(stock);
                    priceHistory.setDate(LocalDate.parse(hp.getDate()));
                    priceHistory.setClosingPrice(hp.getClose()); // Use getClose() as per typical FMP response
                    priceHistory.setPeRatio(peRatio); // Set the current PE ratio
                    priceHistory.setMarketCap(marketCap); // Set the current market cap
                    return priceHistory;
                })
                .collect(Collectors.toList());

        if (!priceHistories.isEmpty()) {
            priceHistoryRepository.saveAll(priceHistories);
            log.info("Saved {} historical price entries for symbol: {}", priceHistories.size(), symbol);
        }
        return priceHistories;
    }

    // Endpoint to trigger price history population for a specific stock
    @PostMapping("/price-history/populate/{symbol}")
    public ResponseEntity<String> populatePriceHistory(@PathVariable String symbol) {
        log.info("Populating price history for symbol: {}", symbol);
        try {
            Stock stock = stockService.getStockById(symbol);
            if (stock == null) {
                log.error("Stock not found for symbol: {}", symbol);
                return ResponseEntity.status(404).body("Stock not found for symbol: " + symbol);
            }

            LocalDate to = LocalDate.now();
            LocalDate from = to.minusYears(1); // 1 year of data
            List<PriceHistory> priceHistories = populatePriceHistoryForStock(stock, from, to);

            if (priceHistories.isEmpty()) {
                return ResponseEntity.ok("No new historical prices found for " + symbol);
            }
            return ResponseEntity
                    .ok("Successfully populated " + priceHistories.size() + " historical price entries for " + symbol);
        } catch (RuntimeException e) {
            log.error("Error populating price history for symbol {}: {}", symbol, e.getMessage(), e);
            return ResponseEntity.status(500).body("Error populating price history: " + e.getMessage());
        }
    }

    // Endpoint to populate price history for all stocks in batches
    @PostMapping("/price-history/populate-all")
    public ResponseEntity<String> populateAllPriceHistories() {
        log.info("Populating price history for all stocks");
        try {
            List<Stock> allStocks = stockService.getAllStocks(Pageable.unpaged()).getContent();
            if (allStocks.isEmpty()) {
                log.warn("No stocks found in the database to populate price history");
                return ResponseEntity.ok("No stocks found to populate price history");
            }

            LocalDate to = LocalDate.now();
            LocalDate from = to.minusYears(1); // 1 year of data
            int totalEntries = 0;
            int batchSize = 10;
            int delayMs = 1000; // 1-second delay between batches (20 requests per batch, 300 requests per
                                // minute)

            for (int i = 0; i < allStocks.size(); i += batchSize) {
                List<Stock> batch = allStocks.subList(i, Math.min(i + batchSize, allStocks.size()));
                log.info("Processing batch of {} stocks (starting at index {})", batch.size(), i);

                for (Stock stock : batch) {
                    try {
                        List<PriceHistory> priceHistories = populatePriceHistoryForStock(stock, from, to);
                        totalEntries += priceHistories.size();
                    } catch (Exception e) {
                        log.error("Failed to populate price history for symbol {}: {}", stock.getTicker(),
                                e.getMessage(), e);
                    }
                }

                if (i + batchSize < allStocks.size()) {
                    log.info("Waiting {}ms before processing the next batch to respect API rate limits", delayMs);
                    Thread.sleep(delayMs);
                }
            }

            log.info("Completed populating price history for all stocks. Total entries added: {}", totalEntries);
            return ResponseEntity
                    .ok("Successfully populated price history for all stocks. Total entries added: " + totalEntries);
        } catch (Exception e) {
            log.error("Error populating price history for all stocks: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error populating price history for all stocks: " + e.getMessage());
        }
    }

    // Endpoint to populate the stocks table with S&P 500 constituents from FMP API
    @PostMapping("/populate")
    public ResponseEntity<String> populateStocks() throws java.io.IOException {
        log.info("Populating stocks table with all S&P 500 stocks");
        try {
            // Fetch the S&P 500 constituents from the FMP API
            List<FMPSP500ConstituentResponse> sp500List = stockDataService.getSP500Constituents();
            if (sp500List.isEmpty()) {
                log.warn("No S&P 500 constituents found in the response");
                return ResponseEntity.ok("No S&P 500 constituents found to populate");
            }

            // Log the raw response and write to a file
            String rawResponse = stockDataService.getRawSP500ConstituentsResponse();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonArray = objectMapper.readTree(rawResponse);
                List<JsonNode> firstFiveNodes = new ArrayList<>();
                Iterator<JsonNode> elements = jsonArray.elements();
                int count = 0;
                while (elements.hasNext() && count < 5) {
                    firstFiveNodes.add(elements.next());
                    count++;
                }
                String firstFiveEntries = objectMapper.writeValueAsString(firstFiveNodes);
                log.info("Raw FMP API /sp500_constituent response (first 5 entries): {}", firstFiveEntries);
            } catch (Exception e) {
                log.error("Failed to parse raw response for logging: {}", e.getMessage());
            }

            try {
                Files.writeString(Paths.get("sp500_constituents_response.json"), rawResponse);
                log.info("Wrote full raw /sp500_constituent response to sp500_constituents_response.json");
            } catch (IOException e) {
                log.error("Failed to write raw response to file: {}", e.getMessage());
            }

            log.info("Total S&P 500 constituents retrieved from FMP API: {}", sp500List.size());

            // Map all S&P 500 stocks to Stock entities
            List<Stock> stocksToSave = sp500List.stream()
                    .map(stockResponse -> {
                        Stock stock = new Stock();
                        stock.setTicker(stockResponse.getSymbol());
                        stock.setCompanyName(stockResponse.getName());
                        return stock;
                    })
                    .collect(Collectors.toList());

            if (stocksToSave.isEmpty()) {
                log.warn("No S&P 500 stocks to populate after mapping");
                return ResponseEntity.ok("No S&P 500 stocks to populate after mapping");
            }

            log.info("All S&P 500 stocks to be saved:");
            stocksToSave
                    .forEach(stock -> log.info("Stock: {}, Company: {}", stock.getTicker(), stock.getCompanyName()));

            stockService.saveAllStocks(stocksToSave);
            log.info("Saved {} stocks to the stocks table", stocksToSave.size());
            return ResponseEntity.ok("Successfully populated " + stocksToSave.size() + " stocks");
        } catch (RuntimeException e) {
            log.error("Error populating stocks table: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error populating stocks table: " + e.getMessage());
        }
    }

    /**
     * Handles GET requests to retrieve stock info by ticker symbol.
     * Returns basic information about the stock including its current price and
     * daily change.
     * 
     * @param ticker The ticker symbol of the stock (e.g., "AAPL")
     * @return ResponseEntity containing stock information or error status
     */
    @GetMapping("/info/{ticker}")
    public ResponseEntity<Map<String, Object>> getStockInfo(@PathVariable String ticker) {
        log.info("Fetching stock info for ticker: {}", ticker);
        try {
            Stock stock = stockService.getStockById(ticker);
            if (stock == null) {
                return ResponseEntity.notFound().build();
            }

            StockPrice stockPrice = stockDataService.getStockPrice(ticker);

            Map<String, Object> stockInfo = new HashMap<>();
            stockInfo.put("ticker", ticker);
            stockInfo.put("companyName", stock.getCompanyName());
            stockInfo.put("price", stockPrice.getPrice());

            // Calculate change based on previous close and current price
            double previousClose = stockPrice.getPreviousClose();
            double currentPrice = stockPrice.getPrice();
            double change = currentPrice - previousClose;
            double changePercent = previousClose > 0 ? (change / previousClose) * 100 : 0;

            stockInfo.put("change", change);
            stockInfo.put("changePercent", changePercent);
            stockInfo.put("marketCap", stockPrice.getMarketCap());

            // Add additional available information
            stockInfo.put("open", stockPrice.getOpen());
            stockInfo.put("high", stockPrice.getHigh());
            stockInfo.put("low", stockPrice.getLow());
            stockInfo.put("peRatio", stockPrice.getPeRatio());

            return ResponseEntity.ok(stockInfo);
        } catch (Exception e) {
            log.error("Error fetching stock info for {}: {}", ticker, e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Handles GET requests to retrieve historical stock data based on ticker symbol
     * and timeframe.
     * Returns a list of historical price data points for the specified timeframe.
     * 
     * @param ticker    The ticker symbol of the stock (e.g., "AAPL")
     * @param timeframe The time period to retrieve data for (e.g., "1d", "1w",
     *                  "1m", "3m", "6m", "1y", "5y")
     * @return ResponseEntity containing a list of historical price data or error
     *         status
     */
    @GetMapping("/history/{ticker}")
    public ResponseEntity<List<Map<String, Object>>> getStockHistory(
            @PathVariable String ticker,
            @RequestParam(defaultValue = "1m") String timeframe) {

        log.info("Fetching stock history for ticker: {} with timeframe: {}", ticker, timeframe);
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate;

            // Determine the start date based on the timeframe
            switch (timeframe) {
                case "1d":
                    startDate = endDate.minusDays(1);
                    break;
                case "5d":
                    startDate = endDate.minusDays(5);
                    break;
                case "1w":
                    startDate = endDate.minusWeeks(1);
                    break;
                case "1m":
                    startDate = endDate.minusMonths(1);
                    break;
                case "3m":
                    startDate = endDate.minusMonths(3);
                    break;
                case "6m":
                    startDate = endDate.minusMonths(6);
                    break;
                case "1y":
                    startDate = endDate.minusYears(1);
                    break;
                case "5y":
                    startDate = endDate.minusYears(5);
                    break;
                default:
                    startDate = endDate.minusMonths(1); // Default to 1 month
            }

            // Get historical prices from the service
            List<HistoricalPrice> historicalPrices = stockDataService.getHistoricalPrices(ticker, startDate, endDate);

            // Convert to format expected by frontend
            List<Map<String, Object>> formattedHistory = historicalPrices.stream()
                    .map(price -> {
                        Map<String, Object> dataPoint = new HashMap<>();
                        dataPoint.put("date", price.getDate());
                        dataPoint.put("close", price.getClose());

                        // HistoricalPrice model doesn't have these fields based on the model definition
                        // If they're needed, they should be added to the HistoricalPrice class
                        // For now, we'll put null values
                        dataPoint.put("open", null);
                        dataPoint.put("high", null);
                        dataPoint.put("low", null);
                        dataPoint.put("volume", null);

                        return dataPoint;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(formattedHistory);
        } catch (Exception e) {
            log.error("Error fetching stock history for {}: {}", ticker, e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
}