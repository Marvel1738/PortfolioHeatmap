package com.PortfolioHeatmap.repositories;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CustomPortfolioRepositoryImpl implements CustomPortfolioRepository {

    @Autowired
    private EntityManager entityManager;

    @Override
    public void refresh(Object entity) {
        entityManager.refresh(entity);
    }
}