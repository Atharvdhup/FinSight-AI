package com.finsight.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a position/holding within a user's portfolio.
 * Maintains quantity, cost basis (average buy price), market valuation, and P&L.
 */
@Entity
@Table(name = "holdings", uniqueConstraints = {
        @UniqueConstraint(name = "uq_portfolio_symbol", columnNames = {"portfolio_id", "symbol"})
})
public class Holding implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(name = "symbol", nullable = false, length = 12)
    private String symbol;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);

    @Column(name = "average_buy_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal averageBuyPrice = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);

    @Column(name = "current_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentPrice = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);

    @Column(name = "total_invested", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalInvested = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);

    @Column(name = "current_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentValue = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);

    @Column(name = "unrealized_pnl", nullable = false, precision = 19, scale = 4)
    private BigDecimal unrealizedPnL = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);

    @Column(name = "unrealized_pnl_pct", nullable = false, precision = 8, scale = 4)
    private BigDecimal unrealizedPnLPercent = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);

    @Column(name = "sector", length = 50)
    private String sector;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Holding() {
    }

    public Holding(Portfolio portfolio, String symbol, BigDecimal quantity, BigDecimal averageBuyPrice, BigDecimal currentPrice, String sector) {
        this.portfolio = portfolio;
        this.symbol = symbol != null ? symbol.toUpperCase().trim() : null;
        this.quantity = quantity != null ? quantity.setScale(4, RoundingMode.HALF_EVEN) : BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);
        this.averageBuyPrice = averageBuyPrice != null ? averageBuyPrice.setScale(4, RoundingMode.HALF_EVEN) : BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);
        this.currentPrice = currentPrice != null ? currentPrice.setScale(4, RoundingMode.HALF_EVEN) : this.averageBuyPrice;
        this.sector = sector;
        recalculateMetrics();
    }

    @PrePersist
    @PreUpdate
    protected void onPersistOrUpdate() {
        this.updatedAt = Instant.now();
        recalculateMetrics();
    }

    /**
     * Deterministic financial metrics recalculation using RoundingMode.HALF_EVEN (Banker's rounding).
     * Avoids IEEE-754 binary floating-point drift.
     */
    public void recalculateMetrics() {
        if (this.quantity == null) {
            this.quantity = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);
        }
        if (this.averageBuyPrice == null) {
            this.averageBuyPrice = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);
        }
        if (this.currentPrice == null) {
            this.currentPrice = this.averageBuyPrice;
        }

        // totalInvested = quantity * averageBuyPrice
        this.totalInvested = this.quantity.multiply(this.averageBuyPrice).setScale(4, RoundingMode.HALF_EVEN);

        // currentValue = quantity * currentPrice
        this.currentValue = this.quantity.multiply(this.currentPrice).setScale(4, RoundingMode.HALF_EVEN);

        // unrealizedPnL = currentValue - totalInvested
        this.unrealizedPnL = this.currentValue.subtract(this.totalInvested).setScale(4, RoundingMode.HALF_EVEN);

        // unrealizedPnLPercent = (unrealizedPnL / totalInvested) * 100
        if (this.totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            this.unrealizedPnLPercent = this.unrealizedPnL
                    .divide(this.totalInvested, 6, RoundingMode.HALF_EVEN)
                    .multiply(new BigDecimal("100"))
                    .setScale(4, RoundingMode.HALF_EVEN);
        } else {
            this.unrealizedPnLPercent = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);
        }
    }

    /**
     * Applies a BUY order using the Weighted-Average Cost Basis formula:
     * New Avg Price = ((Old Qty * Old Avg) + (Buy Qty * Buy Price)) / (Old Qty + Buy Qty)
     */
    public void applyBuy(BigDecimal buyQuantity, BigDecimal buyPrice) {
        BigDecimal newQuantity = this.quantity.add(buyQuantity);
        if (newQuantity.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal currentTotalCost = this.quantity.multiply(this.averageBuyPrice);
            BigDecimal addedCost = buyQuantity.multiply(buyPrice);
            BigDecimal newTotalCost = currentTotalCost.add(addedCost);
            this.averageBuyPrice = newTotalCost.divide(newQuantity, 4, RoundingMode.HALF_EVEN);
            this.quantity = newQuantity.setScale(4, RoundingMode.HALF_EVEN);
            this.currentPrice = buyPrice.setScale(4, RoundingMode.HALF_EVEN);
            recalculateMetrics();
        }
    }

    /**
     * Applies a SELL order (reduces quantity, cost basis per remaining share remains constant).
     */
    public void applySell(BigDecimal sellQuantity) {
        if (sellQuantity.compareTo(this.quantity) > 0) {
            throw new IllegalArgumentException("Cannot sell more shares than currently owned. Owned: " 
                    + this.quantity + ", Attempted: " + sellQuantity);
        }
        this.quantity = this.quantity.subtract(sellQuantity).setScale(4, RoundingMode.HALF_EVEN);
        recalculateMetrics();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol != null ? symbol.toUpperCase().trim() : null;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity != null ? quantity.setScale(4, RoundingMode.HALF_EVEN) : BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);
        recalculateMetrics();
    }

    public BigDecimal getAverageBuyPrice() {
        return averageBuyPrice;
    }

    public void setAverageBuyPrice(BigDecimal averageBuyPrice) {
        this.averageBuyPrice = averageBuyPrice != null ? averageBuyPrice.setScale(4, RoundingMode.HALF_EVEN) : BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);
        recalculateMetrics();
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice != null ? currentPrice.setScale(4, RoundingMode.HALF_EVEN) : BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);
        recalculateMetrics();
    }

    public BigDecimal getTotalInvested() {
        return totalInvested;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public BigDecimal getUnrealizedPnL() {
        return unrealizedPnL;
    }

    public BigDecimal getUnrealizedPnLPercent() {
        return unrealizedPnLPercent;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Holding holding = (Holding) o;
        return Objects.equals(id, holding.id) ||
                (Objects.equals(symbol, holding.symbol) && portfolio != null && holding.portfolio != null && Objects.equals(portfolio.getId(), holding.portfolio.getId()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, symbol);
    }

    @Override
    public String toString() {
        return "Holding{" +
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", quantity=" + quantity +
                ", averageBuyPrice=" + averageBuyPrice +
                ", currentPrice=" + currentPrice +
                ", totalInvested=" + totalInvested +
                ", currentValue=" + currentValue +
                ", unrealizedPnL=" + unrealizedPnL +
                ", unrealizedPnLPercent=" + unrealizedPnLPercent +
                ", sector='" + sector + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
