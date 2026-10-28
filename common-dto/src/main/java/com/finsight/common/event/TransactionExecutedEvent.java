package com.finsight.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event published by portfolio-service via Transactional Outbox when a trade is executed.
 * Consumed by research/analytics and notification consumers.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TransactionExecutedEvent extends BaseEvent<TransactionExecutedEvent.TransactionPayload> {
    private static final long serialVersionUID = 1L;

    public static final String EVENT_TYPE = "TransactionExecuted";

    public TransactionExecutedEvent(EventHeader header, TransactionPayload data) {
        super(header, data);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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
    }

    public static TransactionExecutedEvent of(String source, TransactionPayload payload) {
        EventHeader header = EventHeader.builder()
                .eventType(EVENT_TYPE)
                .source(source)
                .build();
        return new TransactionExecutedEvent(header, payload);
    }
}
