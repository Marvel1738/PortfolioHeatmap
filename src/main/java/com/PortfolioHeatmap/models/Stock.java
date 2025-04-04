package com.PortfolioHeatmap.models;

/**
 * Represents a stock entity in the PortfolioHeatmap application.
 * This class models a stock with its ticker symbol, company name, and associated price history,
 * and is mapped to the "stocks" table in the database.
 *
 * @author Marvel Bana
 */
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stocks")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "ticker")
public class Stock {
    @Id
    @Column(name = "ticker", length = 10)
    private String ticker;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PriceHistory> priceHistories = new ArrayList<>();

    // Getters and setters for accessing and modifying the fields
    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public List<PriceHistory> getPriceHistories() {
        return priceHistories;
    }

    public void setPriceHistories(List<PriceHistory> priceHistories) {
        this.priceHistories = priceHistories;
    }
}