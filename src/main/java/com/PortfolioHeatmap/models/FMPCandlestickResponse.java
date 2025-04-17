package com.PortfolioHeatmap.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents the response structure for a historical price request with full
 * OHLC data
 * from the Financial Modeling Prep (FMP) API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FMPCandlestickResponse {
    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("historical")
    private List<CandlestickEntry> historical;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public List<CandlestickEntry> getHistorical() {
        return historical;
    }

    public void setHistorical(List<CandlestickEntry> historical) {
        this.historical = historical;
    }

    @Override
    public String toString() {
        return "FMPCandlestickResponse{" +
                "symbol='" + symbol + '\'' +
                ", historical=" + historical +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandlestickEntry {
        @JsonProperty("date")
        private String date;

        @JsonProperty("open")
        private Double open;

        @JsonProperty("high")
        private Double high;

        @JsonProperty("low")
        private Double low;

        @JsonProperty("close")
        private Double close;

        @JsonProperty("volume")
        private Long volume;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Double getOpen() {
            return open;
        }

        public void setOpen(Double open) {
            this.open = open;
        }

        public Double getHigh() {
            return high;
        }

        public void setHigh(Double high) {
            this.high = high;
        }

        public Double getLow() {
            return low;
        }

        public void setLow(Double low) {
            this.low = low;
        }

        public Double getClose() {
            return close;
        }

        public void setClose(Double close) {
            this.close = close;
        }

        public Long getVolume() {
            return volume;
        }

        public void setVolume(Long volume) {
            this.volume = volume;
        }

        @Override
        public String toString() {
            return "CandlestickEntry{" +
                    "date='" + date + '\'' +
                    ", open=" + open +
                    ", high=" + high +
                    ", low=" + low +
                    ", close=" + close +
                    ", volume=" + volume +
                    '}';
        }
    }
}