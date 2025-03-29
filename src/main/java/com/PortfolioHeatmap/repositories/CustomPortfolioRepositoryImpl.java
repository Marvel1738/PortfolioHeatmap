package com.PortfolioHeatmap.repositories;

/**
 * Implementation of the CustomPortfolioRepository interface in the PortfolioHeatmap application.
 * Uses an EntityManager to perform custom database operations for portfolios.
 *
 * @author Marvel Bana
 */
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CustomPortfolioRepositoryImpl implements CustomPortfolioRepository {

    @Autowired
    private EntityManager entityManager;

    // Refreshes the given entity by reloading its state from the database
    @Override
    public void refresh(Object entity) {
        entityManager.refresh(entity);
    }
}