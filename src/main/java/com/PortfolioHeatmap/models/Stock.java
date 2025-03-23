package com.PortfolioHeatmap.models;

/**
 * Represents a stock in the application, storing details such as ticker, company name, sector,
 * market cap, and P/E ratio. This class is a JPA entity mapped to the "stocks" table in the database
 * and maintains relationships with PriceHistory and Portfolio entities.
 * 
 * @author [Marvel Bana]
 */
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "ticker") // Generate based on ticker
@Table(name = "stocks")
public class Stock {
    // Primary key for the stock, representing the stock ticker.
    // Must be unique, not null, and limited to 10 characters.
    @Id
    @Column(length = 10, nullable = false, unique = true)
    private String ticker;

    // The name of the company associated with this stock.
    // Cannot be null, as this is a required field.
    @Column(nullable = false)
    private String companyName;

    // Set of historical price entries for this stock.
    // One-to-many relationship with PriceHistory, with cascading operations.
    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL)
    private Set<PriceHistory> priceHistory;

    // Set of portfolio entries that include this stock.
    // One-to-many relationship with Portfolio, managed by the Portfolio entity.
    @OneToMany(mappedBy = "stock")
    private Set<Portfolio> portfolios;
}