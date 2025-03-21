package com.PortfolioHeatmap.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.PortfolioHeatmap.models.Stock;
import com.PortfolioHeatmap.repositories.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockService stockService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetStockByTicker() {
        // Arrange
        Stock stock = new Stock();
        stock.setTicker("AAPL");
        stock.setCompanyName("Apple Inc.");
        stock.setSector("Technology");
        stock.setMarketCap(2.5E12);
        stock.setPeRatio(30.5);

        when(stockRepository.findById("AAPL")).thenReturn(Optional.of(stock));

        // Act
        Stock foundStock = stockService.getStockById("AAPL");

        // Assert
        assertNotNull(foundStock);
        assertEquals("AAPL", foundStock.getTicker());
        assertEquals("Apple Inc.", foundStock.getCompanyName());
        assertEquals("Technology", foundStock.getSector());
        assertEquals(2.5E12, foundStock.getMarketCap());
        assertEquals(30.5, foundStock.getPeRatio());
    }

    @Test
    void testSaveStock() {
        // Arrange
        Stock stock = new Stock();
        stock.setTicker("GOOGL");
        stock.setCompanyName("Google Inc.");
        stock.setSector("Technology");
        stock.setMarketCap(1.8E12);
        stock.setPeRatio(28.0);

        when(stockRepository.save(stock)).thenReturn(stock);

        // Act
        Stock savedStock = stockService.saveStock(stock);

        // Assert
        assertNotNull(savedStock);
        assertEquals("GOOGL", savedStock.getTicker());
        assertEquals("Google Inc.", savedStock.getCompanyName());
        assertEquals("Technology", savedStock.getSector());
        assertEquals(1.8E12, savedStock.getMarketCap());
        assertEquals(28.0, savedStock.getPeRatio());

        verify(stockRepository, times(1)).save(stock);
    }
}
