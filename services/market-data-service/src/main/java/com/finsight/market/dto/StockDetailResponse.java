package com.finsight.market.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class StockDetailResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String symbol;
    private String companyName;
    private String sector;
    private String industry;
    private String currency;
    private BigDecimal currentPrice;
    private BigDecimal change;
    private BigDecimal changePercent;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal previousClose;
    private Long volume;
    private Instant marketTime;

    public StockDetailResponse() {
    }

    public StockDetailResponse(String symbol, String companyName, String sector, String industry,
                               String currency, BigDecimal currentPrice, BigDecimal change,
                               BigDecimal changePercent, BigDecimal openPrice, BigDecimal highPrice,
                               BigDecimal lowPrice, BigDecimal previousClose, Long volume, Instant marketTime) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.sector = sector;
        this.industry = industry;
        this.currency = currency;
        this.currentPrice = currentPrice;
        this.change = change;
        this.changePercent = changePercent;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.previousClose = previousClose;
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

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
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

    public BigDecimal getPreviousClose() {
        return previousClose;
    }

    public void setPreviousClose(BigDecimal previousClose) {
        this.previousClose = previousClose;
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
        private String companyName;
        private String sector;
        private String industry;
        private String currency;
        private BigDecimal currentPrice;
        private BigDecimal change;
        private BigDecimal changePercent;
        private BigDecimal openPrice;
        private BigDecimal highPrice;
        private BigDecimal lowPrice;
        private BigDecimal previousClose;
        private Long volume;
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

        public Builder industry(String industry) {
            this.industry = industry;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder currentPrice(BigDecimal currentPrice) {
            this.currentPrice = currentPrice;
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

        public Builder previousClose(BigDecimal previousClose) {
            this.previousClose = previousClose;
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

        public StockDetailResponse build() {
            return new StockDetailResponse(symbol, companyName, sector, industry, currency,
                    currentPrice, change, changePercent, openPrice, highPrice, lowPrice,
                    previousClose, volume, marketTime);
        }
    }
}
