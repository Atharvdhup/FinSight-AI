package com.finsight.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String userId;
    private String name;
    private String currency;
    private BigDecimal cashBalance;
    private BigDecimal totalInvested;
    private BigDecimal totalCurrentValue;
    private BigDecimal totalUnrealizedPnL;
    private BigDecimal totalUnrealizedPnLPercent;
    private List<HoldingDTO> holdings;
    private Instant createdAt;
    private Instant updatedAt;
}
