// package declaration
package com.PortfolioHeatmap.controllers;

// Existing imports
import com.PortfolioHeatmap.models.Portfolio;
import com.PortfolioHeatmap.models.PortfolioHolding;
import com.PortfolioHeatmap.models.User;
import com.PortfolioHeatmap.services.PortfolioHoldingService;
import com.PortfolioHeatmap.services.PortfolioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// New imports for JWT and user handling
import com.PortfolioHeatmap.security.JwtUtil;
import com.PortfolioHeatmap.services.UserService;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller class responsible for handling HTTP requests related to portfolio
 * management.
 * Provides endpoints for creating, retrieving, updating, and deleting
 * portfolios and their holdings.
 * Integrates with PortfolioService, PortfolioHoldingService, JwtUtil, and
 * UserService for functionality.
 *
 * @author Marvel Bana
 */
@RestController
@RequestMapping("/portfolios")
public class PortfolioController {
    private static final Logger log = LoggerFactory.getLogger(PortfolioController.class);

    private final PortfolioService portfolioService;
    private final PortfolioHoldingService portfolioHoldingService;
    private final JwtUtil jwtUtil; // Added for JWT parsing
    private final UserService userService; // Added for user lookup

    /**
     * Constructor for dependency injection of required services and utilities.
     * 
     * @param portfolioService        Service for portfolio operations
     * @param portfolioHoldingService Service for portfolio holding operations
     * @param jwtUtil                 Utility for JWT token handling
     * @param userService             Service for user operations
     */
    public PortfolioController(
            PortfolioService portfolioService,
            PortfolioHoldingService portfolioHoldingService,
            JwtUtil jwtUtil,
            UserService userService) {
        this.portfolioService = portfolioService;
        this.portfolioHoldingService = portfolioHoldingService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
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
            return ResponseEntity.status(500).body(null); // 500 Internal Server Error
        }
    }

    // Retrieves detailed information about a specific portfolio
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

            // Assemble response map
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
            return ResponseEntity.status(404).body(null); // 404 Not Found
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
            LocalDate sellingLocalDate = sellingDate != null ? LocalDate.parse(sellingDate) : null;
            PortfolioHolding holding = portfolioHoldingService.addHolding(
                    portfolio, ticker, shares, purchasePrice, purchaseLocalDate, sellingPrice, sellingLocalDate);
            return ResponseEntity.ok(holding);
        } catch (RuntimeException e) {
            log.error("Error adding holding: {}", e.getMessage(), e);
            return ResponseEntity.status(400).body(null); // 400 Bad Request
        }
    }

    // Updates an existing holding
    @PutMapping("/holdings/{holdingId}")
    public ResponseEntity<String> updateHolding(
            @PathVariable Long holdingId,
            @RequestParam Double shares,
            @RequestParam(required = false) Double sellingPrice,
            @RequestParam(required = false) String sellingDate) {
        log.info("Updating holding with id: {}, shares: {}", holdingId, shares);
        try {
            LocalDate sellingLocalDate = sellingDate != null ? LocalDate.parse(sellingDate) : null;
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

    // Gets details for a specific holding
    @GetMapping("/holdings/{holdingId}")
    public ResponseEntity<PortfolioHolding> getHolding(@PathVariable Long holdingId) {
        log.info("Fetching holding with id: {}", holdingId);
        try {
            PortfolioHolding holding = portfolioHoldingService.getHoldingById(holdingId);
            if (holding == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(holding);
        } catch (RuntimeException e) {
            log.error("Error fetching holding: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

/**
     * Retrieves the ID of the current authenticated user from the Authorization header.
     * Fetches the token directly from the request header, validates it with JwtUtil,
     * and retrieves the user ID via UserService.
     * 
     * @return Long The ID of the current user
     * @throws RuntimeException if token is missing or invalid
     */
    private Long getCurrentUserId() {
        // Get the current request attributes
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.error("No request attributes available to fetch Authorization header");
            throw new RuntimeException("Unable to determine current user");
        }

        // Get the Authorization header from the request
        String authHeader = attributes.getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("Authorization header missing or invalid: {}", authHeader);
            throw new RuntimeException("Invalid or missing Authorization header");
        }

        // Extract token and validate
        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);
        if (!jwtUtil.validateToken(token, username)) {
            log.error("Invalid JWT token for username: {}", username);
            throw new RuntimeException("Invalid JWT token");
        }

        // Fetch user and return ID
        User user = (User) userService.loadUserByUsername(username);
        return user.getId();
    }
}