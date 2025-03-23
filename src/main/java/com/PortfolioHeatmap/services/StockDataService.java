package com.PortfolioHeatmap.services;

/**
 * Defines the contract for stock data services that fetch stock price information.
 * Implementations of this interface (e.g., FMPStockDataService, AlphaVantageStockDataService)
 * provide methods to retrieve stock prices for individual symbols or in batches.
 * 
 * @author [Marvel Bana]
 */
import java.util.List;

import com.PortfolioHeatmap.models.HistoricalPrice;
import com.PortfolioHeatmap.models.StockPrice;

public interface StockDataService {
    // Fetches the current stock price for a given symbol.
    StockPrice getStockPrice(String symbol);

    // Fetches stock prices for a list of symbols in a batch request.
    List<StockPrice> getBatchStockPrices(List<String> symbols);

    // Fetches historical price data for a given symbol.
    // Returns a list of HistoricalPrice objects representing daily prices.
    List<HistoricalPrice> getHistoricalPrices(String symbol);
}