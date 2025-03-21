package com.PortfolioHeatmap.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "portfolio")
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "stock_ticker", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private int sharesOwned;

    @Column(nullable = false)
    private BigDecimal costBasis;
}
