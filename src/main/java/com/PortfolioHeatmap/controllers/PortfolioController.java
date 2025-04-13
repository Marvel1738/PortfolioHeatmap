package com.PortfolioHeatmap.controllers;

import com.PortfolioHeatmap.models.FMPSP500ConstituentResponse;
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

import com.PortfolioHeatmap.security.JwtUtil;
import com.PortfolioHeatmap.services.UserService;
import com.PortfolioHeatmap.models.PriceHistory;
import com.PortfolioHeatmap.models.StockPrice;
import com.PortfolioHeatmap.services.PriceHistoryService;
import com.PortfolioHeatmap.services.StockDataService;
import com.PortfolioHeatmap.services.StockDataServiceFactory;
import com.PortfolioHeatmap.repositories.PriceHistoryRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import org.springframework.data.domain.Pageable;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/portfolios")
@CrossOrigin(origins = "http://localhost:3000", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH, RequestMethod.DELETE,
        RequestMethod.OPTIONS }, allowedHeaders = { "Authorization", "Content-Type" }, allowCredentials = "true")
public class PortfolioController {
    private static final Logger log = LoggerFactory.getLogger(PortfolioController.class);

    private final PortfolioService portfolioService;
    private final PortfolioHoldingService portfolioHoldingService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final PriceHistoryService priceHistoryService;
    private final StockDataService stockDataService;
    private final PriceHistoryRepository priceHistoryRepository;

    public PortfolioController(
            PortfolioService portfolioService,
            PortfolioHoldingService portfolioHoldingService,
            JwtUtil jwtUtil,
            UserService userService,
            PriceHistoryService priceHistoryService,
            StockDataServiceFactory stockDataServiceFactory,
            PriceHistoryRepository priceHistoryRepository) {
        this.portfolioService = portfolioService;
        this.portfolioHoldingService = portfolioHoldingService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.priceHistoryService = priceHistoryService;
        this.stockDataService = stockDataServiceFactory.getService();
        this.priceHistoryRepository = priceHistoryRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<Portfolio> createPortfolio(@RequestParam String name) {
        log.info("Creating portfolio with name: {}", name);
        try {
            Long userId = getCurrentUserId();
            Portfolio portfolio = portfolioService.createPortfolio(userId, name);
            return ResponseEntity.ok(portfolio);
        } catch (RuntimeException e) {
            log.error("Error creating portfolio: {}", e.getMessage(), e);
            return ResponseEntity.status(400).body(null);
        }
    }

    @PostMapping("/create-random")
    public ResponseEntity<Portfolio> createRandomPortfolio(@RequestParam(required = false) String name) {
        log.info("Creating random portfolio with name: {}", name);
        try {
            Long userId = getCurrentUserId();
            String portfolioName = name != null && !name.trim().isEmpty()
                    ? name
                    : "Random Portfolio " + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            Portfolio portfolio = portfolioService.createPortfolio(userId, portfolioName);
            Random random = new Random();
            int numStocks = random.nextInt(50) + 1;
            log.info("Generating {} random holdings for portfolio {}", numStocks, portfolio.getId());
            List<String> allTickers = stockDataService.getSP500Constituents().stream()
                    .map(FMPSP500ConstituentResponse::getSymbol)
                    .collect(Collectors.toList());
            if (allTickers.isEmpty()) {
                log.error("No S&P 500 tickers found");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(null);
            }
            List<String> selectedTickers = allTickers;
            Collections.shuffle(selectedTickers, random);
            selectedTickers = selectedTickers.stream()
                    .limit(Math.min(numStocks, selectedTickers.size()))
                    .collect(Collectors.toList());
            for (String ticker : selectedTickers) {
                List<PriceHistory> priceHistories = priceHistoryRepository
                        .findByStockTickerOrderByDateDesc(ticker, Pageable.unpaged()).getContent();
                Double purchasePrice;
                if (!priceHistories.isEmpty()) {
                    PriceHistory randomPrice = priceHistories.get(random.nextInt(priceHistories.size()));
                    purchasePrice = randomPrice.getClosingPrice();
                } else {
                    StockPrice currentPrice = stockDataService.getStockPrice(ticker);
                    purchasePrice = currentPrice != null ? currentPrice.getPrice() : 100.0;
                }
                double shares = random.nextInt(50) + 1;
                portfolioHoldingService.addHolding(
                        portfolio,
                        ticker,
                        shares,
                        purchasePrice,
                        LocalDate.now(),
                        null,
                        null);
                log.info("Added holding: ticker={}, shares={}, price=${}", ticker, shares, purchasePrice);
            }
            log.info("Random portfolio created successfully: id={}, name={}", portfolio.getId(), portfolioName);
            return ResponseEntity.ok(portfolio);
        } catch (RuntimeException e) {
            log.error("Error creating random portfolio: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<List<Portfolio>> getUserPortfolios() {
        log.info("Fetching portfolios for current user");
        try {
            Long userId = getCurrentUserId();
            List<Portfolio> portfolios = portfolioService.getPortfoliosByUserId(userId);
            return ResponseEntity.ok(portfolios);
        } catch (RuntimeException e) {
            log.error("Error fetching portfolios: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

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
            double totalInitialValue = 0.0;
            for (PortfolioHolding holding : openPositions) {
                String ticker = holding.getStock().getTicker();
                Double currentPrice = currentPriceMap.getOrDefault(ticker, holding.getPurchasePrice());
                double currentValue = holding.getShares() * currentPrice;
                double initialValue = holding.getShares() * holding.getPurchasePrice();
                totalPortfolioValue += currentValue;
                totalInitialValue += initialValue;
                double startPrice;
                LocalDate effectiveStartDate;
                boolean usedPurchasePriceFallback = false;
                if (timeframe.equals("total")) {
                    startPrice = holding.getPurchasePrice();
                    effectiveStartDate = null;
                    log.info("Ticker: {}, timeframe: total, date: N/A (purchase), oldPrice: ${}, currentPrice: ${}",
                            ticker, startPrice, currentPrice);
                } else {
                    LocalDate initialStartDate = getStartDateForTimeframe(timeframe);
                    Optional<PriceHistory> startPriceOpt = Optional.empty();
                    effectiveStartDate = initialStartDate;
                    for (int daysBack = 0; daysBack <= 3 && !startPriceOpt.isPresent(); daysBack++) {
                        effectiveStartDate = initialStartDate.minusDays(daysBack);
                        startPriceOpt = priceHistoryService.findByStockTickerAndDate(ticker, effectiveStartDate);
                        log.info("Ticker: {}, timeframe: {}, checking date: {}, found: {}",
                                ticker, timeframe, effectiveStartDate, startPriceOpt.isPresent());
                    }
                    if (startPriceOpt.isPresent()) {
                        startPrice = startPriceOpt.get().getClosingPrice();
                        log.info("Ticker: {}, timeframe: {}, date: {}, oldPrice: ${}, currentPrice: ${}",
                                ticker, timeframe, effectiveStartDate, startPrice, currentPrice);
                    } else {
                        startPrice = holding.getPurchasePrice();
                        usedPurchasePriceFallback = true;
                        log.warn(
                                "Ticker: {}, timeframe: {}, date: {} - No data after 3 days back from {}, falling back to purchasePrice: ${}, currentPrice: ${}",
                                ticker, timeframe, effectiveStartDate, initialStartDate, startPrice, currentPrice);
                    }
                }
                double timeframeChange = startPrice != 0
                        ? ((currentPrice - startPrice) / startPrice) * 100
                        : 0;
                timeframePercentageChanges.put(holding.getId(), timeframeChange);
                String changeSign = timeframeChange >= 0 ? "+" : "-";
                log.info(
                        "Ticker: {}, timeframe: {}, date: {}, oldPrice: ${}, currentPrice: ${}, timeFrameChange: {}{}%{}",
                        ticker, timeframe, timeframe.equals("total") ? "N/A (purchase)" : effectiveStartDate,
                        String.format("%.2f", startPrice), String.format("%.2f", currentPrice),
                        changeSign, String.format("%.2f", Math.abs(timeframeChange)),
                        usedPurchasePriceFallback ? " (using purchase price)" : "");
            }
            double totalPercentageReturn = totalInitialValue != 0
                    ? ((totalPortfolioValue - totalInitialValue) / totalInitialValue) * 100
                    : 0.0;
            Map<Long, Double> closedGainsLosses = new HashMap<>();
            Map<Long, Double> closedPercentageReturns = new HashMap<>();
            for (PortfolioHolding holding : closedPositions) {
                double gainLoss = (holding.getSellingPrice() - holding.getPurchasePrice()) * holding.getShares();
                double percentageReturn = holding.getPurchasePrice() != 0
                        ? ((holding.getSellingPrice() - holding.getPurchasePrice()) / holding.getPurchasePrice()) * 100
                        : 0;
                closedGainsLosses.put(holding.getId(), gainLoss);
                closedPercentageReturns.put(holding.getId(), percentageReturn);
                totalPortfolioValue += gainLoss;
                totalInitialValue += holding.getShares() * holding.getPurchasePrice();
            }
            totalPercentageReturn = totalInitialValue != 0
                    ? ((totalPortfolioValue - totalInitialValue) / totalInitialValue) * 100
                    : 0.0;
            double totalDollarReturn = totalPortfolioValue - totalInitialValue;
            Map<String, Object> response = new HashMap<>();
            response.put("openPositions", openPositions);
            response.put("closedPositions", closedPositions);
            response.put("totalPortfolioValue", totalPortfolioValue);
            response.put("totalPercentageReturn", totalPercentageReturn);
            response.put("totalDollarReturn", totalDollarReturn);
            response.put("timeframePercentageChanges", timeframePercentageChanges);
            response.put("closedGainsLosses", closedGainsLosses);
            response.put("closedPercentageReturns", closedPercentageReturns);
            response.put("timeframe", timeframe);
            log.info(
                    "Portfolio {} response prepared - totalValue: ${}, totalPercentageReturn: {}%, timeframeChanges: {}, totalDollarReturn: ${}",
                    id, String.format("%.2f", totalPortfolioValue), String.format("%.2f", totalPercentageReturn),
                    timeframePercentageChanges, String.format("%.2f", totalDollarReturn));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching portfolio details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching portfolio details: " + e.getMessage());
        }
    }

    @PatchMapping("/{portfolioId}/rename")
    public ResponseEntity<Portfolio> renamePortfolio(
            @PathVariable Long portfolioId,
            @RequestParam String name) {
        log.info("Renaming portfolio with id: {} to name: {}", portfolioId, name);
        try {
            Long userId = getCurrentUserId();
            Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
            if (portfolio == null) {
                return ResponseEntity.notFound().build();
            }
            if (!portfolio.getUserId().equals(userId)) {
                log.warn("Access denied - User {} attempted to rename portfolio {} owned by user {}",
                        userId, portfolioId, portfolio.getUserId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            Portfolio updatedPortfolio = portfolioService.renamePortfolio(portfolioId, name);
            return ResponseEntity.ok(updatedPortfolio);
        } catch (RuntimeException e) {
            log.error("Error renaming portfolio: {}", e.getMessage(), e);
            return ResponseEntity.status(400).body(null);
        }
    }

    @PatchMapping("/{portfolioId}/favorite")
    public ResponseEntity<Portfolio> setFavoritePortfolio(
            @PathVariable Long portfolioId,
            @RequestParam boolean isFavorite) {
        log.info("Setting favorite status for portfolio id: {} to {}", portfolioId, isFavorite);
        try {
            Long userId = getCurrentUserId();
            Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
            if (portfolio == null) {
                return ResponseEntity.notFound().build();
            }
            if (!portfolio.getUserId().equals(userId)) {
                log.warn("Access denied - User {} attempted to favorite portfolio {} owned by user {}",
                        userId, portfolioId, portfolio.getUserId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            Portfolio updatedPortfolio = portfolioService.setFavoritePortfolio(portfolioId, isFavorite);
            return ResponseEntity.ok(updatedPortfolio);
        } catch (RuntimeException e) {
            log.error("Error setting favorite portfolio: {}", e.getMessage(), e);
            return ResponseEntity.status(400).body(null);
        }
    }

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
            return ResponseEntity.status(400).body(null);
        }
    }

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

    private LocalDate getStartDateForTimeframe(String timeframe) {
        LocalDate today = LocalDate.now();
        switch (timeframe) {
            case "1d":
                return today.minusDays(1);
            case "1w":
                return today.minusWeeks(1);
            case "1m":
                return today.minusMonths(1);
            case "3m":
                return today.minusMonths(3);
            case "6m":
                return today.minusMonths(6);
            case "ytd":
                return LocalDate.of(today.getYear(), 1, 1);
            case "1y":
                return today.minusYears(1);
            case "total":
                return LocalDate.of(1900, 1, 1);
            default:
                return today.minusDays(1);
        }
    }
}