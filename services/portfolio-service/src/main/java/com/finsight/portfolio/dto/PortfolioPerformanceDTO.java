package com.finsight.portfolio.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class PortfolioPerformanceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long portfolioId;
    private String portfolioName;
    private String currency;
    private BigDecimal cashBalance;
    private BigDecimal totalInvested;
    private BigDecimal totalCurrentValue;
    private BigDecimal totalPortfolioValue;
    private BigDecimal totalUnrealizedPnL;
    private BigDecimal totalUnrealizedPnLPercent;
    private BigDecimal dailyChangeAmount;
    private BigDecimal dailyChangePercent;
    private Instant asOf;

    public PortfolioPerformanceDTO() {
    }

    public PortfolioPerformanceDTO(Long portfolioId, String portfolioName, String currency,
                                   BigDecimal cashBalance, BigDecimal totalInvested,
                                   BigDecimal totalCurrentValue, BigDecimal totalPortfolioValue,
                                   BigDecimal totalUnrealizedPnL, BigDecimal totalUnrealizedPnLPercent,
                                   BigDecimal dailyChangeAmount, BigDecimal dailyChangePercent,
                                   Instant asOf) {
        this.portfolioId = portfolioId;
        this.portfolioName = portfolioName;
        this.currency = currency;
        this.cashBalance = cashBalance;
        this.totalInvested = totalInvested;
        this.totalCurrentValue = totalCurrentValue;
        this.totalPortfolioValue = totalPortfolioValue;
        this.totalUnrealizedPnL = totalUnrealizedPnL;
        this.totalUnrealizedPnLPercent = totalUnrealizedPnLPercent;
        this.dailyChangeAmount = dailyChangeAmount;
        this.dailyChangePercent = dailyChangePercent;
        this.asOf = asOf;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(Long portfolioId) {
        this.portfolioId = portfolioId;
    }

    public String getPortfolioName() {
        return portfolioName;
    }

    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }

    public BigDecimal getTotalInvested() {
        return totalInvested;
    }

    public void setTotalInvested(BigDecimal totalInvested) {
        this.totalInvested = totalInvested;
    }

    public BigDecimal getTotalCurrentValue() {
        return totalCurrentValue;
    }

    public void setTotalCurrentValue(BigDecimal totalCurrentValue) {
        this.totalCurrentValue = totalCurrentValue;
    }

    public BigDecimal getTotalPortfolioValue() {
        return totalPortfolioValue;
    }

    public void setTotalPortfolioValue(BigDecimal totalPortfolioValue) {
        this.totalPortfolioValue = totalPortfolioValue;
    }

    public BigDecimal getTotalUnrealizedPnL() {
        return totalUnrealizedPnL;
    }

    public void setTotalUnrealizedPnL(BigDecimal totalUnrealizedPnL) {
        this.totalUnrealizedPnL = totalUnrealizedPnL;
    }

    public BigDecimal getTotalUnrealizedPnLPercent() {
        return totalUnrealizedPnLPercent;
    }

    public void setTotalUnrealizedPnLPercent(BigDecimal totalUnrealizedPnLPercent) {
        this.totalUnrealizedPnLPercent = totalUnrealizedPnLPercent;
    }

    public BigDecimal getDailyChangeAmount() {
        return dailyChangeAmount;
    }

    public void setDailyChangeAmount(BigDecimal dailyChangeAmount) {
        this.dailyChangeAmount = dailyChangeAmount;
    }

    public BigDecimal getDailyChangePercent() {
        return dailyChangePercent;
    }

    public void setDailyChangePercent(BigDecimal dailyChangePercent) {
        this.dailyChangePercent = dailyChangePercent;
    }

    public Instant getAsOf() {
        return asOf;
    }

    public void setAsOf(Instant asOf) {
        this.asOf = asOf;
    }

    public static class Builder {
        private Long portfolioId;
        private String portfolioName;
        private String currency;
        private BigDecimal cashBalance;
        private BigDecimal totalInvested;
        private BigDecimal totalCurrentValue;
        private BigDecimal totalPortfolioValue;
        private BigDecimal totalUnrealizedPnL;
        private BigDecimal totalUnrealizedPnLPercent;
        private BigDecimal dailyChangeAmount;
        private BigDecimal dailyChangePercent;
        private Instant asOf;

        public Builder portfolioId(Long portfolioId) {
            this.portfolioId = portfolioId;
            return this;
        }

        public Builder portfolioName(String portfolioName) {
            this.portfolioName = portfolioName;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder cashBalance(BigDecimal cashBalance) {
            this.cashBalance = cashBalance;
            return this;
        }

        public Builder totalInvested(BigDecimal totalInvested) {
            this.totalInvested = totalInvested;
            return this;
        }

        public Builder totalCurrentValue(BigDecimal totalCurrentValue) {
            this.totalCurrentValue = totalCurrentValue;
            return this;
        }

        public Builder totalPortfolioValue(BigDecimal totalPortfolioValue) {
            this.totalPortfolioValue = totalPortfolioValue;
            return this;
        }

        public Builder totalUnrealizedPnL(BigDecimal totalUnrealizedPnL) {
            this.totalUnrealizedPnL = totalUnrealizedPnL;
            return this;
        }

        public Builder totalUnrealizedPnLPercent(BigDecimal totalUnrealizedPnLPercent) {
            this.totalUnrealizedPnLPercent = totalUnrealizedPnLPercent;
            return this;
        }

        public Builder dailyChangeAmount(BigDecimal dailyChangeAmount) {
            this.dailyChangeAmount = dailyChangeAmount;
            return this;
        }

        public Builder dailyChangePercent(BigDecimal dailyChangePercent) {
            this.dailyChangePercent = dailyChangePercent;
            return this;
        }

        public Builder asOf(Instant asOf) {
            this.asOf = asOf;
            return this;
        }

        public PortfolioPerformanceDTO build() {
            return new PortfolioPerformanceDTO(portfolioId, portfolioName, currency,
                    cashBalance, totalInvested, totalCurrentValue, totalPortfolioValue,
                    totalUnrealizedPnL, totalUnrealizedPnLPercent, dailyChangeAmount,
                    dailyChangePercent, asOf);
        }
    }
}

