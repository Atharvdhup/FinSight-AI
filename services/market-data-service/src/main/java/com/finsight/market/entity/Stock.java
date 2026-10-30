package com.finsight.market.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "stocks")
public class Stock implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "symbol", length = 12, nullable = false, updatable = false)
    private String symbol;

    @Column(name = "company_name", length = 150, nullable = false)
    private String companyName;

    @Column(name = "sector", length = 50, nullable = false)
    private String sector;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "USD";

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToOne(mappedBy = "stock", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private StockQuote quote;

    public Stock() {
    }

    public Stock(String symbol, String companyName, String sector, String industry, String currency) {
        this.symbol = symbol != null ? symbol.toUpperCase().trim() : null;
        this.companyName = companyName;
        this.sector = sector;
        this.industry = industry;
        this.currency = currency != null ? currency.toUpperCase().trim() : "USD";
        this.isActive = true;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (symbol != null) {
            symbol = symbol.toUpperCase().trim();
        }
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol != null ? symbol.toUpperCase().trim() : null;
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
        this.currency = currency != null ? currency.toUpperCase().trim() : "USD";
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public StockQuote getQuote() {
        return quote;
    }

    public void setQuote(StockQuote quote) {
        this.quote = quote;
    }
}
