package com.finsight.common.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event published by market-data-service when stock quotes are refreshed or simulated.
 * Consumed by alert-service for rule evaluation and portfolio-service for valuation.
 */
public class StockPriceUpdatedEvent extends BaseEvent<StockPriceUpdatedEvent.StockPricePayload> {
    private static final long serialVersionUID = 1L;

    public static final String EVENT_TYPE = "StockPriceUpdated";

    public StockPriceUpdatedEvent() {
    }

    public StockPriceUpdatedEvent(EventHeader header, StockPricePayload data) {
        super(header, data);
    }

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

        public StockPricePayload() {
        }

        public StockPricePayload(String symbol, BigDecimal price, BigDecimal previousClose,
                                 BigDecimal change, BigDecimal changePercent, String currency,
                                 Long volume, Instant marketTime) {
            this.symbol = symbol;
            this.price = price;
            this.previousClose = previousClose;
            this.change = change;
            this.changePercent = changePercent;
            this.currency = currency;
            this.volume = volume;
            this.marketTime = marketTime;
        }

        public static Builder builder() {
            return new Builder();
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public BigDecimal getPreviousClose() {
            return previousClose;
        }

        public void setPreviousClose(BigDecimal previousClose) {
            this.previousClose = previousClose;
        }

        public BigDecimal getChange() {
            return change;
        }

        public void setChange(BigDecimal change) {
            this.change = change;
        }

        public BigDecimal getChangePercent() {
            return changePercent;
        }

        public void setChangePercent(BigDecimal changePercent) {
            this.changePercent = changePercent;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public Long getVolume() {
            return volume;
        }

        public void setVolume(Long volume) {
            this.volume = volume;
        }

        public Instant getMarketTime() {
            return marketTime;
        }

        public void setMarketTime(Instant marketTime) {
            this.marketTime = marketTime;
        }

        public static class Builder {
            private String symbol;
            private BigDecimal price;
            private BigDecimal previousClose;
            private BigDecimal change;
            private BigDecimal changePercent;
            private String currency;
            private Long volume;
            private Instant marketTime;

            public Builder symbol(String symbol) {
                this.symbol = symbol;
                return this;
            }

            public Builder price(BigDecimal price) {
                this.price = price;
                return this;
            }

            public Builder previousClose(BigDecimal previousClose) {
                this.previousClose = previousClose;
                return this;
            }

            public Builder change(BigDecimal change) {
                this.change = change;
                return this;
            }

            public Builder changePercent(BigDecimal changePercent) {
                this.changePercent = changePercent;
                return this;
            }

            public Builder currency(String currency) {
                this.currency = currency;
                return this;
            }

            public Builder volume(Long volume) {
                this.volume = volume;
                return this;
            }

            public Builder marketTime(Instant marketTime) {
                this.marketTime = marketTime;
                return this;
            }

            public StockPricePayload build() {
                return new StockPricePayload(symbol, price, previousClose, change, changePercent, currency, volume, marketTime);
            }
        }
    }

    public static StockPriceUpdatedEvent of(String source, StockPricePayload payload) {
        EventHeader header = EventHeader.builder()
                .eventType(EVENT_TYPE)
                .source(source)
                .build();
        return new StockPriceUpdatedEvent(header, payload);
    }
}
