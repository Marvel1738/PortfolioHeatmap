package com.PortfolioHeatmap.services;

import com.PortfolioHeatmap.exceptions.StockNotFoundException;
import com.PortfolioHeatmap.models.Stock;
import com.PortfolioHeatmap.repositories.StockRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    @Autowired
    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public Page<Stock> getAllStocks(Pageable pageable) {
        return stockRepository.findAll(pageable);
    }

    public Stock getStockById(String id) {
        logger.info("Fetching stock with ID: {}", id);
        return stockRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Stock with ID {} not found", id);
                    return new StockNotFoundException("Stock with ID " + id + " not found");
                });
    }

    public Stock saveStock(Stock stock) {
        return stockRepository.save(stock);
    }

    public void deleteStock(String id) {
        stockRepository.deleteById(id);
    }
}
