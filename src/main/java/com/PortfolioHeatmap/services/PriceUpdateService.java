package com.PortfolioHeatmap.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.PortfolioHeatmap.models.PriceHistory;
import com.PortfolioHeatmap.models.Stock;
import com.PortfolioHeatmap.models.StockPrice;
import com.PortfolioHeatmap.repositories.PriceHistoryRepository;
import com.PortfolioHeatmap.repositories.StockRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PriceUpdateService {

    private static final Logger log = LoggerFactory.getLogger(PriceUpdateService.class);
    private static final int BATCH_SIZE = 100; // Matches the limit in FmpStockDataService

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    @Autowired
    private FMPStockDataService fmpStockDataService;

    @Scheduled(cron = "0 0 18 * * ?") // Runs daily at 6:00 PM EST (assuming server is in UTC; adjust if needed)
    public void updateDailyPrices() {
        log.info("Starting daily price update for all stocks...");

        // Fetch all stocks
        List<Stock> stocks = stockRepository.findAll();
        if (stocks.isEmpty()) {
            log.warn("No stocks found in the database.");
            return;
        }

        // Split stocks into batches of 100
        List<List<Stock>> batches = new ArrayList<>();
        for (int i = 0; i < stocks.size(); i += BATCH_SIZE) {
            batches.add(stocks.subList(i, Math.min(i + BATCH_SIZE, stocks.size())));
        }

        LocalDate today = LocalDate.now();
        for (List<Stock> batch : batches) {
            List<String> symbols = batch.stream().map(Stock::getTicker).collect(Collectors.toList());
            try {
                List<StockPrice> prices = fmpStockDataService.getBatchStockPrices(symbols);
                if (prices.isEmpty()) {
                    log.warn("No prices returned for batch: {}", symbols);
                    continue;
                }

                // Map prices by symbol for easy lookup
                Map<String, StockPrice> priceMap = prices.stream()
                        .collect(Collectors.toMap(StockPrice::getSymbol, price -> price));

                // Save prices to price_history
                for (Stock stock : batch) {
                    // Skip if an entry already exists for this stock and date
                    if (priceHistoryRepository.existsByStockAndDate(stock, today)) {
                        log.info("Price history already exists for {} on {}", stock.getTicker(), today);
                        continue;
                    }

                    StockPrice stockPrice = priceMap.get(stock.getTicker());
                    if (stockPrice != null) {
                        PriceHistory priceHistory = new PriceHistory();
                        priceHistory.setStock(stock);
                        priceHistory.setDate(today);
                        priceHistory.setClosingPrice(stockPrice.getPrice());
                        priceHistory.setPeRatio(stockPrice.getPeRatio());
                        priceHistory.setMarketCap(stockPrice.getMarketCap());
                        priceHistoryRepository.save(priceHistory);
                        log.info("Updated price for {}: ${} on {}", stock.getTicker(), stockPrice.getPrice(), today);
                    } else {
                        log.warn("No price found for {} on {}", stock.getTicker(), today);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to update prices for batch: {}", symbols, e);
            }
        }

        log.info("Daily price update completed.");
    }
}
