package com.finsight.market.service;

import com.finsight.common.dto.StockQuoteDTO;
import com.finsight.common.event.StockPriceUpdatedEvent;
import com.finsight.market.dto.SimulateTickRequest;
import com.finsight.market.dto.SimulateTickResponse;
import com.finsight.market.entity.Stock;
import com.finsight.market.entity.StockQuote;
import com.finsight.market.exception.ResourceNotFoundException;
import com.finsight.market.repository.StockQuoteRepository;
import com.finsight.market.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class MarketSimulatorService {

    private static final Logger log = LoggerFactory.getLogger(MarketSimulatorService.class);
    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;
    private static final String TOPIC_MARKET_UPDATES = "market-price-updates";

    private final StockRepository stockRepository;
    private final StockQuoteRepository stockQuoteRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Random random = new Random();

    public MarketSimulatorService(StockRepository stockRepository,
                                  StockQuoteRepository stockQuoteRepository,
                                  @Autowired(required = false) KafkaTemplate<String, Object> kafkaTemplate) {
        this.stockRepository = stockRepository;
        this.stockQuoteRepository = stockQuoteRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Simulate price movements for a single stock or all active stocks.
     */
    @Transactional
    public SimulateTickResponse simulateTick(SimulateTickRequest request) {
        if (request != null && request.getSymbol() != null && !request.getSymbol().trim().isEmpty()) {
            return simulateSingleStockTick(request);
        } else {
            return simulateMarketWideTick();
        }
    }

    private SimulateTickResponse simulateSingleStockTick(SimulateTickRequest request) {
        String symbol = request.getSymbol().toUpperCase().trim();
        Stock stock = stockRepository.findBySymbolWithQuote(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found with symbol: " + symbol));

        StockQuote quote = stock.getQuote();
        if (quote == null) {
            throw new ResourceNotFoundException("No active quote found for stock symbol: " + symbol);
        }

        BigDecimal newPrice;
        if (request.getNewPrice() != null) {
            if (request.getNewPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Simulated price must be greater than zero");
            }
            newPrice = request.getNewPrice().setScale(SCALE, ROUNDING);
        } else if (request.getPercentChange() != null) {
            BigDecimal factor = BigDecimal.ONE.add(request.getPercentChange().divide(new BigDecimal("100"), 6, ROUNDING));
            newPrice = quote.getCurrentPrice().multiply(factor).setScale(SCALE, ROUNDING);
        } else {
            // Realistic percentage fluctuation between -3.0% and +3.0%
            double deltaPercent = (random.nextDouble() * 6.0) - 3.0;
            BigDecimal factor = BigDecimal.ONE.add(BigDecimal.valueOf(deltaPercent / 100.0));
            newPrice = quote.getCurrentPrice().multiply(factor).setScale(SCALE, ROUNDING);
        }

        updateQuotePrices(quote, newPrice);
        stockQuoteRepository.save(quote);

        StockQuoteDTO dto = mapToDTO(stock, quote);
        publishPriceUpdateEvent(dto);

        log.info("Simulated price tick for {}: new price = {}, change = {}%",
                symbol, quote.getCurrentPrice(), quote.getChangePct());

        return new SimulateTickResponse(Instant.now(), 1, List.of(dto));
    }

    private SimulateTickResponse simulateMarketWideTick() {
        List<Stock> activeStocks = stockRepository.findAllActiveWithQuotes();
        List<StockQuoteDTO> updatedDTOs = new ArrayList<>();

        for (Stock stock : activeStocks) {
            StockQuote quote = stock.getQuote();
            if (quote != null) {
                // Random fluctuation between -3.0% and +3.0%
                double deltaPercent = (random.nextDouble() * 6.0) - 3.0;
                BigDecimal factor = BigDecimal.ONE.add(BigDecimal.valueOf(deltaPercent / 100.0));
                BigDecimal newPrice = quote.getCurrentPrice().multiply(factor).setScale(SCALE, ROUNDING);

                updateQuotePrices(quote, newPrice);
                stockQuoteRepository.save(quote);

                StockQuoteDTO dto = mapToDTO(stock, quote);
                updatedDTOs.add(dto);
                publishPriceUpdateEvent(dto);
            }
        }

        log.info("Market-wide simulation executed for {} active securities", updatedDTOs.size());
        return new SimulateTickResponse(Instant.now(), updatedDTOs.size(), updatedDTOs);
    }

    private void updateQuotePrices(StockQuote quote, BigDecimal newPrice) {
        quote.setCurrentPrice(newPrice);
        if (quote.getHighPrice() == null || newPrice.compareTo(quote.getHighPrice()) > 0) {
            quote.setHighPrice(newPrice);
        }
        if (quote.getLowPrice() == null || newPrice.compareTo(quote.getLowPrice()) < 0) {
            quote.setLowPrice(newPrice);
        }
        long additionalVolume = 1000L + random.nextInt(10000);
        quote.setVolume(quote.getVolume() != null ? quote.getVolume() + additionalVolume : additionalVolume);
        quote.setMarketTime(Instant.now());
        quote.setUpdatedAt(Instant.now());
        quote.recalculateMetrics();
    }

    private StockQuoteDTO mapToDTO(Stock stock, StockQuote quote) {
        return StockQuoteDTO.builder()
                .symbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .sector(stock.getSector())
                .currency(stock.getCurrency())
                .currentPrice(quote.getCurrentPrice())
                .previousClose(quote.getPreviousClose())
                .change(quote.getChangeAmount())
                .changePercent(quote.getChangePct())
                .openPrice(quote.getOpenPrice())
                .highPrice(quote.getHighPrice())
                .lowPrice(quote.getLowPrice())
                .volume(quote.getVolume())
                .marketTime(quote.getMarketTime())
                .build();
    }

    private void publishPriceUpdateEvent(StockQuoteDTO dto) {
        if (kafkaTemplate == null) {
            return;
        }
        try {
            StockPriceUpdatedEvent.StockPricePayload payload = StockPriceUpdatedEvent.StockPricePayload.builder()
                    .symbol(dto.getSymbol())
                    .price(dto.getCurrentPrice())
                    .previousClose(dto.getPreviousClose())
                    .change(dto.getChange())
                    .changePercent(dto.getChangePercent())
                    .currency(dto.getCurrency())
                    .volume(dto.getVolume())
                    .marketTime(dto.getMarketTime())
                    .build();

            StockPriceUpdatedEvent event = StockPriceUpdatedEvent.of("market-data-service", payload);
            kafkaTemplate.send(TOPIC_MARKET_UPDATES, dto.getSymbol(), event);
            log.debug("Published price update event to Kafka for {}", dto.getSymbol());
        } catch (Exception e) {
            log.warn("Kafka event publishing skipped/failed for {}: {}", dto.getSymbol(), e.getMessage());
        }
    }
}

