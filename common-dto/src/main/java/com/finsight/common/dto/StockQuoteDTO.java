package com.finsight.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class StockQuoteDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String symbol;
    private String companyName;
    private String sector;
    private BigDecimal currentPrice;
    private BigDecimal previousClose;
    private BigDecimal change;
    private BigDecimal changePercent;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private Long volume;
    private String currency;
    private Instant marketTime;

    public StockQuoteDTO() {
    }

    public StockQuoteDTO(String symbol, String companyName, String sector, BigDecimal currentPrice,
                         BigDecimal previousClose, BigDecimal change, BigDecimal changePercent,
                         BigDecimal openPrice, BigDecimal highPrice, BigDecimal lowPrice,
                         Long volume, String currency, Instant marketTime) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.sector = sector;
        this.currentPrice = currentPrice;
        this.previousClose = previousClose;
        this.change = change;
        this.changePercent = changePercent;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.volume = volume;
        this.currency = currency;
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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
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

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Instant getMarketTime() {
        return marketTime;
    }

    public void setMarketTime(Instant marketTime) {
        this.marketTime = marketTime;
    }

    public static class Builder {
        private String symbol;
        private String companyName;
        private String sector;
        private BigDecimal currentPrice;
        private BigDecimal previousClose;
        private BigDecimal change;
        private BigDecimal changePercent;
        private BigDecimal openPrice;
        private BigDecimal highPrice;
        private BigDecimal lowPrice;
        private Long volume;
        private String currency;
        private Instant marketTime;

        public Builder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder companyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public Builder sector(String sector) {
            this.sector = sector;
            return this;
        }

        public Builder currentPrice(BigDecimal currentPrice) {
            this.currentPrice = currentPrice;
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

        public Builder volume(Long volume) {
            this.volume = volume;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder marketTime(Instant marketTime) {
            this.marketTime = marketTime;
            return this;
        }

        public StockQuoteDTO build() {
            return new StockQuoteDTO(symbol, companyName, sector, currentPrice, previousClose,
                    change, changePercent, openPrice, highPrice, lowPrice, volume, currency, marketTime);
        }
    }
}
