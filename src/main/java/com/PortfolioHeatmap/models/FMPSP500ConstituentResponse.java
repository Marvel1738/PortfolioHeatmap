package com.PortfolioHeatmap.models;

/**
 * Represents a response object for S&P 500 constituent data from the Financial
 * Modeling Prep (FMP) API.
 * This class encapsulates details about a stock within the S&P 500 index,
 * including its symbol, name,
 * sector, sub-sector, headquarters, and market capitalization.
 *
 * @author Marvel Bana
 */
public class FMPSP500ConstituentResponse {
    private String symbol;
    private String name;
    private String sector;
    private String subSector;
    private String headQuarter;
    private Long marketCap;

    // Getters and setters for accessing and modifying the fields
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

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getSubSector() {
        return subSector;
    }

    public void setSubSector(String subSector) {
        this.subSector = subSector;
    }

    public String getHeadQuarter() {
        return headQuarter;
    }

    public void setHeadQuarter(String headQuarter) {
        this.headQuarter = headQuarter;
    }

    public Long getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(Long marketCap) {
        this.marketCap = marketCap;
    }

    // Overrides toString to provide a concise string representation of the object
    @Override
    public String toString() {
        return "FMPSP500ConstituentResponse[symbol=" + symbol + ", name=" + name + ", marketCap=" + marketCap + "]";
    }
}