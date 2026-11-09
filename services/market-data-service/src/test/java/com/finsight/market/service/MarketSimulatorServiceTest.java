package com.finsight.market.service;

import com.finsight.market.dto.SimulateTickRequest;
import com.finsight.market.dto.SimulateTickResponse;
import com.finsight.market.entity.Stock;
import com.finsight.market.entity.StockQuote;
import com.finsight.market.exception.ResourceNotFoundException;
import com.finsight.market.repository.StockQuoteRepository;
import com.finsight.market.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarketSimulatorService Unit Tests")
class MarketSimulatorServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockQuoteRepository stockQuoteRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private MarketSimulatorService marketSimulatorService;

    private Stock testStock;
    private StockQuote testQuote;

    @BeforeEach
    void setUp() {
        testStock = new Stock("AAPL", "Apple Inc.", "Technology", "Consumer Electronics", "USD");
        testQuote = new StockQuote(
                testStock,
                new BigDecimal("180.0000"),
                new BigDecimal("178.0000"),
                new BigDecimal("182.0000"),
                new BigDecimal("177.0000"),
                new BigDecimal("178.0000"),
                50000000L,
                Instant.now()
        );
        testStock.setQuote(testQuote);
    }

    @Test
    @DisplayName("simulateTick with specific newPrice should update quote price and high/low")
    void shouldUpdateQuoteWithSpecificPrice() {
        when(stockRepository.findBySymbolWithQuote("AAPL")).thenReturn(Optional.of(testStock));

        SimulateTickRequest request = new SimulateTickRequest("AAPL", new BigDecimal("190.0000"), null);
        SimulateTickResponse response = marketSimulatorService.simulateTick(request);

        assertNotNull(response);
        assertEquals(1, response.getUpdatedCount());
        assertEquals("AAPL", response.getUpdatedQuotes().get(0).getSymbol());
        assertEquals(new BigDecimal("190.0000"), response.getUpdatedQuotes().get(0).getCurrentPrice());
        assertEquals(new BigDecimal("190.0000"), testQuote.getHighPrice(), "High price should expand to 190.00");
        verify(stockQuoteRepository).save(testQuote);
    }

    @Test
    @DisplayName("simulateTick with non-positive price should throw IllegalArgumentException")
    void shouldThrowOnInvalidPrice() {
        when(stockRepository.findBySymbolWithQuote("AAPL")).thenReturn(Optional.of(testStock));

        SimulateTickRequest request = new SimulateTickRequest("AAPL", new BigDecimal("-10.0000"), null);
        assertThrows(IllegalArgumentException.class, () -> {
            marketSimulatorService.simulateTick(request);
        });
    }

    @Test
    @DisplayName("simulateTick with percentChange should compute new price accurately")
    void shouldUpdateWithPercentChange() {
        when(stockRepository.findBySymbolWithQuote("AAPL")).thenReturn(Optional.of(testStock));

        // +10% change on 180.0000 -> 198.0000
        SimulateTickRequest request = new SimulateTickRequest("AAPL", null, new BigDecimal("10.0000"));
        SimulateTickResponse response = marketSimulatorService.simulateTick(request);

        assertNotNull(response);
        assertEquals(1, response.getUpdatedCount());
        assertEquals(new BigDecimal("198.0000"), response.getUpdatedQuotes().get(0).getCurrentPrice());
    }

    @Test
    @DisplayName("simulateTick for unknown stock should throw ResourceNotFoundException")
    void shouldThrowForUnknownStock() {
        when(stockRepository.findBySymbolWithQuote("UNKNOWN")).thenReturn(Optional.empty());

        SimulateTickRequest request = new SimulateTickRequest("UNKNOWN", new BigDecimal("100.0000"), null);
        assertThrows(ResourceNotFoundException.class, () -> {
            marketSimulatorService.simulateTick(request);
        });
    }

    @Test
    @DisplayName("simulateTick with empty symbol should execute market-wide tick across all active stocks")
    void shouldExecuteMarketWideTick() {
        when(stockRepository.findAllActiveWithQuotes()).thenReturn(List.of(testStock));

        SimulateTickRequest request = new SimulateTickRequest();
        SimulateTickResponse response = marketSimulatorService.simulateTick(request);

        assertNotNull(response);
        assertEquals(1, response.getUpdatedCount());
        verify(stockQuoteRepository).save(any(StockQuote.class));
    }
}

