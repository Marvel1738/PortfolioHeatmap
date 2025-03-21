package com.PortfolioHeatmap.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StockDataServiceFactory {
    private final AlphaVantageStockDataService alphaService;
    private final FMPStockDataService fmpService;

    @Value("${stock.data.provider:fmp}")
    private String provider;

    public StockDataServiceFactory(AlphaVantageStockDataService alphaService, FMPStockDataService fmpService) {
        this.alphaService = alphaService;
        this.fmpService = fmpService;
    }

    public StockDataService getService() {
        return "fmp".equalsIgnoreCase(provider) ? fmpService : alphaService;
    }
}