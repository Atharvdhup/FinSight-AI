package com.finsight.common.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event published by portfolio-service via Transactional Outbox when a trade is executed.
 * Consumed by research/analytics and notification consumers.
 */
public class TransactionExecutedEvent extends BaseEvent<TransactionExecutedEvent.TransactionPayload> {
    private static final long serialVersionUID = 1L;

    public static final String EVENT_TYPE = "TransactionExecuted";

    public TransactionExecutedEvent() {
    }

    public TransactionExecutedEvent(EventHeader header, TransactionPayload data) {
        super(header, data);
    }

    public static class TransactionPayload implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long portfolioId;
        private Long transactionId;
        private String idempotencyKey;
        private String symbol;
        private String transactionType; // BUY, SELL
        private BigDecimal quantity;
        private BigDecimal price;
        private BigDecimal totalAmount;
        private BigDecimal fees;
        private Instant executedAt;

        public TransactionPayload() {
        }

        public TransactionPayload(Long portfolioId, Long transactionId, String idempotencyKey,
                                  String symbol, String transactionType, BigDecimal quantity,
                                  BigDecimal price, BigDecimal totalAmount, BigDecimal fees,
                                  Instant executedAt) {
            this.portfolioId = portfolioId;
            this.transactionId = transactionId;
            this.idempotencyKey = idempotencyKey;
            this.symbol = symbol;
            this.transactionType = transactionType;
            this.quantity = quantity;
            this.price = price;
            this.totalAmount = totalAmount;
            this.fees = fees;
            this.executedAt = executedAt;
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

        public Long getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(Long transactionId) {
            this.transactionId = transactionId;
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

        public String getTransactionType() {
            return transactionType;
        }

        public void setTransactionType(String transactionType) {
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

        public static class Builder {
            private Long portfolioId;
            private Long transactionId;
            private String idempotencyKey;
            private String symbol;
            private String transactionType;
            private BigDecimal quantity;
            private BigDecimal price;
            private BigDecimal totalAmount;
            private BigDecimal fees;
            private Instant executedAt;

            public Builder portfolioId(Long portfolioId) {
                this.portfolioId = portfolioId;
                return this;
            }

            public Builder transactionId(Long transactionId) {
                this.transactionId = transactionId;
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

            public Builder transactionType(String transactionType) {
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

            public TransactionPayload build() {
                return new TransactionPayload(portfolioId, transactionId, idempotencyKey, symbol,
                        transactionType, quantity, price, totalAmount, fees, executedAt);
            }
        }
    }

    public static TransactionExecutedEvent of(String source, TransactionPayload payload) {
        EventHeader header = EventHeader.builder()
                .eventType(EVENT_TYPE)
                .source(source)
                .build();
        return new TransactionExecutedEvent(header, payload);
    }
}
