package com.PortfolioHeatmap.services;

/**
 * Service class for managing portfolio holdings in the PortfolioHeatmap application.
 * This class provides methods to add, update, delete, and retrieve portfolio holdings,
 * as well as calculate financial metrics such as gain/loss, percentage return, and current value.
 *
 * @author Marvel Bana
 */
import com.PortfolioHeatmap.models.Portfolio;
import com.PortfolioHeatmap.models.PortfolioHolding;
import com.PortfolioHeatmap.models.PriceHistory;
import com.PortfolioHeatmap.models.Stock;
import com.PortfolioHeatmap.repositories.PortfolioHoldingRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PortfolioHoldingService {
    private static final Logger log = LoggerFactory.getLogger(PortfolioHoldingService.class);

    private final PortfolioHoldingRepository portfolioHoldingRepository;
    private final StockService stockService;
    private final PriceHistoryService priceHistoryService;

    // Constructor for dependency injection of required repositories and services
    public PortfolioHoldingService(PortfolioHoldingRepository portfolioHoldingRepository,
            StockService stockService,
            PriceHistoryService priceHistoryService) {
        this.portfolioHoldingRepository = portfolioHoldingRepository;
        this.stockService = stockService;
        this.priceHistoryService = priceHistoryService;
    }

    // Adds a new holding to a portfolio with the specified details
    public PortfolioHolding addHolding(Portfolio portfolio, String ticker, Double shares,
            Double purchasePrice, LocalDate purchaseDate,
            Double sellingPrice, LocalDate sellingDate) {
        log.info("Adding holding to portfolioId: {}, ticker: {}, shares: {}", portfolio.getId(), ticker, shares);
        Stock stock = stockService.getStockById(ticker);
        if (stock == null) {
            throw new RuntimeException("Stock not found with ticker: " + ticker);
        }

        Optional<PortfolioHolding> existingHolding = portfolioHoldingRepository
                .findByPortfolioIdAndStock(portfolio.getId(), stock);
        if (existingHolding.isPresent()) {
            throw new RuntimeException("Stock " + ticker + " already exists in portfolio " + portfolio.getId());
        }

        PortfolioHolding holding = new PortfolioHolding();
        holding.setPortfolio(portfolio);
        holding.setStock(stock);
        holding.setShares(shares);
        holding.setPurchasePrice(purchasePrice);
        holding.setPurchaseDate(purchaseDate);
        holding.setSellingPrice(sellingPrice);
        holding.setSellingDate(sellingDate);
        return portfolioHoldingRepository.save(holding);
    }

    // Updates an existing holding with new share count, selling price, and selling
    // date
    public void updateHolding(Long holdingId, Double shares, Double sellingPrice, LocalDate sellingDate) {
        log.info("Updating holding with id: {}, shares: {}, sellingPrice: {}, sellingDate: {}", holdingId, shares,
                sellingPrice, sellingDate);
        PortfolioHolding holding = portfolioHoldingRepository.findById(holdingId)
                .orElseThrow(() -> new RuntimeException("Holding not found with id: " + holdingId));

        // If shares are 0 or less, delete the holding
        if (shares <= 0) {
            log.info("Shares reduced to 0, deleting holding with id: {}", holdingId);
            portfolioHoldingRepository.deleteById(holdingId);
            return;
        }

        holding.setShares(shares);
        holding.setSellingPrice(sellingPrice);
        holding.setSellingDate(sellingDate);
        portfolioHoldingRepository.save(holding);
    }

    // Deletes a holding by its ID
    public void deleteHolding(Long holdingId) {
        log.info("Deleting holding with id: {}", holdingId);
        if (!portfolioHoldingRepository.existsById(holdingId)) {
            throw new RuntimeException("Holding not found with id: " + holdingId);
        }
        portfolioHoldingRepository.deleteById(holdingId);
    }

    // Retrieves all open positions (holdings with no selling date) for a portfolio
    public List<PortfolioHolding> getOpenPositions(Long portfolioId) {
        log.info("Fetching open positions for portfolioId: {}", portfolioId);
        return portfolioHoldingRepository.findByPortfolioIdAndSellingDateIsNull(portfolioId);
    }

    // Retrieves all closed positions (holdings with a selling date) for a portfolio
    public List<PortfolioHolding> getClosedPositions(Long portfolioId) {
        log.info("Fetching closed positions for portfolioId: {}", portfolioId);
        return portfolioHoldingRepository.findByPortfolioIdAndSellingDateIsNotNull(portfolioId);
    }

    // Calculates the gain or loss for a holding based on its status (open or
    // closed)
    public Double calculateGainLoss(PortfolioHolding holding) {
        // If purchase price is null, we can't calculate gain/loss
        if (holding.getPurchasePrice() == null) {
            return null;
        }

        if (holding.getSellingDate() != null) {
            // Closed position: Gain/loss = (selling price - purchase price) * shares
            return (holding.getSellingPrice() - holding.getPurchasePrice()) * holding.getShares();
        } else {
            // Open position: Gain/loss = (current price - purchase price) * shares
            Optional<PriceHistory> latestPrice = priceHistoryService
                    .findTopByStockTickerOrderByDateDesc(holding.getStock().getTicker());
            if (latestPrice.isEmpty()) {
                log.warn("No price history found for ticker: {}", holding.getStock().getTicker());
                return 0.0;
            }
            return (latestPrice.get().getClosingPrice() - holding.getPurchasePrice()) * holding.getShares();
        }
    }

    // Calculates the percentage return for a holding based on gain/loss and
    // purchase value
    public Double calculatePercentageReturn(PortfolioHolding holding) {
        // If purchase price is null, we can't calculate percentage return
        if (holding.getPurchasePrice() == null) {
            return null;
        }

        Double gainLoss = calculateGainLoss(holding);
        // If gainLoss is null (which could happen if purchase price is null), return
        // null
        if (gainLoss == null) {
            return null;
        }

        Double purchaseValue = holding.getPurchasePrice() * holding.getShares();
        if (purchaseValue == 0) {
            return 0.0;
        }
        return (gainLoss / purchaseValue) * 100;
    }

    // Calculates the current value of a holding (based on selling price for closed,
    // current price for open)
    public Double calculateCurrentValue(PortfolioHolding holding) {
        if (holding.getStock().getTicker().equalsIgnoreCase("Cash")) {
            // For cash holdings, return the exact share amount as the value
            return holding.getShares();
        } else if (holding.getSellingDate() != null) {
            // Closed position: Value at sale
            return holding.getSellingPrice() * holding.getShares();
        } else {
            // Open position: Current value
            Optional<PriceHistory> latestPrice = priceHistoryService
                    .findTopByStockTickerOrderByDateDesc(holding.getStock().getTicker());
            if (latestPrice.isEmpty()) {
                log.warn("No price history found for ticker: {}", holding.getStock().getTicker());
                return 0.0;
            }
            return latestPrice.get().getClosingPrice() * holding.getShares();
        }
    }

    public PortfolioHolding getHoldingById(Long holdingId) {
        log.info("Fetching holding with id: {}", holdingId);
        return portfolioHoldingRepository.findById(holdingId)
                .orElseThrow(() -> new RuntimeException("Holding not found with id: " + holdingId));
    }

    public Optional<PortfolioHolding> findByPortfolioIdAndStockTicker(Long portfolioId, String ticker) {
        return portfolioHoldingRepository.findByPortfolioIdAndStockTicker(portfolioId, ticker);
    }
}