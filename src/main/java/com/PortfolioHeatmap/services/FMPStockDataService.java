package com.PortfolioHeatmap.services;

/**
 * Implements the StockDataService interface to fetch stock price data from the Financial Modeling Prep (FMP) API.
 * This service handles individual and batch stock price requests, deserializing API responses into StockPrice
 * objects for use in the application.
 * 
 * @author [Your Name]
 */
import com.PortfolioHeatmap.models.FMPQuoteResponse;
import com.PortfolioHeatmap.models.StockPrice;
import com.PortfolioHeatmap.models.FMPHistoricalPriceResponse;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FMPStockDataService implements StockDataService {
    // Logger for tracking requests, responses, and errors in this service.
    private static final Logger log = LoggerFactory.getLogger(FMPStockDataService.class);
    // RestTemplate for making HTTP requests to the FMP API.
    private final RestTemplate restTemplate;
    // API key for authenticating requests to the FMP API, loaded from application
    // properties.
    private final String apiKey;
    // ObjectMapper for deserializing JSON responses from the API.
    private final ObjectMapper objectMapper;

    // Constructor for dependency injection of RestTemplateBuilder and API key.
    // Initializes RestTemplate, API key, and ObjectMapper with JavaTimeModule for
    // LocalDate support.
    public FMPStockDataService(RestTemplateBuilder builder, @Value("${fmp.api.key}") String apiKey) {
        this.restTemplate = builder.build();
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        log.info("Initialized with FMP API Key: {}", apiKey);
    }

    @Override
    public StockPrice getStockPrice(String symbol) {
        String url = "https://financialmodelingprep.com/api/v3/quote/" + symbol + "?apikey=" + apiKey;
        log.info("Requesting URL: {}", url);

        String rawResponse;
        try {
            rawResponse = restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            log.error("Failed to fetch data from FMP for symbol {}: {}", symbol, e.getMessage(), e);
            throw new RuntimeException("Error fetching data from FMP for " + symbol, e);
        }
        log.info("Raw API Response: {}", rawResponse);

        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            log.error("Empty response from FMP for symbol: {}", symbol);
            throw new RuntimeException("Empty response from FMP for " + symbol);
        }

        FMPQuoteResponse[] response;
        try {
            response = objectMapper.readValue(rawResponse, FMPQuoteResponse[].class);
            log.info("Deserialized Response: {}", Arrays.toString(response));
        } catch (Exception e) {
            log.error("Failed to deserialize response for symbol {}: {}. Raw response: {}", symbol, e.getMessage(),
                    rawResponse, e);
            throw new RuntimeException("Error parsing FMP response for " + symbol, e);
        }

        if (response == null || response.length == 0) {
            log.error("Invalid or empty response for symbol: {}. Raw response: {}", symbol, rawResponse);
            throw new RuntimeException("Failed to fetch stock data for " + symbol);
        }

        FMPQuoteResponse quote = response[0];
        StockPrice stockPrice = new StockPrice();
        stockPrice.setSymbol(quote.getSymbol());
        stockPrice.setPrice(quote.getPrice());
        stockPrice.setOpen(quote.getOpen());
        stockPrice.setHigh(quote.getHigh());
        stockPrice.setLow(quote.getLow());
        stockPrice.setPreviousClose(quote.getPreviousClose());
        log.info("Parsed Quote: symbol={}, price={}", quote.getSymbol(), quote.getPrice());
        return stockPrice;
    }

    @Override
    public List<StockPrice> getBatchStockPrices(List<String> symbols) {
        log.info("Fetching batch prices for symbols: {}", symbols);
        if (symbols.size() > 100) {
            log.warn("Symbol count exceeds 100: {}", symbols.size());
            throw new IllegalArgumentException("Batch request limited to 100 symbols");
        }

        String symbolList = String.join(",", symbols);
        String url = "https://financialmodelingprep.com/api/v3/quote/" + symbolList + "?apikey=" + apiKey;
        log.info("Requesting URL: {}", url);

        String rawResponse;
        try {
            rawResponse = restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            log.error("Failed to fetch batch data from FMP for symbols {}: {}", symbolList, e.getMessage(), e);
            throw new RuntimeException("Error fetching batch data from FMP for " + symbolList, e);
        }
        log.info("Raw API Response: {}", rawResponse);

        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            log.error("Empty response from FMP for symbols: {}", symbolList);
            throw new RuntimeException("Empty response from FMP for " + symbolList);
        }

        FMPQuoteResponse[] response;
        try {
            response = objectMapper.readValue(rawResponse, FMPQuoteResponse[].class);
            log.info("Deserialized Batch Response: {}", Arrays.toString(response));
        } catch (Exception e) {
            log.error("Failed to deserialize batch response for symbols {}: {}. Raw response: {}", symbolList,
                    e.getMessage(), rawResponse, e);
            throw new RuntimeException("Error parsing FMP batch response for " + symbolList, e);
        }

        if (response == null || response.length == 0) {
            log.warn("No valid stock quotes found for symbols: {}. Raw response: {}", symbolList, rawResponse);
            return List.of();
        }

        List<StockPrice> stockPrices = Arrays.stream(response)
                .map(quote -> {
                    StockPrice stockPrice = new StockPrice();
                    stockPrice.setSymbol(quote.getSymbol());
                    stockPrice.setPrice(quote.getPrice());
                    stockPrice.setOpen(quote.getOpen());
                    stockPrice.setHigh(quote.getHigh());
                    stockPrice.setLow(quote.getLow());
                    stockPrice.setPreviousClose(quote.getPreviousClose());
                    log.info("Mapped StockPrice: symbol={}, price={}", quote.getSymbol(), quote.getPrice());
                    return stockPrice;
                })
                .collect(Collectors.toList());
        log.info("Returning batch prices: {}", stockPrices);
        return stockPrices;
    }

    @Override
    public List<HistoricalPrice> getHistoricalPrices(String symbol, LocalDate from, LocalDate to) {
        // Construct the API URL for historical prices with the symbol, date range, and
        // API key.
        String url = String.format(
                "https://financialmodelingprep.com/api/v3/historical-price-full/%s?from=%s&to=%s&apikey=%s",
                symbol, from, to, apiKey);
        log.info("Requesting historical prices URL: {}", url);

        // Make the HTTP request and get the raw JSON response.
        String rawResponse;
        try {
            rawResponse = restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            log.error("Failed to fetch historical data from FMP for symbol {}: {}", symbol, e.getMessage(), e);
            throw new RuntimeException("Error fetching historical data from FMP for " + symbol, e);
        }
        log.info("Raw API Response: {}", rawResponse);

        // Check if the response is null or empty, and throw an exception if it is.
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            log.error("Empty response from FMP for symbol: {}", symbol);
            throw new RuntimeException("Empty response from FMP for " + symbol);
        }

        // Deserialize the raw JSON response into an FMPHistoricalPriceResponse object.
        FMPHistoricalPriceResponse response;
        try {
            response = objectMapper.readValue(rawResponse, FMPHistoricalPriceResponse.class);
            log.info("Deserialized Historical Response: {}", response);
        } catch (Exception e) {
            log.error("Failed to deserialize historical response for symbol {}: {}. Raw response: {}", symbol,
                    e.getMessage(),
                    rawResponse, e);
            throw new RuntimeException("Error parsing FMP historical response for " + symbol, e);
        }

        // Validate the response, returning an empty list if it is null or has no
        // historical data.
        if (response == null || response.getHistorical() == null || response.getHistorical().isEmpty()) {
            log.warn("No historical data found for symbol: {}. Raw response: {}", symbol, rawResponse);
            return List.of();
        }

        // Map each historical entry to a HistoricalPrice object and collect into a
        // list.
        List<HistoricalPrice> historicalPrices = response.getHistorical().stream()
                .map(entry -> new HistoricalPrice(entry.getDate(), entry.getClose()))
                .collect(Collectors.toList());
        log.info("Returning historical prices for {}: {}", symbol, historicalPrices);
        return historicalPrices;
    }
}