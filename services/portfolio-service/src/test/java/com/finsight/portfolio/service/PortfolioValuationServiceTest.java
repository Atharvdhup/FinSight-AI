package com.finsight.portfolio.service;

import com.finsight.common.dto.StockQuoteDTO;
import com.finsight.portfolio.client.MarketDataClient;
import com.finsight.portfolio.dto.PortfolioAllocationResponse;
import com.finsight.portfolio.dto.PortfolioPerformanceDTO;
import com.finsight.portfolio.entity.Holding;
import com.finsight.portfolio.entity.Portfolio;
import com.finsight.portfolio.exception.ResourceNotFoundException;
import com.finsight.portfolio.repository.HoldingRepository;
import com.finsight.portfolio.repository.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioValuationService Unit Tests")
class PortfolioValuationServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private MarketDataClient marketDataClient;

    @InjectMocks
    private PortfolioValuationService valuationService;

    private Portfolio portfolio;
    private Holding aaplHolding;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio("user-1", "Growth Portfolio", "USD", new BigDecimal("50000.0000"));
        portfolio.setId(10L);

        aaplHolding = new Holding(portfolio, "AAPL", new BigDecimal("10.0000"),
                new BigDecimal("150.0000"), new BigDecimal("150.0000"), "Technology");
        aaplHolding.setId(101L);
    }

    @Test
    @DisplayName("calculatePerformance should compute valuation, P&L, and daily return using live quotes")
    void shouldCalculatePerformanceWithLiveQuote() {
        when(portfolioRepository.findById(10L)).thenReturn(Optional.of(portfolio));
        when(holdingRepository.findByPortfolioId(10L)).thenReturn(List.of(aaplHolding));

        StockQuoteDTO liveQuote = StockQuoteDTO.builder()
                .symbol("AAPL")
                .currentPrice(new BigDecimal("190.0000"))
                .previousClose(new BigDecimal("185.0000"))
                .change(new BigDecimal("5.0000"))
                .changePercent(new BigDecimal("2.7027"))
                .build();

        when(marketDataClient.getLiveQuote("AAPL")).thenReturn(Optional.of(liveQuote));

        PortfolioPerformanceDTO performance = valuationService.calculatePerformance(10L);

        assertNotNull(performance);
        assertEquals(10L, performance.getPortfolioId());
        // Invested = 10 * 150 = 1500.0000
        assertEquals(new BigDecimal("1500.0000"), performance.getTotalInvested());
        // Current Holdings Value = 10 * 190 = 1900.0000
        assertEquals(new BigDecimal("1900.0000"), performance.getTotalCurrentValue());
        // Total Portfolio = 50000 + 1900 = 51900.0000
        assertEquals(new BigDecimal("51900.0000"), performance.getTotalPortfolioValue());
        // Unrealized P&L = 1900 - 1500 = 400.0000
        assertEquals(new BigDecimal("400.0000"), performance.getTotalUnrealizedPnL());
        // Unrealized P&L % = 400 / 1500 * 100 = 26.6667%
        assertEquals(new BigDecimal("26.6667"), performance.getTotalUnrealizedPnLPercent());
        // Daily Change = 10 * 5 = 50.0000
        assertEquals(new BigDecimal("50.0000"), performance.getDailyChangeAmount());
    }

    @Test
    @DisplayName("calculatePerformance should gracefully fallback to holding cached price when live quote fails")
    void shouldFallbackToCachedPriceWhenMarketClientFails() {
        when(portfolioRepository.findById(10L)).thenReturn(Optional.of(portfolio));
        when(holdingRepository.findByPortfolioId(10L)).thenReturn(List.of(aaplHolding));
        when(marketDataClient.getLiveQuote("AAPL")).thenReturn(Optional.empty());

        PortfolioPerformanceDTO performance = valuationService.calculatePerformance(10L);

        assertNotNull(performance);
        // Uses holding.getCurrentPrice() which is 150.0000
        assertEquals(new BigDecimal("1500.0000"), performance.getTotalCurrentValue());
        assertEquals(new BigDecimal("0.0000"), performance.getTotalUnrealizedPnL());
    }

    @Test
    @DisplayName("calculateAllocation should calculate stock and sector weight percentages accurately")
    void shouldCalculateAllocationPercentages() {
        portfolio.setCashBalance(new BigDecimal("10000.0000"));

        Holding msftHolding = new Holding(portfolio, "MSFT", new BigDecimal("20.0000"),
                new BigDecimal("200.0000"), new BigDecimal("200.0000"), "Technology");
        msftHolding.setId(102L);

        when(portfolioRepository.findById(10L)).thenReturn(Optional.of(portfolio));
        when(holdingRepository.findByPortfolioId(10L)).thenReturn(List.of(aaplHolding, msftHolding));

        // AAPL price = 600.00 -> 10 * 600 = 6,000
        when(marketDataClient.getLiveQuote("AAPL")).thenReturn(Optional.of(
                StockQuoteDTO.builder().symbol("AAPL").currentPrice(new BigDecimal("600.0000")).build()
        ));
        // MSFT price = 200.00 -> 20 * 200 = 4,000
        when(marketDataClient.getLiveQuote("MSFT")).thenReturn(Optional.of(
                StockQuoteDTO.builder().symbol("MSFT").currentPrice(new BigDecimal("200.0000")).build()
        ));

        PortfolioAllocationResponse allocation = valuationService.calculateAllocation(10L);

        assertNotNull(allocation);
        // Total value = 10,000 (cash) + 6,000 (AAPL) + 4,000 (MSFT) = 20,000.0000
        assertEquals(new BigDecimal("20000.0000"), allocation.getTotalPortfolioValue());
        // Cash weight = 10,000 / 20,000 = 50.00%
        assertEquals(new BigDecimal("50.00"), allocation.getCashWeightPercent());
        // Holdings weight = 10,000 / 20,000 = 50.00%
        assertEquals(new BigDecimal("50.00"), allocation.getHoldingsWeightPercent());

        // Stock weights: AAPL = 6,000 / 20,000 = 30.00%, MSFT = 4,000 / 20,000 = 20.00%
        assertEquals(2, allocation.getStockAllocations().size());
        assertEquals(new BigDecimal("30.00"), allocation.getStockAllocations().get(0).getWeightPercent());
        assertEquals(new BigDecimal("20.00"), allocation.getStockAllocations().get(1).getWeightPercent());

        // Technology sector = (6000 + 4000) / 20000 = 50.00%
        assertEquals(1, allocation.getSectorAllocations().size());
        assertEquals("Technology", allocation.getSectorAllocations().get(0).getSector());
        assertEquals(new BigDecimal("50.00"), allocation.getSectorAllocations().get(0).getWeightPercent());
    }

    @Test
    @DisplayName("calculatePerformance should throw ResourceNotFoundException for non-existent portfolio")
    void shouldThrowWhenPortfolioNotFound() {
        when(portfolioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            valuationService.calculatePerformance(999L);
        });
    }
}

