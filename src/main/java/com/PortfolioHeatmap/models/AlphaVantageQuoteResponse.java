package com.PortfolioHeatmap.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AlphaVantageQuoteResponse {
    @JsonProperty("Global Quote")
    private GlobalQuote globalQuote;

    public GlobalQuote getGlobalQuote() {
        return globalQuote;
    }

    public void setGlobalQuote(GlobalQuote globalQuote) {
        this.globalQuote = globalQuote;
    }

    @Override
    public String toString() {
        return "AlphaVantageQuoteResponse[globalQuote=" + globalQuote + "]";
    }

    @JsonIgnoreProperties(ignoreUnknown = true) // Ignore extra fields
    public static class GlobalQuote {
        @JsonProperty("01. symbol")
        private String symbol;
        @JsonProperty("05. price")
        private String price;
        @JsonProperty("02. open")
        private String open;
        @JsonProperty("03. high")
        private String high;
        @JsonProperty("04. low")
        private String low;
        @JsonProperty("08. previous close")
        private String previousClose;

        public String getSymbol() {
            return symbol;
        }

        public String getPrice() {
            return price;
        }

        public String getOpen() {
            return open;
        }

        public String getHigh() {
            return high;
        }

        public String getLow() {
            return low;
        }

        public String getPreviousClose() {
            return previousClose;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public void setOpen(String open) {
            this.open = open;
        }

        public void setHigh(String high) {
            this.high = high;
        }

        public void setLow(String low) {
            this.low = low;
        }

        public void setPreviousClose(String previousClose) {
            this.previousClose = previousClose;
        }

        @Override
        public String toString() {
            return "GlobalQuote[symbol=" + symbol + ", price=" + price + ", ...]";
        }
    }
}