package com.finsight.portfolio.dto;

import com.finsight.common.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;

public class TradeRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Stock symbol cannot be blank")
    @Size(min = 1, max = 12, message = "Symbol must be between 1 and 12 characters")
    private String symbol;

    @NotNull(message = "Transaction type is required (BUY or SELL)")
    private TransactionType type;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0001", message = "Quantity must be strictly greater than zero")
    private BigDecimal quantity;

    @NotNull(message = "Execution price is required")
    @DecimalMin(value = "0.0001", message = "Execution price must be strictly greater than zero")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Fees cannot be negative")
    private BigDecimal fees = BigDecimal.ZERO;

    public TradeRequest() {
    }

    public TradeRequest(String symbol, TransactionType type, BigDecimal quantity, BigDecimal price, BigDecimal fees) {
        this.symbol = symbol != null ? symbol.toUpperCase().trim() : null;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.fees = fees != null ? fees : BigDecimal.ZERO;
    }

    // Getters and Setters
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol != null ? symbol.toUpperCase().trim() : null;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getFees() {
        return fees;
    }

    public void setFees(BigDecimal fees) {
        this.fees = fees != null ? fees : BigDecimal.ZERO;
    }
}
