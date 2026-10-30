package com.finsight.market.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Entity
@Table(name = "stock_quotes")
public class StockQuote implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "symbol", referencedColumnName = "symbol", nullable = false, unique = true)
    private Stock stock;

    @Column(name = "current_price", precision = 19, scale = SCALE, nullable = false)
    private BigDecimal currentPrice;

    @Column(name = "open_price", precision = 19, scale = SCALE)
    private BigDecimal openPrice;

    @Column(name = "high_price", precision = 19, scale = SCALE)
    private BigDecimal highPrice;

    @Column(name = "low_price", precision = 19, scale = SCALE)
    private BigDecimal lowPrice;

    @Column(name = "previous_close", precision = 19, scale = SCALE)
    private BigDecimal previousClose;

    @Column(name = "change_amount", precision = 19, scale = SCALE)
    private BigDecimal changeAmount;

    @Column(name = "change_pct", precision = 8, scale = SCALE)
    private BigDecimal changePct;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "market_time", nullable = false)
    private Instant marketTime;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public StockQuote() {
    }

    public StockQuote(Stock stock, BigDecimal currentPrice, BigDecimal openPrice,
                      BigDecimal highPrice, BigDecimal lowPrice, BigDecimal previousClose,
                      Long volume, Instant marketTime) {
        this.stock = stock;
        this.currentPrice = currentPrice != null ? currentPrice.setScale(SCALE, ROUNDING) : null;
        this.openPrice = openPrice != null ? openPrice.setScale(SCALE, ROUNDING) : this.currentPrice;
        this.highPrice = highPrice != null ? highPrice.setScale(SCALE, ROUNDING) : this.currentPrice;
        this.lowPrice = lowPrice != null ? lowPrice.setScale(SCALE, ROUNDING) : this.currentPrice;
        this.previousClose = previousClose != null ? previousClose.setScale(SCALE, ROUNDING) : this.currentPrice;
        this.volume = volume;
        this.marketTime = marketTime != null ? marketTime : Instant.now();
        calculateChanges();
    }

    public void recalculateMetrics() {
        calculateChanges();
    }

    private void calculateChanges() {
        if (currentPrice != null && previousClose != null && previousClose.compareTo(BigDecimal.ZERO) != 0) {
            this.changeAmount = currentPrice.subtract(previousClose).setScale(SCALE, ROUNDING);
            this.changePct = changeAmount.divide(previousClose, 6, ROUNDING)
                    .multiply(new BigDecimal("100"))
                    .setScale(SCALE, ROUNDING);
        } else {
            this.changeAmount = BigDecimal.ZERO.setScale(SCALE, ROUNDING);
            this.changePct = BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
        if (marketTime == null) {
            marketTime = Instant.now();
        }
        calculateChanges();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        calculateChanges();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice != null ? currentPrice.setScale(SCALE, ROUNDING) : null;
        calculateChanges();
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(BigDecimal openPrice) {
        this.openPrice = openPrice != null ? openPrice.setScale(SCALE, ROUNDING) : null;
    }

    public BigDecimal getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(BigDecimal highPrice) {
        this.highPrice = highPrice != null ? highPrice.setScale(SCALE, ROUNDING) : null;
    }

    public BigDecimal getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(BigDecimal lowPrice) {
        this.lowPrice = lowPrice != null ? lowPrice.setScale(SCALE, ROUNDING) : null;
    }

    public BigDecimal getPreviousClose() {
        return previousClose;
    }

    public void setPreviousClose(BigDecimal previousClose) {
        this.previousClose = previousClose != null ? previousClose.setScale(SCALE, ROUNDING) : null;
        calculateChanges();
    }

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(BigDecimal changeAmount) {
        this.changeAmount = changeAmount;
    }

    public BigDecimal getChangePct() {
        return changePct;
    }

    public void setChangePct(BigDecimal changePct) {
        this.changePct = changePct;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
