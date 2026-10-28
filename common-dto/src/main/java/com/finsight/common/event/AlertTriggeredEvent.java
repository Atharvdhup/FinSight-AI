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
 * Event published by alert-service when a price threshold rule evaluates to true.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AlertTriggeredEvent extends BaseEvent<AlertTriggeredEvent.AlertPayload> {
    private static final long serialVersionUID = 1L;

    public static final String EVENT_TYPE = "AlertTriggered";

    public AlertTriggeredEvent(EventHeader header, AlertPayload data) {
        super(header, data);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertPayload implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long alertRuleId;
        private Long notificationId;
        private String userId;
        private String symbol;
        private String conditionType;
        private BigDecimal triggeredPrice;
        private BigDecimal thresholdValue;
        private String title;
        private String message;
        private Instant triggeredAt;
    }

    public static AlertTriggeredEvent of(String source, AlertPayload payload) {
        EventHeader header = EventHeader.builder()
                .eventType(EVENT_TYPE)
                .source(source)
                .build();
        return new AlertTriggeredEvent(header, payload);
    }
}