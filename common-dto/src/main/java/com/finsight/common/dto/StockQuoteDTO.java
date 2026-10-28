package com.finsight.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockQuoteDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String symbol;
    private String companyName;
    private String sector;
    private BigDecimal currentPrice;
    private BigDecimal previousClose;
    private BigDecimal change;
    private BigDecimal changePercent;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private Long volume;
    private String currency;
    private Instant marketTime;
}

