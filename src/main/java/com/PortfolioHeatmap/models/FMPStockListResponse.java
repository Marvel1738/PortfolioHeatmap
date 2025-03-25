package com.PortfolioHeatmap.models;

/**
 * Represents a stock entry from the FMP API's /stock/list endpoint response.
 * 
 * @author [Your Name]
 */
public class FMPStockListResponse {
    private String symbol;
    private String name;
    private Double price;
    private String exchange;
    private String exchangeShortName;
    private String type;
    private Long marketCap;

    // Getters and setters
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getExchangeShortName() {
        return exchangeShortName;
    }

    public void setExchangeShortName(String exchangeShortName) {
        this.exchangeShortName = exchangeShortName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(Long marketCap) {
        this.marketCap = marketCap;
    }

    @Override
    public String toString() {
        return "FMPStockListResponse[symbol=" + symbol + ", name=" + name + ", marketCap=" + marketCap + "]";
    }
}