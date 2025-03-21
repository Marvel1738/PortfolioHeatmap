package com.PortfolioHeatmap.services;

import java.util.List;

import com.PortfolioHeatmap.models.StockPrice;

public interface StockDataService {
    StockPrice getStockPrice(String symbol);

    List<StockPrice> getBatchStockPrices(List<String> symbols);
}