package com.PortfolioHeatmap.services;

/**
 * Service class for managing portfolios in the PortfolioHeatmap application.
 * This class provides methods to create, retrieve, and delete portfolios,
 * interacting with the PortfolioRepository for database operations.
 *
 * @author Marvel Bana
 */
import com.PortfolioHeatmap.models.Portfolio;
import com.PortfolioHeatmap.repositories.PortfolioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PortfolioService {
    private static final Logger log = LoggerFactory.getLogger(PortfolioService.class);

    private final PortfolioRepository portfolioRepository;

    // Constructor for dependency injection of PortfolioRepository
    public PortfolioService(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    // Creates a new portfolio for a user with the specified name
    @Transactional
    public Portfolio createPortfolio(Long userId, String name) {
        log.info("Creating portfolio for userId: {}, name: {}", userId, name);
        Optional<Portfolio> existingPortfolio = portfolioRepository.findByUserIdAndName(userId, name);
        if (existingPortfolio.isPresent()) {
            log.warn("Portfolio already exists for userId: {}, name: {}", userId, name);
            throw new RuntimeException("Portfolio with name " + name + " already exists for user");
        }

        Portfolio portfolio = new Portfolio();
        portfolio.setUserId(userId);
        portfolio.setName(name);
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        log.info("Successfully saved portfolio with id: {}", savedPortfolio.getId());
        return savedPortfolio;
    }

    // Retrieves all portfolios for a given user ID
    @Transactional(readOnly = true)
    public List<Portfolio> getPortfoliosByUserId(Long userId) {
        log.info("Fetching portfolios for userId: {}", userId);
        return portfolioRepository.findByUserId(userId);
    }

    // Retrieves a portfolio by its ID, throwing an exception if not found
    @Transactional(readOnly = true)
    public Portfolio getPortfolioById(Long portfolioId) {
        log.info("Fetching portfolio with id: {}", portfolioId);
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found with id: " + portfolioId));
    }

    // Deletes a portfolio by its ID, throwing an exception if not found
    @Transactional
    public void deletePortfolio(Long portfolioId) {
        log.info("Deleting portfolio with id: {}", portfolioId);
        if (!portfolioRepository.existsById(portfolioId)) {
            throw new RuntimeException("Portfolio not found with id: " + portfolioId);
        }
        portfolioRepository.deleteById(portfolioId);
    }
}