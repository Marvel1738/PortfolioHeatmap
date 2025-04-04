package com.PortfolioHeatmap.models;

public class HistoricalPrice {
    private String date;
    private Double close;
    // No marketCap or peRatio fields in this response

    public HistoricalPrice(String date, Double close) {
        this.date = date;
        this.close = close;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }

    // Likely missing these methods since the API doesn't provide these fields
    public Long getMarketCap() {
        return null; // Not provided by /historical-price-full
    }

    public Double getPeRatio() {
        return null; // Not provided by /historical-price-full
    }
}