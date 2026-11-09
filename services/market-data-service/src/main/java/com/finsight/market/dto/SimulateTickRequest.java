package com.finsight.market.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class SimulateTickRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String symbol;
    private BigDecimal newPrice;
    private BigDecimal percentChange;

    public SimulateTickRequest() {
    }

    public SimulateTickRequest(String symbol, BigDecimal newPrice, BigDecimal percentChange) {
        this.symbol = symbol;
        this.newPrice = newPrice;
        this.percentChange = percentChange;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(BigDecimal newPrice) {
        this.newPrice = newPrice;
    }

    public BigDecimal getPercentChange() {
        return percentChange;
    }

    public void setPercentChange(BigDecimal percentChange) {
        this.percentChange = percentChange;
    }
}

