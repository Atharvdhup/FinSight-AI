package com.finsight.market.service;

import com.finsight.common.dto.StockQuoteDTO;
import com.finsight.market.dto.HistoricalCandleDTO;
import com.finsight.market.dto.StockDetailResponse;
import com.finsight.market.entity.HistoricalPrice;
import com.finsight.market.entity.Stock;
import com.finsight.market.entity.StockQuote;
import com.finsight.market.exception.ResourceNotFoundException;
import com.finsight.market.repository.HistoricalPriceRepository;
import com.finsight.market.repository.StockQuoteRepository;
import com.finsight.market.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarketDataService Unit Tests")
class MarketDataServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockQuoteRepository stockQuoteRepository;

    @Mock
    private HistoricalPriceRepository historicalPriceRepository;

    @InjectMocks
    private MarketDataService marketDataService;

    private Stock testStock;
    private StockQuote testQuote;

    @BeforeEach
    void setUp() {
        testStock = new Stock("AAPL", "Apple Inc.", "Technology", "Consumer Electronics", "USD");
        testQuote = new StockQuote(
                testStock,
                new BigDecimal("182.5000"),
                new BigDecimal("180.0000"),
                new BigDecimal("185.0000"),
                new BigDecimal("179.5000"),
                new BigDecimal("180.0000"),
                50000000L,
                Instant.now()
        );
        testStock.setQuote(testQuote);
    }

    @Test
    @DisplayName("getAllActiveStocks should return list of mapped stock profiles")
    void shouldReturnAllActiveStocks() {
        when(stockRepository.findAllActiveWithQuotes()).thenReturn(List.of(testStock));

        List<StockDetailResponse> results = marketDataService.getAllActiveStocks();

        assertNotNull(results);
        assertEquals(1, results.size());
        StockDetailResponse detail = results.get(0);
        assertEquals("AAPL", detail.getSymbol());
        assertEquals("Apple Inc.", detail.getCompanyName());
        assertEquals("Technology", detail.getSector());
        assertEquals(new BigDecimal("182.5000"), detail.getCurrentPrice());
        assertEquals(new BigDecimal("2.5000"), detail.getChange());
        verify(stockRepository).findAllActiveWithQuotes();
    }

    @Test
    @DisplayName("getStockBySymbol should return details when stock exists")
    void shouldReturnStockBySymbol() {
        when(stockRepository.findBySymbolWithQuote("AAPL")).thenReturn(Optional.of(testStock));

        StockDetailResponse detail = marketDataService.getStockBySymbol("AAPL");

        assertNotNull(detail);
        assertEquals("AAPL", detail.getSymbol());
        assertEquals("Apple Inc.", detail.getCompanyName());
        assertEquals("Technology", detail.getSector());
        assertEquals(new BigDecimal("182.5000"), detail.getCurrentPrice());
    }

    @Test
    @DisplayName("getStockBySymbol should throw ResourceNotFoundException for unknown symbol")
    void shouldThrowNotFoundForUnknownSymbol() {
        when(stockRepository.findBySymbolWithQuote("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            marketDataService.getStockBySymbol("UNKNOWN");
        });
    }

    @Test
    @DisplayName("getHistoricalCandles should return candle DTOs for valid symbol and range")
    void shouldReturnHistoricalCandles() {
        HistoricalPrice candle = new HistoricalPrice(
                testStock,
                LocalDate.of(2024, 1, 15),
                new BigDecimal("180.0000"),
                new BigDecimal("185.0000"),
                new BigDecimal("179.0000"),
                new BigDecimal("183.0000"),
                45000000L
        );

        when(stockRepository.findBySymbolIgnoreCase("AAPL")).thenReturn(Optional.of(testStock));
        when(historicalPriceRepository.findCandlesBetween(eq("AAPL"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(candle));

        List<HistoricalCandleDTO> candles = marketDataService.getHistoricalCandles("AAPL", 30, null, null);

        assertNotNull(candles);
        assertEquals(1, candles.size());
        assertEquals("AAPL", candles.get(0).getSymbol());
        assertEquals(LocalDate.of(2024, 1, 15), candles.get(0).getPriceDate());
        assertEquals(new BigDecimal("183.0000"), candles.get(0).getClosePrice());
    }

    @Test
    @DisplayName("getHistoricalCandles should throw IllegalArgumentException when from date is after to date")
    void shouldThrowOnInvalidDateRange() {
        when(stockRepository.findBySymbolIgnoreCase("AAPL")).thenReturn(Optional.of(testStock));

        LocalDate from = LocalDate.of(2024, 2, 1);
        LocalDate to = LocalDate.of(2024, 1, 1);

        assertThrows(IllegalArgumentException.class, () -> {
            marketDataService.getHistoricalCandles("AAPL", null, from, to);
        });
    }

    @Test
    @DisplayName("getStockQuoteDTO should return standardized DTO from common-dto")
    void shouldReturnStandardQuoteDTO() {
        when(stockRepository.findBySymbolWithQuote("AAPL")).thenReturn(Optional.of(testStock));

        StockQuoteDTO dto = marketDataService.getStockQuoteDTO("AAPL");

        assertNotNull(dto);
        assertEquals("AAPL", dto.getSymbol());
        assertEquals("Apple Inc.", dto.getCompanyName());
        assertEquals("Technology", dto.getSector());
        assertEquals(new BigDecimal("182.5000"), dto.getCurrentPrice());
    }
}

