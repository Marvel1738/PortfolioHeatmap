package com.PortfolioHeatmap.models;

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
    @Id
    @Column(length = 10, nullable = false, unique = true)
    private String ticker;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String sector;

    @Column(nullable = false)
    private Double marketCap;

    @Column(nullable = false)
    private Double peRatio;

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL)
    private Set<PriceHistory> priceHistory;

    @OneToMany(mappedBy = "stock")
    private Set<Portfolio> portfolios;
}
