package com.finsight.market.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class HistoricalCandleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String symbol;
    private LocalDate priceDate;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal closePrice;
    private Long volume;

    public HistoricalCandleDTO() {
    }

    public HistoricalCandleDTO(String symbol, LocalDate priceDate, BigDecimal openPrice,
                              BigDecimal highPrice, BigDecimal lowPrice, BigDecimal closePrice,
                              Long volume) {
        this.symbol = symbol;
        this.priceDate = priceDate;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
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

    public LocalDate getPriceDate() {
        return priceDate;
    }

    public void setPriceDate(LocalDate priceDate) {
        this.priceDate = priceDate;
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(BigDecimal openPrice) {
        this.openPrice = openPrice;
    }

    public BigDecimal getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(BigDecimal highPrice) {
        this.highPrice = highPrice;
    }

    public BigDecimal getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(BigDecimal lowPrice) {
        this.lowPrice = lowPrice;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(BigDecimal closePrice) {
        this.closePrice = closePrice;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public static class Builder {
        private String symbol;
        private LocalDate priceDate;
        private BigDecimal openPrice;
        private BigDecimal highPrice;
        private BigDecimal lowPrice;
        private BigDecimal closePrice;
        private Long volume;

        public Builder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder priceDate(LocalDate priceDate) {
            this.priceDate = priceDate;
            return this;
        }

        public Builder openPrice(BigDecimal openPrice) {
            this.openPrice = openPrice;
            return this;
        }

        public Builder highPrice(BigDecimal highPrice) {
            this.highPrice = highPrice;
            return this;
        }

        public Builder lowPrice(BigDecimal lowPrice) {
            this.lowPrice = lowPrice;
            return this;
        }

        public Builder closePrice(BigDecimal closePrice) {
            this.closePrice = closePrice;
            return this;
        }

        public Builder volume(Long volume) {
            this.volume = volume;
            return this;
        }

        public HistoricalCandleDTO build() {
            return new HistoricalCandleDTO(symbol, priceDate, openPrice, highPrice, lowPrice, closePrice, volume);
        }
    }
}
