package com.finsight.portfolio.dto;

import com.finsight.common.enums.TransactionType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class TransactionResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long portfolioId;
    private String idempotencyKey;
    private String symbol;
    private TransactionType transactionType;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private BigDecimal fees;
    private Instant executedAt;
    private String status; // "EXECUTED" or "ALREADY_PROCESSED"

    public TransactionResponse() {
    }

    public TransactionResponse(Long id, Long portfolioId, String idempotencyKey, String symbol,
                               TransactionType transactionType, BigDecimal quantity, BigDecimal price,
                               BigDecimal totalAmount, BigDecimal fees, Instant executedAt, String status) {
        this.id = id;
        this.portfolioId = portfolioId;
        this.idempotencyKey = idempotencyKey;
        this.symbol = symbol;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.price = price;
        this.totalAmount = totalAmount;
        this.fees = fees;
        this.executedAt = executedAt;
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
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

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getFees() {
        return fees;
    }

    public void setFees(BigDecimal fees) {
        this.fees = fees;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(Instant executedAt) {
        this.executedAt = executedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static class Builder {
        private Long id;
        private Long portfolioId;
        private String idempotencyKey;
        private String symbol;
        private TransactionType transactionType;
        private BigDecimal quantity;
        private BigDecimal price;
        private BigDecimal totalAmount;
        private BigDecimal fees;
        private Instant executedAt;
        private String status;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder portfolioId(Long portfolioId) {
            this.portfolioId = portfolioId;
            return this;
        }

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public Builder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder transactionType(TransactionType transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public Builder quantity(BigDecimal quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder fees(BigDecimal fees) {
            this.fees = fees;
            return this;
        }

        public Builder executedAt(Instant executedAt) {
            this.executedAt = executedAt;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public TransactionResponse build() {
            return new TransactionResponse(id, portfolioId, idempotencyKey, symbol, transactionType,
                    quantity, price, totalAmount, fees, executedAt, status);
        }
    }
}
