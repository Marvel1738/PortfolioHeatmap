package com.PortfolioHeatmap.services;

import com.PortfolioHeatmap.models.FMPQuoteResponse;
import com.PortfolioHeatmap.models.StockPrice;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FMPStockDataService implements StockDataService {
    private static final Logger log = LoggerFactory.getLogger(FMPStockDataService.class);
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public FMPStockDataService(RestTemplateBuilder builder, @Value("${fmp.api.key}") String apiKey) {
        this.restTemplate = builder.build();
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
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
}