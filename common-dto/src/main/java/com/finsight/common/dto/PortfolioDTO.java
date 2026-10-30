package com.finsight.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PortfolioDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String userId;
    private String name;
    private String currency;
    private BigDecimal cashBalance;
    private BigDecimal totalInvested;
    private BigDecimal totalCurrentValue;
    private BigDecimal totalUnrealizedPnL;
    private BigDecimal totalUnrealizedPnLPercent;
    private List<HoldingDTO> holdings = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    public PortfolioDTO() {
    }

    public PortfolioDTO(Long id, String userId, String name, String currency, BigDecimal cashBalance,
                        BigDecimal totalInvested, BigDecimal totalCurrentValue, BigDecimal totalUnrealizedPnL,
                        BigDecimal totalUnrealizedPnLPercent, List<HoldingDTO> holdings, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.currency = currency;
        this.cashBalance = cashBalance;
        this.totalInvested = totalInvested;
        this.totalCurrentValue = totalCurrentValue;
        this.totalUnrealizedPnL = totalUnrealizedPnL;
        this.totalUnrealizedPnLPercent = totalUnrealizedPnLPercent;
        this.holdings = holdings != null ? holdings : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Explicit Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<HoldingDTO> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<HoldingDTO> holdings) {
        this.holdings = holdings != null ? holdings : new ArrayList<>();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class Builder {
        private Long id;
        private String userId;
        private String name;
        private String currency;
        private BigDecimal cashBalance;
        private BigDecimal totalInvested;
        private BigDecimal totalCurrentValue;
        private BigDecimal totalUnrealizedPnL;
        private BigDecimal totalUnrealizedPnLPercent;
        private List<HoldingDTO> holdings = new ArrayList<>();
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
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

        public Builder totalUnrealizedPnL(BigDecimal totalUnrealizedPnL) {
            this.totalUnrealizedPnL = totalUnrealizedPnL;
            return this;
        }

        public Builder totalUnrealizedPnLPercent(BigDecimal totalUnrealizedPnLPercent) {
            this.totalUnrealizedPnLPercent = totalUnrealizedPnLPercent;
            return this;
        }

        public Builder holdings(List<HoldingDTO> holdings) {
            this.holdings = holdings != null ? holdings : new ArrayList<>();
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public PortfolioDTO build() {
            return new PortfolioDTO(id, userId, name, currency, cashBalance, totalInvested, totalCurrentValue,
                    totalUnrealizedPnL, totalUnrealizedPnLPercent, holdings, createdAt, updatedAt);
        }
    }
}
