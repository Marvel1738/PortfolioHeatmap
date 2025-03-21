package com.PortfolioHeatmap.services;

import com.PortfolioHeatmap.models.AlphaVantageQuoteResponse;
import com.PortfolioHeatmap.models.StockPrice;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlphaVantageStockDataService implements StockDataService {
    private static final Logger log = LoggerFactory.getLogger(AlphaVantageStockDataService.class);
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public AlphaVantageStockDataService(RestTemplateBuilder builder, @Value("${alphavantage.api.key}") String apiKey) {
        this.restTemplate = builder.build();
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
        log.info("Initialized with API Key: {}", apiKey);
    }

    @Override
    public StockPrice getStockPrice(String symbol) {
        String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + apiKey;
        log.info("Requesting URL: {}", url);

        String rawResponse = restTemplate.getForObject(url, String.class);
        log.info("Raw API Response: {}", rawResponse);

        if (rawResponse == null) {
            log.error("Received null response from Alpha Vantage for symbol: {}", symbol);
            throw new RuntimeException("No response from Alpha Vantage");
        }

        AlphaVantageQuoteResponse response;
        try {
            response = objectMapper.readValue(rawResponse, AlphaVantageQuoteResponse.class);
            log.info("Deserialized Response: {}", response);
        } catch (Exception e) {
            log.error("Failed to deserialize response: {}", e.getMessage(), e);
            throw new RuntimeException("Error parsing Alpha Vantage response", e);
        }

        if (response == null || response.getGlobalQuote() == null) {
            log.error("Invalid or empty response for symbol: {}. Raw response: {}", symbol, rawResponse);
            throw new RuntimeException("Failed to fetch stock data for " + symbol);
        }

        AlphaVantageQuoteResponse.GlobalQuote quote = response.getGlobalQuote();
        log.info("Parsed Quote: symbol={}, price={}", quote.getSymbol(), quote.getPrice());

        StockPrice stockPrice = new StockPrice();
        stockPrice.setSymbol(quote.getSymbol());
        stockPrice.setPrice(parseDouble(quote.getPrice(), "price"));
        stockPrice.setOpen(parseDouble(quote.getOpen(), "open"));
        stockPrice.setHigh(parseDouble(quote.getHigh(), "high"));
        stockPrice.setLow(parseDouble(quote.getLow(), "low"));
        stockPrice.setPreviousClose(parseDouble(quote.getPreviousClose(), "previous close"));
        return stockPrice;
    }

    @Override
    public List<StockPrice> getBatchStockPrices(List<String> symbols) {
        log.info("Fetching batch prices for symbols: {}", symbols);
        if (symbols.size() > 100) {
            log.warn("Symbol count exceeds 100: {}", symbols.size());
            throw new IllegalArgumentException("Batch request limited to 100 symbols");
        }

        List<StockPrice> stockPrices = symbols.stream()
                .map(symbol -> {
                    try {
                        StockPrice price = getStockPrice(symbol);
                        log.info("Fetched price for {}: {}", symbol, price);
                        return price;
                    } catch (Exception e) {
                        log.warn("Failed to fetch price for {}: {}", symbol, e.getMessage());
                        return null; // Skip failed symbols
                    }
                })
                .filter(stockPrice -> stockPrice != null) // Remove nulls
                .collect(Collectors.toList());

        if (stockPrices.isEmpty()) {
            log.warn("No valid stock prices fetched for symbols: {}", symbols);
        } else {
            log.info("Returning batch prices: {}", stockPrices);
        }
        return stockPrices;
    }

    private double parseDouble(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            log.warn("Field '{}' is null or empty, defaulting to 0.0", fieldName);
            return 0.0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.error("Failed to parse field '{}': {}", fieldName, value, e);
            throw e;
        }
    }
}