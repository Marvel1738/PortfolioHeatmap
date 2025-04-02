package com.PortfolioHeatmap.services;

/**
 * Service class for managing price history data in the PortfolioHeatmap application.
 * This class provides methods to save and retrieve price history records,
 * interacting with the PriceHistoryRepository for database operations.
 *
 * @author Marvel Bana
 */
import com.PortfolioHeatmap.models.PriceHistory;
import com.PortfolioHeatmap.models.StockPrice;
import com.PortfolioHeatmap.repositories.PriceHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PriceHistoryService {
    private static final Logger log = LoggerFactory.getLogger(PriceHistoryService.class);

    private final PriceHistoryRepository priceHistoryRepository;
    private final FMPStockDataService fmpStockDataService;

    public PriceHistoryService(PriceHistoryRepository priceHistoryRepository, FMPStockDataService fmpStockDataService) {
        this.priceHistoryRepository = priceHistoryRepository;
        this.fmpStockDataService = fmpStockDataService;
    }

    @Cacheable(value = "priceHistory", key = "#ticker + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<PriceHistory> getPriceHistory(String ticker, Pageable pageable) {
        return priceHistoryRepository.findByStockTickerOrderByDateDesc(ticker, pageable);
    }

    @Cacheable(value = "priceHistoryByDate", key = "#stockTicker + '-' + #date")
    public Optional<PriceHistory> findByStockTickerAndDate(String stockTicker, LocalDate date) {
        return priceHistoryRepository.findByStockTickerAndDate(stockTicker, date);
    }

    public PriceHistory save(PriceHistory priceHistory) {
        return priceHistoryRepository.save(priceHistory);
    }

    @Cacheable(value = "latestPrice", key = "#stockTicker")
    public Optional<PriceHistory> findTopByStockTickerOrderByDateDesc(String stockTicker) {
        return priceHistoryRepository.findTopByStockTickerOrderByDateDesc(stockTicker);
    }

    @Cacheable(value = "historicalPrice", key = "#stockTicker + '-' + #date")
    public Optional<PriceHistory> findFirstByStockTickerAndDateLessThanOrderByDateDesc(String stockTicker,
            LocalDate date) {
        return priceHistoryRepository.findFirstByStockTickerAndDateLessThanOrderByDateDesc(stockTicker, date);
    }

    @Cacheable(value = "percentageChange", key = "#ticker + '-' + #timeframe")
    public double calculatePercentageChange(String ticker, String timeframe) {
        // Get today's current price from the API
        StockPrice currentStockPrice = fmpStockDataService.getStockPrice(ticker);
        if (currentStockPrice == null) {
            return 0.0;
        }
        double currentPrice = currentStockPrice.getPrice();

        // Calculate the start date based on timeframe
        LocalDate startDate;
        switch (timeframe) {
            case "1d":
                // For 1-day, find yesterday's closing price
                Optional<PriceHistory> previousDay = priceHistoryRepository
                        .findFirstByStockTickerAndDateLessThanOrderByDateDesc(ticker, LocalDate.now());
                if (previousDay.isEmpty()) {
                    return 0.0;
                }
                double previousClose = previousDay.get().getClosingPrice();
                return ((currentPrice - previousClose) / previousClose) * 100;
            case "1w":
                startDate = LocalDate.now().minusWeeks(1);
                break;
            case "1m":
                startDate = LocalDate.now().minusMonths(1);
                break;
            case "3m":
                startDate = LocalDate.now().minusMonths(3);
                break;
            case "6m":
                startDate = LocalDate.now().minusMonths(6);
                break;
            case "ytd":
                startDate = LocalDate.now().withMonth(1).withDayOfMonth(1);
                break;
            case "1y":
                startDate = LocalDate.now().minusYears(1);
                break;
            default:
                return 0.0;
        }

        // Get the historical price for the start date
        Optional<PriceHistory> historicalPrice = priceHistoryRepository
                .findFirstByStockTickerAndDateLessThanEqualOrderByDateDesc(ticker, startDate);
        if (historicalPrice.isEmpty()) {
            return 0.0;
        }

        // Calculate percentage change using current market price
        double startPrice = historicalPrice.get().getClosingPrice();
        return ((currentPrice - startPrice) / startPrice) * 100;
    }
}