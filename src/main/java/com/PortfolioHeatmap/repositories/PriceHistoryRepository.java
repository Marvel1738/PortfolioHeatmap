package com.PortfolioHeatmap.repositories;

import com.PortfolioHeatmap.models.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing {@link PriceHistory} entities.
 * Extends JpaRepository to provide built-in CRUD operations.
 */

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
}