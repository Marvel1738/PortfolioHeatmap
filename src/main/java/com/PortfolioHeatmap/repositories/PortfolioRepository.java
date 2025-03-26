package com.PortfolioHeatmap.repositories;

import com.PortfolioHeatmap.models.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Optional<Portfolio> findByUserIdAndName(Long userId, String name);

    List<Portfolio> findByUserId(Long userId);
}