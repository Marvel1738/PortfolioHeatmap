package com.PortfolioHeatmap.repositories;

/**
 * Interface for custom portfolio repository operations in the PortfolioHeatmap
 * application.
 * Provides a method to refresh entities in the persistence context.
 *
 * @author Marvel Bana
 */
public interface CustomPortfolioRepository {
    // Refreshes the state of an entity from the database
    void refresh(Object entity);
}