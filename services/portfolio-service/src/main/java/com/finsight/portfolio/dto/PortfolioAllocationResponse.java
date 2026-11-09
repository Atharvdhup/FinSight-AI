package com.finsight.portfolio.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PortfolioAllocationResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long portfolioId;
    private BigDecimal totalPortfolioValue;
    private BigDecimal cashBalance;
    private BigDecimal cashWeightPercent;
    private BigDecimal holdingsWeightPercent;
    private List<SectorAllocationDTO> sectorAllocations = new ArrayList<>();
    private List<StockAllocationDTO> stockAllocations = new ArrayList<>();
    private Instant asOf;

    public PortfolioAllocationResponse() {
        this.asOf = Instant.now();
    }

    public PortfolioAllocationResponse(Long portfolioId, BigDecimal totalPortfolioValue,
                                       BigDecimal cashBalance, BigDecimal cashWeightPercent,
                                       BigDecimal holdingsWeightPercent, List<SectorAllocationDTO> sectorAllocations,
                                       List<StockAllocationDTO> stockAllocations, Instant asOf) {
        this.portfolioId = portfolioId;
        this.totalPortfolioValue = totalPortfolioValue;
        this.cashBalance = cashBalance;
        this.cashWeightPercent = cashWeightPercent;
        this.holdingsWeightPercent = holdingsWeightPercent;
        this.sectorAllocations = sectorAllocations != null ? sectorAllocations : new ArrayList<>();
        this.stockAllocations = stockAllocations != null ? stockAllocations : new ArrayList<>();
        this.asOf = asOf != null ? asOf : Instant.now();
    }

    public Long getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(Long portfolioId) {
        this.portfolioId = portfolioId;
    }

    public BigDecimal getTotalPortfolioValue() {
        return totalPortfolioValue;
    }

    public void setTotalPortfolioValue(BigDecimal totalPortfolioValue) {
        this.totalPortfolioValue = totalPortfolioValue;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }

    public BigDecimal getCashWeightPercent() {
        return cashWeightPercent;
    }

    public void setCashWeightPercent(BigDecimal cashWeightPercent) {
        this.cashWeightPercent = cashWeightPercent;
    }

    public BigDecimal getHoldingsWeightPercent() {
        return holdingsWeightPercent;
    }

    public void setHoldingsWeightPercent(BigDecimal holdingsWeightPercent) {
        this.holdingsWeightPercent = holdingsWeightPercent;
    }

    public List<SectorAllocationDTO> getSectorAllocations() {
        return sectorAllocations;
    }

    public void setSectorAllocations(List<SectorAllocationDTO> sectorAllocations) {
        this.sectorAllocations = sectorAllocations != null ? sectorAllocations : new ArrayList<>();
    }

    public List<StockAllocationDTO> getStockAllocations() {
        return stockAllocations;
    }

    public void setStockAllocations(List<StockAllocationDTO> stockAllocations) {
        this.stockAllocations = stockAllocations != null ? stockAllocations : new ArrayList<>();
    }

    public Instant getAsOf() {
        return asOf;
    }

    public void setAsOf(Instant asOf) {
        this.asOf = asOf;
    }
}

