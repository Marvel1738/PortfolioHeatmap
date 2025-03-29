package com.PortfolioHeatmap.repositories;

/**
 * Repository interface for managing Portfolio entities in the PortfolioHeatmap application.
 * Extends JpaRepository to provide CRUD operations and includes custom query methods for portfolios.
 *
 * @author Marvel Bana
 */
import com.PortfolioHeatmap.models.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    // Finds a portfolio by user ID and name, returning an Optional
    Optional<Portfolio> findByUserIdAndName(Long userId, String name);

    // Retrieves all portfolios belonging to a specific user
    List<Portfolio> findByUserId(Long userId);
}