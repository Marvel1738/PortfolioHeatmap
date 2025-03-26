package com.PortfolioHeatmap.services;

import com.PortfolioHeatmap.models.Portfolio;
import com.PortfolioHeatmap.models.PortfolioHolding;
import com.PortfolioHeatmap.models.PriceHistory;
import com.PortfolioHeatmap.models.Stock;
import com.PortfolioHeatmap.repositories.PortfolioHoldingRepository;

// Removed incorrect Optional import
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional; // Ensure the correct Optional is imported

@Service
public class PortfolioHoldingService {
    private static final Logger log = LoggerFactory.getLogger(PortfolioHoldingService.class);

    private final PortfolioHoldingRepository portfolioHoldingRepository;
    private final StockService stockService;
    private final PriceHistoryService priceHistoryService;

    public PortfolioHoldingService(PortfolioHoldingRepository portfolioHoldingRepository,
            StockService stockService,
            PriceHistoryService priceHistoryService) {
        this.portfolioHoldingRepository = portfolioHoldingRepository;
        this.stockService = stockService;
        this.priceHistoryService = priceHistoryService;
    }

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

    public void updateHolding(Long holdingId, Double shares, Double sellingPrice, LocalDate sellingDate) {
        log.info("Updating holding with id: {}, shares: {}, sellingPrice: {}, sellingDate: {}", holdingId, shares,
                sellingPrice, sellingDate);
        PortfolioHolding holding = portfolioHoldingRepository.findById(holdingId)
                .orElseThrow(() -> new RuntimeException("Holding not found with id: " + holdingId));
        holding.setShares(shares);
        holding.setSellingPrice(sellingPrice);
        holding.setSellingDate(sellingDate);
        portfolioHoldingRepository.save(holding);
    }

    public void deleteHolding(Long holdingId) {
        log.info("Deleting holding with id: {}", holdingId);
        if (!portfolioHoldingRepository.existsById(holdingId)) {
            throw new RuntimeException("Holding not found with id: " + holdingId);
        }
        portfolioHoldingRepository.deleteById(holdingId);
    }

    public List<PortfolioHolding> getOpenPositions(Long portfolioId) {
        log.info("Fetching open positions for portfolioId: {}", portfolioId);
        return portfolioHoldingRepository.findByPortfolioIdAndSellingDateIsNull(portfolioId);
    }

    public List<PortfolioHolding> getClosedPositions(Long portfolioId) {
        log.info("Fetching closed positions for portfolioId: {}", portfolioId);
        return portfolioHoldingRepository.findByPortfolioIdAndSellingDateIsNotNull(portfolioId);
    }

    public Double calculateGainLoss(PortfolioHolding holding) {
        if (holding.getSellingDate() != null) {
            // Closed position: Gain/loss = (selling price - purchase price) * shares
            return (holding.getSellingPrice() - holding.getPurchasePrice()) * holding.getShares();
        } else {
            // Open position: Gain/loss = (current price - purchase price) * shares
            PriceHistory latestPrice = priceHistoryService.getLatestPriceHistory(holding.getStock().getTicker());
            if (latestPrice == null) {
                log.warn("No price history found for ticker: {}", holding.getStock().getTicker());
                return 0.0;
            }
            return (latestPrice.getClosingPrice() - holding.getPurchasePrice()) * holding.getShares();
        }
    }

    public Double calculatePercentageReturn(PortfolioHolding holding) {
        Double gainLoss = calculateGainLoss(holding);
        Double purchaseValue = holding.getPurchasePrice() * holding.getShares();
        if (purchaseValue == 0) {
            return 0.0;
        }
        return (gainLoss / purchaseValue) * 100;
    }

    public Double calculateCurrentValue(PortfolioHolding holding) {
        if (holding.getSellingDate() != null) {
            // Closed position: Value at sale
            return holding.getSellingPrice() * holding.getShares();
        } else {
            // Open position: Current value
            PriceHistory latestPrice = priceHistoryService.getLatestPriceHistory(holding.getStock().getTicker());
            if (latestPrice == null) {
                log.warn("No price history found for ticker: {}", holding.getStock().getTicker());
                return 0.0;
            }
            return latestPrice.getClosingPrice() * holding.getShares();
        }
    }
}