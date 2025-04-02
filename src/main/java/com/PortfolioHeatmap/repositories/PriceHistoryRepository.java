package com.PortfolioHeatmap.repositories;

/**
 * Repository interface for managing PriceHistory entities in the database.
 * Provides methods for basic CRUD operations and custom queries for price history data.
 * 
 * @author [Marvel Bana]
 */
import com.PortfolioHeatmap.models.PriceHistory;
import com.PortfolioHeatmap.models.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    Page<PriceHistory> findByStockTickerOrderByDateDesc(String stockTicker, Pageable pageable);

    @Query("SELECT ph FROM PriceHistory ph JOIN ph.stock s WHERE s.ticker = :ticker AND ph.date = :date")
    Optional<PriceHistory> findByStockTickerAndDate(@Param("ticker") String ticker, @Param("date") LocalDate date);

    // Check if a price history entry exists for a given stock and date
    Optional<PriceHistory> findTopByStockTickerOrderByDateDesc(String stockTicker);

    boolean existsByStockAndDate(Stock stock, LocalDate date);

    // Find latest price history for a stock
    Optional<PriceHistory> findFirstByStockTickerOrderByDateDesc(String stockTicker);

    // Find price history for a stock on or before a specific date
    Optional<PriceHistory> findFirstByStockTickerAndDateLessThanEqualOrderByDateDesc(String stockTicker,
            LocalDate date);

    // Find the previous trading day's price for a stock
    Optional<PriceHistory> findFirstByStockTickerAndDateLessThanOrderByDateDesc(String stockTicker,
            LocalDate date);
}