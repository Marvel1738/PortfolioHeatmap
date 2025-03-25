package com.PortfolioHeatmap.repositories;

/**
 * Repository interface for managing PriceHistory entities in the database.
 * Provides methods for basic CRUD operations and custom queries for price history data.
 * 
 * @author [Your Name]
 */
import com.PortfolioHeatmap.models.PriceHistory;
import com.PortfolioHeatmap.models.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    // Check if a price history entry exists for a given stock and date
    boolean existsByStockAndDate(Stock stock, LocalDate date);
}