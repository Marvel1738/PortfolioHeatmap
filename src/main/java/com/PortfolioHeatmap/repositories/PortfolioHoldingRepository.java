package com.PortfolioHeatmap.repositories;

/**
 * Repository interface for managing PortfolioHolding entities in the PortfolioHeatmap application.
 * Extends JpaRepository to provide CRUD operations and includes custom query methods for portfolio holdings.
 *
 * @author Marvel Bana
 */
import com.PortfolioHeatmap.models.PortfolioHolding;
import com.PortfolioHeatmap.models.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioHoldingRepository extends JpaRepository<PortfolioHolding, Long> {
    // Finds a holding by portfolio ID and stock, returning an Optional
    Optional<PortfolioHolding> findByPortfolioIdAndStock(Long portfolioId, Stock stock);

    // Retrieves all open positions (holdings with no selling date) for a portfolio
    List<PortfolioHolding> findByPortfolioIdAndSellingDateIsNull(Long portfolioId); // Open positions

    // Retrieves all closed positions (holdings with a selling date) for a portfolio
    List<PortfolioHolding> findByPortfolioIdAndSellingDateIsNotNull(Long portfolioId); // Closed positions
}