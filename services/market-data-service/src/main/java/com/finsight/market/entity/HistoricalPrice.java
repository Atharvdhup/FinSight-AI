package com.finsight.market.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "historical_prices")
public class HistoricalPrice implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "symbol", referencedColumnName = "symbol", nullable = false)
    private Stock stock;

    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    @Column(name = "open_price", precision = 19, scale = SCALE, nullable = false)
    private BigDecimal openPrice;

    @Column(name = "high_price", precision = 19, scale = SCALE, nullable = false)
    private BigDecimal highPrice;

    @Column(name = "low_price", precision = 19, scale = SCALE, nullable = false)
    private BigDecimal lowPrice;

    @Column(name = "close_price", precision = 19, scale = SCALE, nullable = false)
    private BigDecimal closePrice;

    @Column(name = "volume", nullable = false)
    private Long volume;

    public HistoricalPrice() {
    }

    public HistoricalPrice(Stock stock, LocalDate priceDate, BigDecimal openPrice,
                           BigDecimal highPrice, BigDecimal lowPrice, BigDecimal closePrice,
                           Long volume) {
        this.stock = stock;
        this.priceDate = priceDate;
        this.openPrice = openPrice != null ? openPrice.setScale(SCALE, ROUNDING) : null;
        this.highPrice = highPrice != null ? highPrice.setScale(SCALE, ROUNDING) : null;
        this.lowPrice = lowPrice != null ? lowPrice.setScale(SCALE, ROUNDING) : null;
        this.closePrice = closePrice != null ? closePrice.setScale(SCALE, ROUNDING) : null;
        this.volume = volume;
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

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(BigDecimal closePrice) {
        this.closePrice = closePrice != null ? closePrice.setScale(SCALE, ROUNDING) : null;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }
}
