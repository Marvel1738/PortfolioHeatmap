package com.PortfolioHeatmap.services;

import com.PortfolioHeatmap.models.Portfolio;
import com.PortfolioHeatmap.repositories.PortfolioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final PortfolioHoldingService portfolioHoldingService;
    private static final Logger log = LoggerFactory.getLogger(PortfolioService.class);

    // Default Warren Buffett portfolio holdings
    private static final String[][] DEFAULT_HOLDINGS = {
            { "AAPL", "300000000" },
            { "AXP", "151610700" },
            { "BAC", "680233587" },
            { "KO", "400000000" },
            { "CVX", "118610534" },
            { "MCO", "24669778" },
            { "OXY", "264941431" },
            { "KHC", "325634818" },
            { "DVA", "35142479" },
            { "VRSN", "13271457" },
            { "V", "8297460" },
            { "MA", "3986648" },
            { "STZ", "5624324" },
            { "DPZ", "2382000" },
            { "CHTR", "1991759" },
            { "TMUS", "4350000" },
            { "C", "14639502" },
            { "COF", "7450000" },
            { "KR", "50000000" },
            { "CHUBB", "27033784" },
            { "AON", "4100000" },
            { "HEI", "1049687" },
            { "POOL", "598689" },
            { "NVR", "11112" },
            { "DEO", "227750" },
            { "AMZN", "10000000" },
            { "ALLY", "29000000" },
            { "LEN.B", "152572" },
            { "TMO", "1000000" },
            { "MDT", "2000000" }
    };

    public PortfolioService(PortfolioRepository portfolioRepository, PortfolioHoldingService portfolioHoldingService) {
        this.portfolioRepository = portfolioRepository;
        this.portfolioHoldingService = portfolioHoldingService;
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

    public void createDefaultPortfolio(Long userId) {
        log.info("Creating default Warren Buffett portfolio for user: {}", userId);

        try {
            // Create the portfolio
            Portfolio portfolio = new Portfolio();
            portfolio.setUserId(userId);
            portfolio.setName("Warren Buffett's Portfolio");
            portfolio = portfolioRepository.save(portfolio);
            log.info("Created default portfolio with ID: {}", portfolio.getId());

            // Add the cash holding first
            try {
                log.info("Adding cash holding: 334 billion dollars");
                portfolioHoldingService.addHolding(
                        portfolio,
                        "Cash",
                        334000000000.0,
                        null, // Set purchase price to null for cash
                        LocalDate.now(),
                        null,
                        null);
                log.info("Successfully added cash holding");
            } catch (Exception e) {
                log.error("Failed to add cash holding: {}", e.getMessage());
            }

            // Add the holdings
            for (String[] holding : DEFAULT_HOLDINGS) {
                try {
                    String ticker = holding[0];
                    Double shares = Double.parseDouble(holding[1]);

                    log.info("Adding holding: {} with {} shares", ticker, shares);
                    portfolioHoldingService.addHolding(
                            portfolio,
                            ticker,
                            shares,
                            null, // purchase price
                            LocalDate.now(),
                            null, // selling price
                            null // selling date
                    );
                    log.info("Successfully added holding: {}", ticker);
                } catch (Exception e) {
                    log.error("Failed to add holding {}: {}", holding[0], e.getMessage());
                    // Continue with next holding even if one fails
                }
            }

            log.info("Completed creating default portfolio for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to create default portfolio for user {}: {}", userId, e.getMessage());
            throw e;
        }
    }
}