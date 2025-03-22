package com.PortfolioHeatmap.services;

/**
 * Implements the StockDataService interface to fetch stock price data from the Alpha Vantage API.
 * This service handles individual and batch stock price requests, deserializing API responses into
 * StockPrice objects for use in the application.
 * 
 * @author [Marvel Bana]
 */
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
    // Logger for tracking requests, responses, and errors in this service.
    private static final Logger log = LoggerFactory.getLogger(AlphaVantageStockDataService.class);
    // RestTemplate for making HTTP requests to the Alpha Vantage API.
    private final RestTemplate restTemplate;
    // API key for authenticating requests to the Alpha Vantage API, loaded from
    // application properties.
    private final String apiKey;
    // ObjectMapper for deserializing JSON responses from the API.
    private final ObjectMapper objectMapper;

    // Constructor for dependency injection of RestTemplateBuilder and API key.
    // Initializes RestTemplate, API key, and ObjectMapper, and logs the API key
    // initialization.
    public AlphaVantageStockDataService(RestTemplateBuilder builder, @Value("${alphavantage.api.key}") String apiKey) {
        this.restTemplate = builder.build();
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
        log.info("Initialized with API Key: {}", apiKey);
    }

    // Fetches the current stock price for a given symbol from the Alpha Vantage
    // API.
    // Constructs the API URL, makes the request, deserializes the response, and
    // maps it to a StockPrice object.
    @Override
    public StockPrice getStockPrice(String symbol) {
        // Construct the API URL with the symbol and API key.
        String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + apiKey;
        log.info("Requesting URL: {}", url);

        // Make the HTTP request and get the raw JSON response.
        String rawResponse = restTemplate.getForObject(url, String.class);
        log.info("Raw API Response: {}", rawResponse);

        // Check if the response is null, and throw an exception if it is.
        if (rawResponse == null) {
            log.error("Received null response from Alpha Vantage for symbol: {}", symbol);
            throw new RuntimeException("No response from Alpha Vantage");
        }

        // Deserialize the raw JSON response into an AlphaVantageQuoteResponse object.
        AlphaVantageQuoteResponse response;
        try {
            response = objectMapper.readValue(rawResponse, AlphaVantageQuoteResponse.class);
            log.info("Deserialized Response: {}", response);
        } catch (Exception e) {
            log.error("Failed to deserialize response: {}", e.getMessage(), e);
            throw new RuntimeException("Error parsing Alpha Vantage response", e);
        }

        // Validate the response and its global quote, throwing an exception if invalid.
        if (response == null || response.getGlobalQuote() == null) {
            log.error("Invalid or empty response for symbol: {}. Raw response: {}", symbol, rawResponse);
            throw new RuntimeException("Failed to fetch stock data for " + symbol);
        }

        // Extract the global quote from the response.
        AlphaVantageQuoteResponse.GlobalQuote quote = response.getGlobalQuote();
        log.info("Parsed Quote: symbol={}, price={}", quote.getSymbol(), quote.getPrice());

        // Map the global quote data to a StockPrice object.
        StockPrice stockPrice = new StockPrice();
        stockPrice.setSymbol(quote.getSymbol());
        stockPrice.setPrice(parseDouble(quote.getPrice(), "price"));
        stockPrice.setOpen(parseDouble(quote.getOpen(), "open"));
        stockPrice.setHigh(parseDouble(quote.getHigh(), "high"));
        stockPrice.setLow(parseDouble(quote.getLow(), "low"));
        stockPrice.setPreviousClose(parseDouble(quote.getPreviousClose(), "previous close"));
        return stockPrice;
    }

    // Fetches stock prices for a list of symbols by calling getStockPrice for each
    // symbol.
    // Limits the batch size to 100 symbols and skips any symbols that fail to
    // fetch.
    @Override
    public List<StockPrice> getBatchStockPrices(List<String> symbols) {
        log.info("Fetching batch prices for symbols: {}", symbols);
        // Check if the number of symbols exceeds the API limit of 100.
        if (symbols.size() > 100) {
            log.warn("Symbol count exceeds 100: {}", symbols.size());
            throw new IllegalArgumentException("Batch request limited to 100 symbols");
        }

        // Fetch prices for each symbol, handling failures gracefully by skipping failed
        // symbols.
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

        // Log the result of the batch request.
        if (stockPrices.isEmpty()) {
            log.warn("No valid stock prices fetched for symbols: {}", symbols);
        } else {
            log.info("Returning batch prices: {}", stockPrices);
        }
        return stockPrices;
    }

    // Helper method to parse a string value into a double.
    // Returns 0.0 if the value is null or empty, and throws an exception if parsing
    // fails.
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