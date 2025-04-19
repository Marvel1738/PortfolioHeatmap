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
@CrossOrigin(origins = "http://localhost:3000", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH,
        RequestMethod.DELETE,
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
            boolean hasHoldingsWithPurchasePrices = false;
            for (PortfolioHolding holding : openPositions) {
                String ticker = holding.getStock().getTicker();
                Double currentPrice = currentPriceMap.getOrDefault(ticker,
                        holding.getPurchasePrice() != null ? holding.getPurchasePrice() : 0.0);
                double currentValue = holding.getShares() * currentPrice;
                double initialValue = holding.getPurchasePrice() != null
                        ? holding.getShares() * holding.getPurchasePrice()
                        : 0;
                totalPortfolioValue += currentValue;
                totalInitialValue += initialValue;
                double startPrice;
                LocalDate effectiveStartDate;
                boolean usedPurchasePriceFallback = false;

                if (timeframe.equals("total")) {
                    startPrice = holding.getPurchasePrice() != null ? holding.getPurchasePrice() : 0.0;
                    effectiveStartDate = null;
                    log.info("Ticker: {}, timeframe: total, date: N/A (purchase), oldPrice: ${}, currentPrice: ${}",
                            ticker, startPrice, currentPrice);

                    // Skip calculation only for total timeframe when purchase price is null
                    if (holding.getPurchasePrice() == null) {
                        log.warn(
                                "Ticker: {}, timeframe: total, skipping calculation as purchase price is null and required for total timeframe",
                                ticker);
                        timeframePercentageChanges.put(holding.getId(), null);
                        continue;
                    }
                } else {
                    LocalDate initialStartDate = getStartDateForTimeframe(timeframe);
                    Optional<PriceHistory> startPriceOpt = Optional.empty();
                    effectiveStartDate = initialStartDate;

                    // Special handling for 1-day timeframe on market holidays
                    if (timeframe.equals("1d")) {
                        // First try to get yesterday's price
                        startPriceOpt = priceHistoryService.findByStockTickerAndDate(ticker, effectiveStartDate);

                        // If we found yesterday's price and it matches current price (market closed),
                        // go back one more day
                        if (startPriceOpt.isPresent() &&
                                Math.abs(startPriceOpt.get().getClosingPrice() - currentPrice) < 0.0001) {
                            log.info(
                                    "Market appears closed - current price matches yesterday's closing price for {}. Going back one more day.",
                                    ticker);
                            effectiveStartDate = effectiveStartDate.minusDays(1);
                            startPriceOpt = priceHistoryService.findByStockTickerAndDate(ticker, effectiveStartDate);
                        }
                    }

                    // For other timeframes or if we need to look further back
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
                        // For timeframes other than "total", when no price history is found:
                        // - Use purchase price as fallback if available
                        // - Use 0.0 as fallback if purchase price is null (this will result in a 0%
                        // change for new holdings)
                        startPrice = holding.getPurchasePrice() != null ? holding.getPurchasePrice() : 0.0;
                        // Only mark as using purchase price fallback if purchase price is not null
                        usedPurchasePriceFallback = (holding.getPurchasePrice() != null);
                        String fallbackType = holding.getPurchasePrice() != null
                                ? "purchasePrice: $" + String.format("%.2f", startPrice)
                                : "default value of 0.0 (will calculate change from zero)";

                        log.warn(
                                "Ticker: {}, timeframe: {}, date: {} - No data after 3 days back from {}, falling back to {}",
                                ticker, timeframe, effectiveStartDate, initialStartDate, fallbackType);
                    }
                }

                double timeframeChange;
                if (startPrice == 0.0 && holding.getPurchasePrice() == null && !timeframe.equals("total")) {
                    // For non-total timeframes with null purchase price and no history, show 0%
                    // change
                    // rather than infinity (from dividing by zero)
                    timeframeChange = 0.0;
                    log.info("Using 0% change for ticker {} with no purchase price and no history data", ticker);
                } else {
                    timeframeChange = startPrice != 0
                            ? ((currentPrice - startPrice) / startPrice) * 100
                            : 0;
                }
                timeframePercentageChanges.put(holding.getId(), timeframeChange);
                String changeSign = timeframeChange >= 0 ? "+" : "-";
                log.info(
                        "Ticker: {}, timeframe: {}, date: {}, oldPrice: ${}, currentPrice: ${}, timeFrameChange: {}{}%{}",
                        ticker, timeframe, timeframe.equals("total") ? "N/A (purchase)" : effectiveStartDate,
                        String.format("%.2f", startPrice), String.format("%.2f", currentPrice),
                        changeSign, String.format("%.2f", Math.abs(timeframeChange)),
                        usedPurchasePriceFallback ? " (using purchase price)" : "");
            }
            double totalPortfolioValueWithPrices = 0;
            double totalInitialValueWithPrices = 0;
            hasHoldingsWithPurchasePrices = false;

            for (PortfolioHolding holding : openPositions) {
                if (holding.getPurchasePrice() != null) {
                    double currentPrice = priceHistoryService
                            .findTopByStockTickerOrderByDateDesc(holding.getStock().getTicker())
                            .map(PriceHistory::getClosingPrice)
                            .orElse(holding.getPurchasePrice());
                    totalPortfolioValueWithPrices += holding.getShares() * currentPrice;
                    totalInitialValueWithPrices += holding.getShares() * holding.getPurchasePrice();
                    hasHoldingsWithPurchasePrices = true;
                } else {
                    double currentPrice = currentPriceMap.getOrDefault(holding.getStock().getTicker(), 0.0);
                    totalPortfolioValue += holding.getShares() * currentPrice;
                }
            }

            Map<Long, Double> closedGainsLosses = new HashMap<>();
            Map<Long, Double> closedPercentageReturns = new HashMap<>();

            for (PortfolioHolding holding : closedPositions) {
                if (holding.getPurchasePrice() != null) {
                    double gainLoss = (holding.getSellingPrice() - holding.getPurchasePrice()) * holding.getShares();
                    double percentageReturn = holding.getPurchasePrice() != 0
                            ? ((holding.getSellingPrice() - holding.getPurchasePrice()) / holding.getPurchasePrice())
                                    * 100
                            : 0;
                    closedGainsLosses.put(holding.getId(), gainLoss);
                    closedPercentageReturns.put(holding.getId(), percentageReturn);
                    totalPortfolioValueWithPrices += holding.getShares() * holding.getSellingPrice();
                    totalInitialValueWithPrices += holding.getShares() * holding.getPurchasePrice();
                    hasHoldingsWithPurchasePrices = true;
                } else {
                    closedGainsLosses.put(holding.getId(), null);
                    closedPercentageReturns.put(holding.getId(), null);

                    totalPortfolioValue += holding.getShares() * holding.getSellingPrice();
                }
            }

            totalPortfolioValue = openPositions.stream()
                    .mapToDouble(h -> h.getShares() * currentPriceMap.getOrDefault(h.getStock().getTicker(), 0.0))
                    .sum();

            Double totalPercentageReturn = null;
            Double totalDollarReturn = null;

            log.debug("Initial value calculation:");
            openPositions.stream()
                    .filter(h -> h.getPurchasePrice() != null && h.getPurchasePrice() > 0)
                    .forEach(h -> {
                        log.debug("  Holding {}: {} shares × ${} = ${}",
                                h.getStock().getTicker(),
                                h.getShares(),
                                String.format("%.2f", h.getPurchasePrice()),
                                String.format("%.2f", h.getShares() * h.getPurchasePrice()));
                    });

            log.debug("Total initial value with valid prices: ${}", String.format("%.2f", totalInitialValueWithPrices));

            log.debug("Current value calculation:");
            openPositions.forEach(h -> {
                Double currentPrice = currentPriceMap.getOrDefault(h.getStock().getTicker(), 0.0);
                log.debug("  Holding {}: {} shares × ${} = ${}",
                        h.getStock().getTicker(),
                        h.getShares(),
                        String.format("%.2f", currentPrice),
                        String.format("%.2f", h.getShares() * currentPrice));
            });

            log.debug("Total current portfolio value: ${}", String.format("%.2f", totalPortfolioValue));

            if (totalInitialValueWithPrices > 0) {
                totalDollarReturn = totalPortfolioValue - totalInitialValueWithPrices;
                totalPercentageReturn = (totalDollarReturn / totalInitialValueWithPrices) * 100;

                log.debug("Return calculation:");
                log.debug("  Dollar return: ${} - ${} = ${}",
                        String.format("%.2f", totalPortfolioValue),
                        String.format("%.2f", totalInitialValueWithPrices),
                        String.format("%.2f", totalDollarReturn));
                log.debug("  Percentage return: ({} / {}) × 100 = {}%",
                        String.format("%.2f", totalDollarReturn),
                        String.format("%.2f", totalInitialValueWithPrices),
                        String.format("%.2f", totalPercentageReturn));
            } else {
                log.debug("Total initial value is zero or negative ({}), skipping return calculation",
                        totalInitialValueWithPrices);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("openPositions", openPositions);
            response.put("closedPositions", closedPositions);
            response.put("totalPortfolioValue", totalPortfolioValue);
            response.put("portfolioHoldings", openPositions);
            response.put("totalPercentageReturn", totalPercentageReturn);
            response.put("totalDollarReturn", totalDollarReturn);
            response.put("timeframePercentageChanges", timeframePercentageChanges);
            response.put("closedGainsLosses", closedGainsLosses);
            response.put("closedPercentageReturns", closedPercentageReturns);
            response.put("timeframe", timeframe);
            log.info(
                    "Returning portfolio response with {} holdings, totalValue: ${}, percentReturn: {}%, dollarReturn: ${}",
                    openPositions.size(),
                    String.format("%.2f", totalPortfolioValue),
                    totalPercentageReturn != null ? String.format("%.2f", totalPercentageReturn) : "null",
                    totalDollarReturn != null ? String.format("%.2f", totalDollarReturn) : "null");

            log.debug("Portfolio response details - ID: {}, Name: {}, OpenPositions: {}, ClosedPositions: {}, " +
                    "TotalValue: {}, HasValidInitialValue: {}, TotalInitialValue: {}, " +
                    "PercentReturn: {}, DollarReturn: {}",
                    portfolio.getId(),
                    portfolio.getName(),
                    openPositions.size(),
                    closedPositions.size(),
                    totalPortfolioValue > 0 ? String.format("$%.2f", totalPortfolioValue) : "$0.00",
                    hasHoldingsWithPurchasePrices,
                    totalInitialValueWithPrices > 0 ? String.format("$%.2f", totalInitialValueWithPrices) : "$0.00",
                    totalPercentageReturn != null ? String.format("%.2f%%", totalPercentageReturn) : "null",
                    totalDollarReturn != null ? String.format("$%.2f", totalDollarReturn) : "null");

            openPositions.forEach(holding -> {
                String ticker = holding.getStock().getTicker();
                Double currentPrice = currentPriceMap.getOrDefault(ticker, 0.0);
                log.debug(
                        "Holding: {} - Ticker: {}, Shares: {}, PurchasePrice: {}, CurrentPrice: {}, CurrentValue: {}, "
                                +
                                "TimeframeChange: {}",
                        holding.getId(),
                        ticker,
                        holding.getShares(),
                        holding.getPurchasePrice() != null ? String.format("$%.2f", holding.getPurchasePrice())
                                : "null",
                        String.format("$%.2f", currentPrice),
                        String.format("$%.2f", holding.getShares() * currentPrice),
                        timeframePercentageChanges.get(holding.getId()) != null
                                ? String.format("%.2f%%", timeframePercentageChanges.get(holding.getId()))
                                : "null");
            });

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
            @RequestParam(required = false) Double purchasePrice,
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

    @GetMapping("/api/portfolio/holdings/purchase-price")
    public ResponseEntity<?> getPurchasePrice(
            @RequestParam String ticker,
            @RequestParam Long portfolioId,
            @RequestHeader("Authorization") String token) {
        try {
            log.info("Getting purchase price for ticker: {} in portfolio: {}", ticker, portfolioId);

            // Validate token and get user
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            log.debug("Extracted username from token: {}", username);

            User user = userService.getUserByUsername(username);
            log.debug("Found user with ID: {}", user.getId());

            // Get the portfolio
            Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
            log.debug("Found portfolio with ID: {} and user ID: {}", portfolio.getId(), portfolio.getUserId());

            // Verify portfolio belongs to user
            if (!portfolio.getUserId().equals(user.getId())) {
                log.warn("Portfolio {} does not belong to user {}", portfolioId, user.getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Portfolio does not belong to user");
            }

            // Find the holding with matching ticker
            log.debug("Searching for holding with ticker: {} in portfolio: {}", ticker, portfolioId);
            Optional<PortfolioHolding> holding = portfolioHoldingService.findByPortfolioIdAndStockTicker(portfolioId,
                    ticker);

            if (holding.isPresent()) {
                Double purchasePrice = holding.get().getPurchasePrice();
                log.info("Found purchase price for {} in portfolio {}: {}", ticker, portfolioId, purchasePrice);
                return ResponseEntity.ok(purchasePrice);
            } else {
                log.info("No holding found for ticker {} in portfolio {}", ticker, portfolioId);
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            log.error("Error getting purchase price for ticker: {} in portfolio: {}", ticker, portfolioId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error getting purchase price");
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