package com.finsight.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;

public class CreatePortfolioRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "User ID cannot be blank")
    @Size(max = 64, message = "User ID cannot exceed 64 characters")
    private String userId;

    @NotBlank(message = "Portfolio name cannot be blank")
    @Size(max = 100, message = "Portfolio name cannot exceed 100 characters")
    private String name;

    @Size(min = 3, max = 3, message = "Currency must be a 3-character ISO code (e.g. USD, INR, EUR)")
    private String currency = "USD";

    @PositiveOrZero(message = "Initial cash balance must be zero or positive")
    private BigDecimal initialCash = new BigDecimal("100000.0000");

    public CreatePortfolioRequest() {
    }

    public CreatePortfolioRequest(String userId, String name, String currency, BigDecimal initialCash) {
        this.userId = userId;
        this.name = name;
        this.currency = (currency != null && !currency.isBlank()) ? currency.toUpperCase().trim() : "USD";
        this.initialCash = initialCash != null ? initialCash : new BigDecimal("100000.0000");
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = (currency != null && !currency.isBlank()) ? currency.toUpperCase().trim() : "USD";
    }

    public BigDecimal getInitialCash() {
        return initialCash;
    }

    public void setInitialCash(BigDecimal initialCash) {
        this.initialCash = initialCash;
    }
}
