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
 * Event published by market-data-service when stock quotes are refreshed or simulated.
 * Consumed by alert-service for rule evaluation and portfolio-service for valuation.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class StockPriceUpdatedEvent extends BaseEvent<StockPriceUpdatedEvent.StockPricePayload> {
    private static final long serialVersionUID = 1L;

    public static final String EVENT_TYPE = "StockPriceUpdated";

    public StockPriceUpdatedEvent(EventHeader header, StockPricePayload data) {
        super(header, data);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockPricePayload implements Serializable {
        private static final long serialVersionUID = 1L;

        private String symbol;
        private BigDecimal price;
        private BigDecimal previousClose;
        private BigDecimal change;
        private BigDecimal changePercent;
        private String currency;
        private Long volume;
        private Instant marketTime;
    }

    public static StockPriceUpdatedEvent of(String source, StockPricePayload payload) {
        EventHeader header = EventHeader.builder()
                .eventType(EVENT_TYPE)
                .source(source)
                .build();
        return new StockPriceUpdatedEvent(header, payload);
    }
}
