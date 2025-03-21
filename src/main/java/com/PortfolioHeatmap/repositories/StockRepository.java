package com.PortfolioHeatmap.repositories;

import com.PortfolioHeatmap.models.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface StockRepository extends JpaRepository<Stock, String> {
    Page<Stock> findAll(Pageable pageable);
}
