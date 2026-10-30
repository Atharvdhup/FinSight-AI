package com.finsight.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class HoldingDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long portfolioId;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal averageBuyPrice;
    private BigDecimal currentPrice;
    private BigDecimal totalInvested;
    private BigDecimal currentValue;
    private BigDecimal unrealizedPnL;
    private BigDecimal unrealizedPnLPercent;
    private BigDecimal portfolioWeightPercent;
    private String sector;
    private Instant updatedAt;

    public HoldingDTO() {
    }

    public HoldingDTO(Long id, Long portfolioId, String symbol, BigDecimal quantity, BigDecimal averageBuyPrice,
                      BigDecimal currentPrice, BigDecimal totalInvested, BigDecimal currentValue,
                      BigDecimal unrealizedPnL, BigDecimal unrealizedPnLPercent, BigDecimal portfolioWeightPercent,
                      String sector, Instant updatedAt) {
        this.id = id;
        this.portfolioId = portfolioId;
        this.symbol = symbol;
        this.quantity = quantity;
        this.averageBuyPrice = averageBuyPrice;
        this.currentPrice = currentPrice;
        this.totalInvested = totalInvested;
        this.currentValue = currentValue;
        this.unrealizedPnL = unrealizedPnL;
        this.unrealizedPnLPercent = unrealizedPnLPercent;
        this.portfolioWeightPercent = portfolioWeightPercent;
        this.sector = sector;
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

    public Long getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(Long portfolioId) {
        this.portfolioId = portfolioId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAverageBuyPrice() {
        return averageBuyPrice;
    }

    public void setAverageBuyPrice(BigDecimal averageBuyPrice) {
        this.averageBuyPrice = averageBuyPrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getTotalInvested() {
        return totalInvested;
    }

    public void setTotalInvested(BigDecimal totalInvested) {
        this.totalInvested = totalInvested;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public BigDecimal getUnrealizedPnL() {
        return unrealizedPnL;
    }

    public void setUnrealizedPnL(BigDecimal unrealizedPnL) {
        this.unrealizedPnL = unrealizedPnL;
    }

    public BigDecimal getUnrealizedPnLPercent() {
        return unrealizedPnLPercent;
    }

    public void setUnrealizedPnLPercent(BigDecimal unrealizedPnLPercent) {
        this.unrealizedPnLPercent = unrealizedPnLPercent;
    }

    public BigDecimal getPortfolioWeightPercent() {
        return portfolioWeightPercent;
    }

    public void setPortfolioWeightPercent(BigDecimal portfolioWeightPercent) {
        this.portfolioWeightPercent = portfolioWeightPercent;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class Builder {
        private Long id;
        private Long portfolioId;
        private String symbol;
        private BigDecimal quantity;
        private BigDecimal averageBuyPrice;
        private BigDecimal currentPrice;
        private BigDecimal totalInvested;
        private BigDecimal currentValue;
        private BigDecimal unrealizedPnL;
        private BigDecimal unrealizedPnLPercent;
        private BigDecimal portfolioWeightPercent;
        private String sector;
        private Instant updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder portfolioId(Long portfolioId) {
            this.portfolioId = portfolioId;
            return this;
        }

        public Builder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder quantity(BigDecimal quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder averageBuyPrice(BigDecimal averageBuyPrice) {
            this.averageBuyPrice = averageBuyPrice;
            return this;
        }

        public Builder currentPrice(BigDecimal currentPrice) {
            this.currentPrice = currentPrice;
            return this;
        }

        public Builder totalInvested(BigDecimal totalInvested) {
            this.totalInvested = totalInvested;
            return this;
        }

        public Builder currentValue(BigDecimal currentValue) {
            this.currentValue = currentValue;
            return this;
        }

        public Builder unrealizedPnL(BigDecimal unrealizedPnL) {
            this.unrealizedPnL = unrealizedPnL;
            return this;
        }

        public Builder unrealizedPnLPercent(BigDecimal unrealizedPnLPercent) {
            this.unrealizedPnLPercent = unrealizedPnLPercent;
            return this;
        }

        public Builder portfolioWeightPercent(BigDecimal portfolioWeightPercent) {
            this.portfolioWeightPercent = portfolioWeightPercent;
            return this;
        }

        public Builder sector(String sector) {
            this.sector = sector;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public HoldingDTO build() {
            return new HoldingDTO(id, portfolioId, symbol, quantity, averageBuyPrice, currentPrice,
                    totalInvested, currentValue, unrealizedPnL, unrealizedPnLPercent, portfolioWeightPercent,
                    sector, updatedAt);
        }
    }
}
