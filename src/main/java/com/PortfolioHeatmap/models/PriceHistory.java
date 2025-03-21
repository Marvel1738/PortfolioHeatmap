package com.PortfolioHeatmap.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "price_history")
public class PriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stock_ticker", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Double closingPrice;
}
