package com.finsight.market.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class SeedStockItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String symbol;
    private String companyName;
    private String sector;
    private String industry;
    private String currency;
    private BigDecimal baselinePrice;
    private Long averageVolume;

    public SeedStockItem() {
    }

    public SeedStockItem(String symbol, String companyName, String sector, String industry,
                         String currency, BigDecimal baselinePrice, Long averageVolume) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.sector = sector;
        this.industry = industry;
        this.currency = currency;
        this.baselinePrice = baselinePrice;
        this.averageVolume = averageVolume;
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

    public BigDecimal getBaselinePrice() {
        return baselinePrice;
    }

    public void setBaselinePrice(BigDecimal baselinePrice) {
        this.baselinePrice = baselinePrice;
    }

    public Long getAverageVolume() {
        return averageVolume;
    }

    public void setAverageVolume(Long averageVolume) {
        this.averageVolume = averageVolume;
    }
}
