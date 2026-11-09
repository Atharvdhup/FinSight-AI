package com.finsight.common.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event published by alert-service when a price threshold rule evaluates to true.
 */
public class AlertTriggeredEvent extends BaseEvent<AlertTriggeredEvent.AlertPayload> {
    private static final long serialVersionUID = 1L;

    public static final String EVENT_TYPE = "AlertTriggered";

    public AlertTriggeredEvent() {
    }

    public AlertTriggeredEvent(EventHeader header, AlertPayload data) {
        super(header, data);
    }

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

        public AlertPayload() {
        }

        public AlertPayload(Long alertRuleId, Long notificationId, String userId, String symbol,
                            String conditionType, BigDecimal triggeredPrice, BigDecimal thresholdValue,
                            String title, String message, Instant triggeredAt) {
            this.alertRuleId = alertRuleId;
            this.notificationId = notificationId;
            this.userId = userId;
            this.symbol = symbol;
            this.conditionType = conditionType;
            this.triggeredPrice = triggeredPrice;
            this.thresholdValue = thresholdValue;
            this.title = title;
            this.message = message;
            this.triggeredAt = triggeredAt;
        }

        public static Builder builder() {
            return new Builder();
        }

        public Long getAlertRuleId() {
            return alertRuleId;
        }

        public void setAlertRuleId(Long alertRuleId) {
            this.alertRuleId = alertRuleId;
        }

        public Long getNotificationId() {
            return notificationId;
        }

        public void setNotificationId(Long notificationId) {
            this.notificationId = notificationId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getConditionType() {
            return conditionType;
        }

        public void setConditionType(String conditionType) {
            this.conditionType = conditionType;
        }

        public BigDecimal getTriggeredPrice() {
            return triggeredPrice;
        }

        public void setTriggeredPrice(BigDecimal triggeredPrice) {
            this.triggeredPrice = triggeredPrice;
        }

        public BigDecimal getThresholdValue() {
            return thresholdValue;
        }

        public void setThresholdValue(BigDecimal thresholdValue) {
            this.thresholdValue = thresholdValue;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Instant getTriggeredAt() {
            return triggeredAt;
        }

        public void setTriggeredAt(Instant triggeredAt) {
            this.triggeredAt = triggeredAt;
        }

        public static class Builder {
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

            public Builder alertRuleId(Long alertRuleId) {
                this.alertRuleId = alertRuleId;
                return this;
            }

            public Builder notificationId(Long notificationId) {
                this.notificationId = notificationId;
                return this;
            }

            public Builder userId(String userId) {
                this.userId = userId;
                return this;
            }

            public Builder symbol(String symbol) {
                this.symbol = symbol;
                return this;
            }

            public Builder conditionType(String conditionType) {
                this.conditionType = conditionType;
                return this;
            }

            public Builder triggeredPrice(BigDecimal triggeredPrice) {
                this.triggeredPrice = triggeredPrice;
                return this;
            }

            public Builder thresholdValue(BigDecimal thresholdValue) {
                this.thresholdValue = thresholdValue;
                return this;
            }

            public Builder title(String title) {
                this.title = title;
                return this;
            }

            public Builder message(String message) {
                this.message = message;
                return this;
            }

            public Builder triggeredAt(Instant triggeredAt) {
                this.triggeredAt = triggeredAt;
                return this;
            }

            public AlertPayload build() {
                return new AlertPayload(alertRuleId, notificationId, userId, symbol, conditionType,
                        triggeredPrice, thresholdValue, title, message, triggeredAt);
            }
        }
    }

    public static AlertTriggeredEvent of(String source, AlertPayload payload) {
        EventHeader header = EventHeader.builder()
                .eventType(EVENT_TYPE)
                .source(source)
                .build();
        return new AlertTriggeredEvent(header, payload);
    }
}
