package com.PortfolioHeatmap.services;

/**
 * A factory class that provides the appropriate StockDataService implementation based on configuration.
 * This class allows the application to switch between different stock data providers (e.g., FMP or Alpha Vantage)
 * at runtime, using a provider property.
 * 
 * @author [Marvel Bana]
 */
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StockDataServiceFactory {
    // The Alpha Vantage stock data service implementation.
    private final AlphaVantageStockDataService alphaService;
    // The FMP stock data service implementation.
    private final FMPStockDataService fmpService;

    // The provider to use, loaded from application properties (defaults to "fmp").
    @Value("${stock.data.provider:fmp}")
    private String provider;

    // Constructor for dependency injection of the Alpha Vantage and FMP stock data
    // services.
    public StockDataServiceFactory(AlphaVantageStockDataService alphaService, FMPStockDataService fmpService) {
        this.alphaService = alphaService;
        this.fmpService = fmpService;
    }

    // Returns the appropriate StockDataService implementation based on the provider
    // property.
    // Returns FMPStockDataService if the provider is "fmp", otherwise returns
    // AlphaVantageStockDataService.
    public StockDataService getService() {
        return "fmp".equalsIgnoreCase(provider) ? fmpService : alphaService;
    }
}