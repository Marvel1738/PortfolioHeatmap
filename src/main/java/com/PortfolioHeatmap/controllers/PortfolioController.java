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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// New imports for JWT and user handling
import com.PortfolioHeatmap.security.JwtUtil;
import com.PortfolioHeatmap.services.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import com.PortfolioHeatmap.models.PriceHistory;
import com.PortfolioHeatmap.models.StockPrice;
import com.PortfolioHeatmap.services.PriceHistoryService;
import com.PortfolioHeatmap.services.StockDataService;
import com.PortfolioHeatmap.services.StockDataServiceFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
@CrossOrigin(origins = "http://localhost:3000")
public class PortfolioController {
    private static final Logger log = LoggerFactory.getLogger(PortfolioController.class);

    private final PortfolioService portfolioService;
    private final PortfolioHoldingService portfolioHoldingService;
    private final JwtUtil jwtUtil; // Added for JWT parsing
    private final UserService userService; // Added for user lookup
    private final PriceHistoryService priceHistoryService;
    private final StockDataService stockDataService; // New dependency

    /**
     * Constructor for dependency injection of required services and utilities.
     * 
     * @param portfolioService        Service for portfolio operations
     * @param portfolioHoldingService Service for portfolio holding operations
     * @param jwtUtil                 Utility for JWT token handling
     * @param userService             Service for user operations
     * @param priceHistoryService     Service for price history operations
     */
    public PortfolioController(
            PortfolioService portfolioService,
            PortfolioHoldingService portfolioHoldingService,
            JwtUtil jwtUtil,
            UserService userService,
            PriceHistoryService priceHistoryService,
            StockDataServiceFactory stockDataServiceFactory) {
        this.portfolioService = portfolioService;
        this.portfolioHoldingService = portfolioHoldingService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.priceHistoryService = priceHistoryService;
        this.stockDataService = stockDataServiceFactory.getService();
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
   @GetMapping("/{id}")
    public ResponseEntity<?> getPortfolioDetails(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1d") String timeframe,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long currentUserId = getCurrentUserId(authHeader);
            log.info("Fetching portfolio details with id: {} and timeframe: {}", id, timeframe);

            Portfolio portfolio = portfolioService.getPortfolioById(id);
            if (portfolio == null) {
                return ResponseEntity.notFound().build();
            }

            if (!portfolio.getUserId().equals(currentUserId)) {
                log.warn("Access denied - User {} attempted to access portfolio {} owned by user {}",
                        currentUserId, id, portfolio.getUserId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You do not have permission to access this portfolio");
            }

            List<PortfolioHolding> openPositions = portfolioHoldingService.getOpenPositions(id);
            List<PortfolioHolding> closedPositions = portfolioHoldingService.getClosedPositions(id);

            List<String> tickers = openPositions.stream()
                    .map(h -> h.getStock().getTicker())
                    .collect(Collectors.toList());
            List<StockPrice> currentPrices = stockDataService.getBatchStockPrices(tickers);
            Map<String, Double> currentPriceMap = currentPrices.stream()
                    .collect(Collectors.toMap(StockPrice::getSymbol, StockPrice::getPrice));

            Map<Long, Double> timeframePercentageChanges = new HashMap<>();
            double totalPortfolioValue = 0.0;

            for (PortfolioHolding holding : openPositions) {
                String ticker = holding.getStock().getTicker();
                Double currentPrice = currentPriceMap.getOrDefault(ticker, holding.getPurchasePrice());
                double currentValue = holding.getShares() * currentPrice;

                LocalDate startDate = getStartDateForTimeframe(timeframe);
                double startPrice;
                if (timeframe.equals("total")) {
                    startPrice = holding.getPurchasePrice();
                    log.info("Timeframe 'total' for {}: using purchasePrice={}", ticker, startPrice);
                } else {
                    Optional<PriceHistory> startPriceOpt = priceHistoryService.findByStockTickerAndDate(ticker,
                            startDate);
                    startPrice = startPriceOpt.map(PriceHistory::getClosingPrice).orElse(holding.getPurchasePrice());
                    log.info("Timeframe '{}' for {}: startDate={}, startPrice={}, foundInDB={}",
                            timeframe, ticker, startDate, startPrice, startPriceOpt.isPresent());
                }

                double timeframeChange = startPrice != 0
                        ? ((currentPrice - startPrice) / startPrice) * 100
                        : 0;
                timeframePercentageChanges.put(holding.getId(), timeframeChange);

                log.info("Holding {}: currentPrice={}, startPrice={}, timeframeChange={}",
                        ticker, currentPrice, startPrice, timeframeChange);

                totalPortfolioValue += currentValue;
            }

            // Closed positions logic (unchanged)
            Map<Long, Double> closedGainsLosses = new HashMap<>();
            Map<Long, Double> closedPercentageReturns = new HashMap<>();
            for (PortfolioHolding holding : closedPositions) {
                double gainLoss = (holding.getSellingPrice() - holding.getPurchasePrice()) * holding.getShares();
                double percentageReturn = ((holding.getSellingPrice() - holding.getPurchasePrice())
                        / holding.getPurchasePrice()) * 100;
                closedGainsLosses.put(holding.getId(), gainLoss);
                closedPercentageReturns.put(holding.getId(), percentageReturn);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("openPositions", openPositions);
            response.put("closedPositions", closedPositions);
            response.put("totalPortfolioValue", totalPortfolioValue);
            response.put("timeframePercentageChanges", timeframePercentageChanges);
            response.put("closedGainsLosses", closedGainsLosses);
            response.put("closedPercentageReturns", closedPercentageReturns);
            response.put("timeframe", timeframe);

            log.info("Returning response with {} open positions and {} closed positions",
                    openPositions.size(), closedPositions.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching portfolio details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching portfolio details: " + e.getMessage());
        }
    }

    // Helper method to determine start date for timeframe
    private LocalDate getStartDateForTimeframe(String timeframe) {
        LocalDate today = LocalDate.now();
        switch (timeframe) {
            case "1d": return today.minusDays(1);
            case "1w": return today.minusWeeks(1);
            case "1m": return today.minusMonths(1);
            case "3m": return today.minusMonths(3);
            case "6m": return today.minusMonths(6);
            case "ytd": return LocalDate.of(today.getYear(), 1, 1);
            case "1y": return today.minusYears(1);
            case "total": return LocalDate.of(1900, 1, 1); // Far back, will use purchasePrice
            default: return today.minusDays(1); // Default to 1d
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
     * Retrieves the ID of the current authenticated user from the Authorization
     * header.
     * Fetches the token directly from the request header, validates it with
     * JwtUtil,
     * and retrieves the user ID via UserService.
     * 
     * @return Long The ID of the current user
     * @throws RuntimeException if token is missing or invalid
     */
    private Long getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.error("No request attributes available to fetch Authorization header");
            throw new RuntimeException("Unable to determine current user");
        }
        String authHeader = attributes.getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("Authorization header missing or invalid: {}", authHeader);
            throw new RuntimeException("Invalid or missing Authorization header");
        }
        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);
        if (!jwtUtil.validateToken(token, username)) {
            log.error("Invalid JWT token for username: {}", username);
            throw new RuntimeException("Invalid JWT token");
        }
        User user = (User) userService.loadUserByUsername(username);
        return user.getId();
    }

    private Long getCurrentUserId(String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        return userService.getUserByUsername(username).getId();
    }
}