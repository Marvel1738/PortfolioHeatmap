package com.PortfolioHeatmap.services;

/**
 * Defines the contract for stock data services that fetch stock price information.
 * Implementations of this interface (e.g., FMPStockDataService, AlphaVantageStockDataService)
 * provide methods to retrieve stock prices for individual symbols or in batches.
 * 
 * @author [Marvel Bana]
 */
import java.util.List;
import java.time.LocalDate;

import com.PortfolioHeatmap.models.StockPrice;
import com.PortfolioHeatmap.models.FMPSP500ConstituentResponse;
import com.PortfolioHeatmap.models.FMPStockListResponse;
import com.PortfolioHeatmap.models.HistoricalPrice;

public interface StockDataService {
    // Fetches the current stock price for a given symbol.
    StockPrice getStockPrice(String symbol);

    // Fetches stock prices for a list of symbols in a batch request.
    List<StockPrice> getBatchStockPrices(List<String> symbols);

    // Fetches historical price data for a given symbol within the specified date
    // range.
    // Returns a list of HistoricalPrice objects representing daily prices.
    List<HistoricalPrice> getHistoricalPrices(String symbol, LocalDate from, LocalDate to);

    // Fetches a list of stock symbols available for trading.
    List<FMPStockListResponse> getStockList();

    String getRawStockListResponse();

    String getRawSP500ConstituentsResponse();

    List<FMPSP500ConstituentResponse> getSP500Constituents();
}