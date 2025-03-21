package com.PortfolioHeatmap.controllers;

import com.PortfolioHeatmap.models.Stock;
import com.PortfolioHeatmap.services.StockService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/stocks")
public class StockController {

    @Autowired
    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public Page<Stock> getAllStocks(Pageable pageable) {
        return stockService.getAllStocks(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Stock> getStockById(@PathVariable String id) {
        Stock stock = stockService.getStockById(id);
        if (stock != null) {
            return ResponseEntity.ok(stock);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public Stock addStock(@RequestBody Stock stock) {
        return stockService.saveStock(stock);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable String id) {
        stockService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }
}
