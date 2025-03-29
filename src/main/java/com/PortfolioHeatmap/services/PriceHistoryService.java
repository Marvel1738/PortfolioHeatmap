package com.PortfolioHeatmap.services;

/**
 * Service class for managing price history data in the PortfolioHeatmap application.
 * This class provides methods to save and retrieve price history records,
 * interacting with the PriceHistoryRepository for database operations.
 *
 * @author Marvel Bana
 */
import com.PortfolioHeatmap.models.PriceHistory;
import com.PortfolioHeatmap.repositories.PriceHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriceHistoryService {
    private static final Logger log = LoggerFactory.getLogger(PriceHistoryService.class);

    private final PriceHistoryRepository priceHistoryRepository;

    // Constructor for dependency injection of PriceHistoryRepository
    public PriceHistoryService(PriceHistoryRepository priceHistoryRepository) {
        this.priceHistoryRepository = priceHistoryRepository;
    }

    // Saves a list of price history entries to the database
    public void saveAllPriceHistories(List<PriceHistory> priceHistories) {
        log.info("Saving {} price history entries", priceHistories.size());
        priceHistoryRepository.saveAll(priceHistories);
    }

    // Retrieves a paginated list of all price history records
    public Page<PriceHistory> getAllPriceHistories(Pageable pageable) {
        return priceHistoryRepository.findAll(pageable);
    }

    // Fetches the most recent price history record for a given stock ticker
    public PriceHistory getLatestPriceHistory(String ticker) {
        log.info("Fetching latest price history for ticker: {}", ticker);
        return priceHistoryRepository.findTopByStockTickerOrderByDateDesc(ticker)
                .orElse(null);
    }
}