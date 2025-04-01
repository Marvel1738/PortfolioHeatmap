// package declaration
package com.PortfolioHeatmap.services;

// Existing imports
import com.PortfolioHeatmap.exceptions.StockNotFoundException;
import com.PortfolioHeatmap.models.Stock;
import com.PortfolioHeatmap.repositories.StockRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

// New imports for price history and custom queries
import com.PortfolioHeatmap.models.PriceHistory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides business logic for managing Stock entities, including CRUD
 * operations.
 * Extended to include a search method for ticker prefix with market cap
 * sorting.
 * 
 * @author Marvel Bana
 */
@Service
public class StockService {
    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    private final StockRepository stockRepository;
    @PersistenceContext // Inject EntityManager for custom queries
    private EntityManager entityManager;

    // Constructor updated to include EntityManager
    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    // Existing methods
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

    public void saveAllStocks(List<Stock> stocks) {
        stockRepository.saveAll(stocks);
    }

    public void deleteStock(String id) {
        stockRepository.deleteById(id);
    }

    /**
     * Searches stocks by ticker prefix using the latest price history data.
     * Returns a list of maps with ticker, company name, and market cap, sorted by
     * market cap descending, limited to 10 results.
     * 
     * @param prefix The ticker prefix to search (e.g., "A")
     * @return List of stock details (ticker, companyName, marketCap)
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchStocksByPrefix(String prefix) {
        logger.info("Searching stocks with ticker prefix: {}", prefix);
        // Native SQL query to join Stock and PriceHistory, get latest market cap
        String sql = "SELECT s.ticker, s.company_name, ph.market_cap " +
                "FROM stocks s " +
                "LEFT JOIN price_history ph ON s.ticker = ph.stock_ticker " +
                "WHERE s.ticker LIKE :prefix " +
                "AND (ph.date = (SELECT MAX(date) FROM price_history) OR ph.date IS NULL) " +
                "ORDER BY ph.market_cap DESC " +
                "LIMIT 10";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("prefix", prefix + "%"); // Wildcard for prefix match

        // Map query results to a list of stock details
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<Map<String, Object>> stockList = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> stock = new HashMap<>();
            stock.put("ticker", row[0]); // ticker from stocks
            stock.put("companyName", row[1]); // company_name from stocks
            stock.put("marketCap", row[2]); // market_cap from price_history (nullable)
            stockList.add(stock);
        }
        logger.info("Found {} stocks for prefix: {}", stockList.size(), prefix);
        return stockList;
    }
}