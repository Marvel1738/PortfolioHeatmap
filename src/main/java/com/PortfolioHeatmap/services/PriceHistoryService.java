package com.PortfolioHeatmap.services;

import com.PortfolioHeatmap.models.PriceHistory;
import com.PortfolioHeatmap.repositories.PriceHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PriceHistoryService {
    private static final Logger log = LoggerFactory.getLogger(PriceHistoryService.class);

    private final PriceHistoryRepository priceHistoryRepository;

    public PriceHistoryService(PriceHistoryRepository priceHistoryRepository) {
        this.priceHistoryRepository = priceHistoryRepository;
    }

    public void saveAllPriceHistories(List<PriceHistory> priceHistories) {
        log.info("Saving {} price history entries", priceHistories.size());
        priceHistoryRepository.saveAll(priceHistories);
    }

    public Page<PriceHistory> getAllPriceHistories(Pageable pageable) {
        return priceHistoryRepository.findAll(pageable);
    }

    public PriceHistory getLatestPriceHistory(String ticker) {
        log.info("Fetching latest price history for ticker: {}", ticker);
        return priceHistoryRepository.findTopByStockTickerOrderByDateDesc(ticker)
                .orElse(null);
    }
}