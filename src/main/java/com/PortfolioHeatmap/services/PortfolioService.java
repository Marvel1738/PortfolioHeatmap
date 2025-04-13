package com.PortfolioHeatmap.services;

import com.PortfolioHeatmap.models.Portfolio;
import com.PortfolioHeatmap.repositories.PortfolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;

    public PortfolioService(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    public Portfolio createPortfolio(Long userId, String name) {
        Portfolio portfolio = new Portfolio();
        portfolio.setUserId(userId);
        portfolio.setName(name);
        return portfolioRepository.save(portfolio);
    }

    public List<Portfolio> getPortfoliosByUserId(Long userId) {
        return portfolioRepository.findByUserId(userId);
    }

    public Portfolio getPortfolioById(Long id) {
        return portfolioRepository.findById(id).orElse(null);
    }

    public void deletePortfolio(Long portfolioId) {
        portfolioRepository.deleteById(portfolioId);
    }

    @Transactional
    public Portfolio renamePortfolio(Long portfolioId, String newName) {
        Optional<Portfolio> portfolioOpt = portfolioRepository.findById(portfolioId);
        if (portfolioOpt.isPresent()) {
            Portfolio portfolio = portfolioOpt.get();
            portfolio.setName(newName);
            return portfolioRepository.save(portfolio);
        }
        throw new RuntimeException("Portfolio not found");
    }

    @Transactional
    public Portfolio setFavoritePortfolio(Long portfolioId, boolean isFavorite) {
        Optional<Portfolio> portfolioOpt = portfolioRepository.findById(portfolioId);
        if (portfolioOpt.isPresent()) {
            Portfolio portfolio = portfolioOpt.get();
            portfolio.setFavorite(isFavorite);
            return portfolioRepository.save(portfolio);
        }
        throw new RuntimeException("Portfolio not found");
    }
}