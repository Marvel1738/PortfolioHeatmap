package com.PortfolioHeatmap.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AlphaVantageBatchResponse {
    @JsonProperty("Stock Quotes")
    private List<StockQuote> stockQuotes;

    public List<StockQuote> getStockQuotes() {
        return stockQuotes;
    }

    public void setStockQuotes(List<StockQuote> stockQuotes) {
        this.stockQuotes = stockQuotes;
    }

    @Override
    public String toString() {
        return "AlphaVantageBatchResponse[stockQuotes=" + stockQuotes + "]";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StockQuote {
        @JsonProperty("1. symbol")
        private String symbol;
        @JsonProperty("2. price")
        private String price;

        public String getSymbol() {
            return symbol;
        }

        public String getPrice() {
            return price;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        @Override
        public String toString() {
            return "StockQuote[symbol=" + symbol + ", price=" + price + "]";
        }
    }
}