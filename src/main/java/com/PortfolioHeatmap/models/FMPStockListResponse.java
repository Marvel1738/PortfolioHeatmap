package com.PortfolioHeatmap.models;

/**
 * Represents a single entry in the stock list response from the Financial Modeling Prep (FMP) API.
 * Used to deserialize the JSON response from the /stock/list endpoint.
 * 
 * @author [Your Name]
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FMPStockListResponse {
    @JsonProperty("symbol")
    private String symbol;
    @JsonProperty("name")
    private String name;
    @JsonProperty("exchange")
    private String exchange;
    @JsonProperty("type")
    private String type;

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

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "FMPStockListResponse[symbol=" + symbol + ", name=" + name + ", exchange=" + exchange + ", type=" + type
                + "]";
    }
}