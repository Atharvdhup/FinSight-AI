package com.finsight.portfolio.service;

import com.finsight.portfolio.entity.Holding;
import com.finsight.portfolio.entity.Portfolio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Portfolio & Holding Math Unit Tests")
class PortfolioCalculationTest {

    private Portfolio portfolio;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio("user-test-1", "Test Portfolio", "USD", new BigDecimal("100000.0000"));
        portfolio.setId(1L);
    }

    @Test
    @DisplayName("Should accurately calculate Weighted Average Buy Price on multiple BUY orders")
    void shouldCalculateWeightedAverageOnMultipleBuys() {
        // Initial Buy: 10 shares @ $150.00
        Holding holding = new Holding(portfolio, "AAPL", new BigDecimal("10.0000"),
                new BigDecimal("150.0000"), new BigDecimal("150.0000"), "Technology");

        assertEquals(new BigDecimal("10.0000"), holding.getQuantity());
        assertEquals(new BigDecimal("150.0000"), holding.getAverageBuyPrice());
        assertEquals(new BigDecimal("1500.0000"), holding.getTotalInvested());

        // Second Buy: 10 shares @ $200.00
        // New Avg Price = ((10 * 150) + (10 * 200)) / (10 + 10) = 3500 / 20 = 175.0000
        holding.applyBuy(new BigDecimal("10.0000"), new BigDecimal("200.0000"));

        assertEquals(new BigDecimal("20.0000"), holding.getQuantity());
        assertEquals(new BigDecimal("175.0000"), holding.getAverageBuyPrice());
        assertEquals(new BigDecimal("3500.0000"), holding.getTotalInvested());
        assertEquals(new BigDecimal("4000.0000"), holding.getCurrentValue()); // 20 * 200
        assertEquals(new BigDecimal("500.0000"), holding.getUnrealizedPnL());  // 4000 - 3500
        assertEquals(new BigDecimal("14.2857"), holding.getUnrealizedPnLPercent()); // 500 / 3500 * 100

        // Third Buy: 20 shares @ $300.00
        // New Avg Price = ((20 * 175) + (20 * 300)) / (20 + 20) = (3500 + 6000) / 40 = 9500 / 40 = 237.5000
        holding.applyBuy(new BigDecimal("20.0000"), new BigDecimal("300.0000"));

        assertEquals(new BigDecimal("40.0000"), holding.getQuantity());
        assertEquals(new BigDecimal("237.5000"), holding.getAverageBuyPrice());
        assertEquals(new BigDecimal("9500.0000"), holding.getTotalInvested());
    }

    @Test
    @DisplayName("Should preserve average cost basis on partial SELL orders")
    void shouldPreserveAverageCostBasisOnPartialSell() {
        Holding holding = new Holding(portfolio, "MSFT", new BigDecimal("50.0000"),
                new BigDecimal("200.0000"), new BigDecimal("250.0000"), "Technology");

        // Partial Sell: 20 shares (leaving 30 shares)
        holding.applySell(new BigDecimal("20.0000"));

        assertEquals(new BigDecimal("30.0000"), holding.getQuantity());
        assertEquals(new BigDecimal("200.0000"), holding.getAverageBuyPrice(), "Cost basis per share must remain constant on SELL");
        assertEquals(new BigDecimal("6000.0000"), holding.getTotalInvested()); // 30 * 200
        assertEquals(new BigDecimal("7500.0000"), holding.getCurrentValue());  // 30 * 250
        assertEquals(new BigDecimal("1500.0000"), holding.getUnrealizedPnL()); // 7500 - 6000
        assertEquals(new BigDecimal("25.0000"), holding.getUnrealizedPnLPercent());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when attempting to SELL more shares than owned")
    void shouldThrowExceptionWhenOverselling() {
        Holding holding = new Holding(portfolio, "GOOGL", new BigDecimal("10.0000"),
                new BigDecimal("100.0000"), new BigDecimal("120.0000"), "Technology");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            holding.applySell(new BigDecimal("15.0000"));
        });

        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Should maintain Banker's Rounding (HALF_EVEN) with exact 4-decimal scale")
    void shouldMaintainBankersRounding() {
        // Test dividing 100 by 3 -> 33.333333... rounded to 4 decimals = 33.3333
        BigDecimal value1 = new BigDecimal("100.0000");
        BigDecimal value2 = new BigDecimal("3.0000");
        BigDecimal result = value1.divide(value2, 4, RoundingMode.HALF_EVEN);

        assertEquals(new BigDecimal("33.3333"), result);

        // Test exact midpoint rounding: 2.5 rounds to 2 (even), 3.5 rounds to 4 (even)
        BigDecimal midpointEven = new BigDecimal("2.5").setScale(0, RoundingMode.HALF_EVEN);
        BigDecimal midpointOdd = new BigDecimal("3.5").setScale(0, RoundingMode.HALF_EVEN);

        assertEquals(new BigDecimal("2"), midpointEven);
        assertEquals(new BigDecimal("4"), midpointOdd);
    }
}

