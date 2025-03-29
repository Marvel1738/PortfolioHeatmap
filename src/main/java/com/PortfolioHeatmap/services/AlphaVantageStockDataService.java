package com.PortfolioHeatmap.services;

/**
 * Implements the StockDataService interface to fetch stock price data from the Alpha Vantage API.
 * This service handles individual and batch stock price requests, deserializing API responses into
 * StockPrice objects for use in the application.
 * 
 * @author [Marvel Bana]
 */
import com.PortfolioHeatmap.models.AlphaVantageQuoteResponse;
import com.PortfolioHeatmap.models.FMPSP500ConstituentResponse;
import com.PortfolioHeatmap.models.FMPStockListResponse;
import com.PortfolioHeatmap.models.StockPrice;
import com.PortfolioHeatmap.models.AlphaVantageHistoricalPriceResponse;
import com.PortfolioHeatmap.models.HistoricalPrice;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
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
    // Initializes RestTemplate, API key, and ObjectMapper with JavaTimeModule for
    // LocalDate support.
    public AlphaVantageStockDataService(RestTemplateBuilder builder, @Value("${alphavantage.api.key}") String apiKey) {
        this.restTemplate = builder.build();
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
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

    @Override
    public List<HistoricalPrice> getHistoricalPrices(String symbol, LocalDate from, LocalDate to) {
        // Construct the API URL for historical prices with the symbol and API key.
        String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + symbol + "&apikey="
                + apiKey;
        log.info("Requesting historical prices URL: {}", url);

        // Make the HTTP request and get the raw JSON response.
        String rawResponse = restTemplate.getForObject(url, String.class);
        log.info("Raw API Response: {}", rawResponse);

        // Check if the response is null, and throw an exception if it is.
        if (rawResponse == null) {
            log.error("Received null response from Alpha Vantage for symbol: {}", symbol);
            throw new RuntimeException("No response from Alpha Vantage");
        }

        // Deserialize the raw JSON response into an AlphaVantageHistoricalPriceResponse
        // object.
        AlphaVantageHistoricalPriceResponse response;
        try {
            response = objectMapper.readValue(rawResponse, AlphaVantageHistoricalPriceResponse.class);
            log.info("Deserialized Historical Response: {}", response);
        } catch (Exception e) {
            log.error("Failed to deserialize historical response: {}", e.getMessage(), e);
            throw new RuntimeException("Error parsing Alpha Vantage historical response", e);
        }

        // Validate the response, returning an empty list if it is null or has no time
        // series data.
        if (response == null || response.getTimeSeries() == null || response.getTimeSeries().isEmpty()) {
            log.warn("No historical data found for symbol: {}. Raw response: {}", symbol, rawResponse);
            return List.of();
        }

        // Map each time series entry to a HistoricalPrice object, filter by date range,
        // and collect into a list.
        List<HistoricalPrice> historicalPrices = response.getTimeSeries().entrySet().stream()
                .filter(entry -> !entry.getKey().isBefore(from) && !entry.getKey().isAfter(to))
                .map(entry -> new HistoricalPrice(String .valueOf(entry.getKey()), parseDouble(entry.getValue().getClose(), "close")))
                .collect(Collectors.toList());
        log.info("Returning historical prices for {}: {}", symbol, historicalPrices);
        return historicalPrices;
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
    @Override
    public List<FMPStockListResponse> getStockList() {
        throw new UnsupportedOperationException("Alpha Vantage does not support fetching a stock list");
    }

    @Override
    public String getRawStockListResponse() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRawStockListResponse'");
    }

    @Override
    public String getRawSP500ConstituentsResponse() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRawSP500ConstituentsResponse'");
    }

    @Override
    public List<FMPSP500ConstituentResponse> getSP500Constituents() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSP500Constituents'");
    }
}