package com.PortfolioHeatmap.controllers;

import com.PortfolioHeatmap.models.Portfolio;
import com.PortfolioHeatmap.models.PortfolioHolding;
import com.PortfolioHeatmap.services.PortfolioHoldingService;
import com.PortfolioHeatmap.services.PortfolioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller class responsible for handling HTTP requests related to portfolio
 * management.
 * This class provides endpoints for creating, retrieving, updating, and
 * deleting portfolios and their holdings.
 * It interacts with the PortfolioService and PortfolioHoldingService to perform
 * business logic operations.
 *
 * @author Marvel Bana
 */
@RestController
@RequestMapping("/portfolios")
public class PortfolioController {
    private static final Logger log = LoggerFactory.getLogger(PortfolioController.class);

    private final PortfolioService portfolioService;
    private final PortfolioHoldingService portfolioHoldingService;

    // Constructs a PortfolioController with the required services
    public PortfolioController(PortfolioService portfolioService, PortfolioHoldingService portfolioHoldingService) {
        this.portfolioService = portfolioService;
        this.portfolioHoldingService = portfolioHoldingService;
    }

    // Creates a new portfolio for the current user with the specified name
    @PostMapping("/create")
    public ResponseEntity<Portfolio> createPortfolio(@RequestParam String name) {
        log.info("Creating portfolio with name: {}", name);
        try {
            Long userId = getCurrentUserId();
            Portfolio portfolio = portfolioService.createPortfolio(userId, name);
            return ResponseEntity.ok(portfolio);
        } catch (RuntimeException e) {
            log.error("Error creating portfolio: {}", e.getMessage(), e);
            return ResponseEntity.status(400).body(null); // 400 Bad Request for client errors
        }
    }

    // Retrieves all portfolios belonging to the current user
    @GetMapping("/user")
    public ResponseEntity<List<Portfolio>> getUserPortfolios() {
        log.info("Fetching portfolios for current user");
        try {
            Long userId = getCurrentUserId();
            List<Portfolio> portfolios = portfolioService.getPortfoliosByUserId(userId);
            return ResponseEntity.ok(portfolios);
        } catch (RuntimeException e) {
            log.error("Error fetching portfolios: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null); // 500 Internal Server Error for unexpected issues
        }
    }

    // Retrieves detailed information about a specific portfolio, including open and
    // closed positions and calculated metrics
    @GetMapping("/{portfolioId}")
    public ResponseEntity<Map<String, Object>> getPortfolioDetails(@PathVariable Long portfolioId) {
        log.info("Fetching portfolio details with id: {}", portfolioId);
        try {
            Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
            List<PortfolioHolding> openPositions = portfolioHoldingService.getOpenPositions(portfolioId);
            List<PortfolioHolding> closedPositions = portfolioHoldingService.getClosedPositions(portfolioId);

            // Calculate metrics for open positions
            double totalPortfolioValue = 0.0;
            double totalOpenGainLoss = 0.0;
            Map<Long, Double> openGainsLosses = new HashMap<>();
            Map<Long, Double> openPercentageReturns = new HashMap<>();
            for (PortfolioHolding holding : openPositions) {
                double currentValue = portfolioHoldingService.calculateCurrentValue(holding);
                double gainLoss = portfolioHoldingService.calculateGainLoss(holding);
                double percentageReturn = portfolioHoldingService.calculatePercentageReturn(holding);
                totalPortfolioValue += currentValue;
                totalOpenGainLoss += gainLoss;
                openGainsLosses.put(holding.getId(), gainLoss);
                openPercentageReturns.put(holding.getId(), percentageReturn);
            }

            // Calculate metrics for closed positions
            double totalClosedGainLoss = 0.0;
            Map<Long, Double> closedGainsLosses = new HashMap<>();
            Map<Long, Double> closedPercentageReturns = new HashMap<>();
            for (PortfolioHolding holding : closedPositions) {
                double gainLoss = portfolioHoldingService.calculateGainLoss(holding);
                double percentageReturn = portfolioHoldingService.calculatePercentageReturn(holding);
                totalClosedGainLoss += gainLoss;
                closedGainsLosses.put(holding.getId(), gainLoss);
                closedPercentageReturns.put(holding.getId(), percentageReturn);
            }

            // Assemble response map with all calculated data
            Map<String, Object> response = new HashMap<>();
            response.put("portfolio", portfolio);
            response.put("openPositions", openPositions);
            response.put("closedPositions", closedPositions);
            response.put("totalPortfolioValue", totalPortfolioValue);
            response.put("totalOpenGainLoss", totalOpenGainLoss);
            response.put("totalClosedGainLoss", totalClosedGainLoss);
            response.put("openGainsLosses", openGainsLosses);
            response.put("openPercentageReturns", openPercentageReturns);
            response.put("closedGainsLosses", closedGainsLosses);
            response.put("closedPercentageReturns", closedPercentageReturns);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error fetching portfolio details: {}", e.getMessage(), e);
            return ResponseEntity.status(404).body(null); // 404 Not Found if portfolio doesn't exist
        }
    }

    // Deletes a portfolio by its ID
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<String> deletePortfolio(@PathVariable Long portfolioId) {
        log.info("Deleting portfolio with id: {}", portfolioId);
        try {
            portfolioService.deletePortfolio(portfolioId);
            return ResponseEntity.ok("Portfolio deleted successfully");
        } catch (RuntimeException e) {
            log.error("Error deleting portfolio: {}", e.getMessage(), e);
            return ResponseEntity.status(404).body("Error deleting portfolio: " + e.getMessage());
        }
    }

    // Adds a new holding to a specified portfolio
    @PostMapping("/{portfolioId}/holdings/add")
    public ResponseEntity<PortfolioHolding> addHolding(
            @PathVariable Long portfolioId,
            @RequestParam String ticker,
            @RequestParam Double shares,
            @RequestParam Double purchasePrice,
            @RequestParam String purchaseDate,
            @RequestParam(required = false) Double sellingPrice,
            @RequestParam(required = false) String sellingDate) {
        log.info("Adding holding to portfolioId: {}, ticker: {}, shares: {}", portfolioId, ticker, shares);
        try {
            Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
            LocalDate purchaseLocalDate = LocalDate.parse(purchaseDate);
            LocalDate sellingLocalDate = sellingDate != null ? LocalDate.parse(sellingDate) : null; // Handle optional
                                                                                                    // selling date
            PortfolioHolding holding = portfolioHoldingService.addHolding(
                    portfolio, ticker, shares, purchasePrice, purchaseLocalDate, sellingPrice, sellingLocalDate);
            return ResponseEntity.ok(holding);
        } catch (RuntimeException e) {
            log.error("Error adding holding: {}", e.getMessage(), e);
            return ResponseEntity.status(400).body(null); // 400 Bad Request for invalid input
        }
    }

    // Updates an existing holding with new share count, selling price, and selling
    // date
    @PutMapping("/holdings/{holdingId}")
    public ResponseEntity<String> updateHolding(
            @PathVariable Long holdingId,
            @RequestParam Double shares,
            @RequestParam(required = false) Double sellingPrice,
            @RequestParam(required = false) String sellingDate) {
        log.info("Updating holding with id: {}, shares: {}", holdingId, shares);
        try {
            LocalDate sellingLocalDate = sellingDate != null ? LocalDate.parse(sellingDate) : null; // Parse optional
                                                                                                    // date
            portfolioHoldingService.updateHolding(holdingId, shares, sellingPrice, sellingLocalDate);
            return ResponseEntity.ok("Holding updated successfully");
        } catch (RuntimeException e) {
            log.error("Error updating holding: {}", e.getMessage(), e);
            return ResponseEntity.status(404).body("Error updating holding: " + e.getMessage());
        }
    }

    // Deletes a holding by its ID
    @DeleteMapping("/holdings/{holdingId}")
    public ResponseEntity<String> deleteHolding(@PathVariable Long holdingId) {
        log.info("Deleting holding with id: {}", holdingId);
        try {
            portfolioHoldingService.deleteHolding(holdingId);
            return ResponseEntity.ok("Holding deleted successfully");
        } catch (RuntimeException e) {
            log.error("Error deleting holding: {}", e.getMessage(), e);
            return ResponseEntity.status(404).body("Error deleting holding: " + e.getMessage());
        }
    }

    // Retrieves the ID of the current authenticated user (placeholder
    // implementation)
    private Long getCurrentUserId() {
        return 1L; // Replace with actual user ID retrieval from SecurityContextHolder or similar
    }
}