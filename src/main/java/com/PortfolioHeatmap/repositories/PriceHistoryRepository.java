package com.PortfolioHeatmap.repositories;

import com.PortfolioHeatmap.models.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
}