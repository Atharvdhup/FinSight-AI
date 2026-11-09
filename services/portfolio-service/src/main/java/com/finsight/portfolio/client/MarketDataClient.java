package com.finsight.portfolio.client;

import com.finsight.common.dto.StockQuoteDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/**
 * Modern REST client for inter-service communication with market-data-service.
 * Uses Spring 6 RestClient with graceful degradation/fallback when market data is unreachable.
 */
@Component
public class MarketDataClient {

    private static final Logger log = LoggerFactory.getLogger(MarketDataClient.class);

    private final RestClient restClient;

    public MarketDataClient(@Value("${market-data.service.url:http://localhost:8082}") String marketDataUrl) {
        log.info("Initializing MarketDataClient with base URL: {}", marketDataUrl);
        this.restClient = RestClient.builder()
                .baseUrl(marketDataUrl)
                .build();
    }

    /**
     * Fetch the latest live quote for a given stock symbol.
     * Returns Optional.empty() if market-data-service is offline or ticker is not found.
     */
    public Optional<StockQuoteDTO> getLiveQuote(String symbol) {
        try {
            StockQuoteDTO quote = restClient.get()
                    .uri("/api/v1/market/quotes/{symbol}", symbol)
                    .retrieve()
                    .body(StockQuoteDTO.class);
            return Optional.ofNullable(quote);
        } catch (Exception e) {
            log.warn("Failed to fetch live quote for {} from market-data-service: {}. Falling back to cached price.",
                    symbol, e.getMessage());
            return Optional.empty();
        }
    }
}