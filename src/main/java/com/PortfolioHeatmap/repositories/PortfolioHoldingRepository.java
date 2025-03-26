package com.PortfolioHeatmap.repositories;

import com.PortfolioHeatmap.models.PortfolioHolding;
import com.PortfolioHeatmap.models.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioHoldingRepository extends JpaRepository<PortfolioHolding, Long> {
    Optional<PortfolioHolding> findByPortfolioIdAndStock(Long portfolioId, Stock stock);

    List<PortfolioHolding> findByPortfolioIdAndSellingDateIsNull(Long portfolioId); // Open positions

    List<PortfolioHolding> findByPortfolioIdAndSellingDateIsNotNull(Long portfolioId); // Closed positions
}