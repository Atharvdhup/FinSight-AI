package com.finsight.portfolio.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class StockAllocationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String symbol;
    private String sector;
    private BigDecimal quantity;
    private BigDecimal currentPrice;
    private BigDecimal currentValue;
    private BigDecimal weightPercent;

    public StockAllocationDTO() {
    }

    public StockAllocationDTO(String symbol, String sector, BigDecimal quantity,
                              BigDecimal currentPrice, BigDecimal currentValue, BigDecimal weightPercent) {
        this.symbol = symbol;
        this.sector = sector;
        this.quantity = quantity;
        this.currentPrice = currentPrice;
        this.currentValue = currentValue;
        this.weightPercent = weightPercent;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public BigDecimal getWeightPercent() {
        return weightPercent;
    }

    public void setWeightPercent(BigDecimal weightPercent) {
        this.weightPercent = weightPercent;
    }
}

