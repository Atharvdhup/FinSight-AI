package com.finsight.portfolio.service;

import com.finsight.common.dto.StockQuoteDTO;
import com.finsight.portfolio.client.MarketDataClient;
import com.finsight.portfolio.dto.PortfolioAllocationResponse;
import com.finsight.portfolio.dto.PortfolioPerformanceDTO;
import com.finsight.portfolio.dto.SectorAllocationDTO;
import com.finsight.portfolio.dto.StockAllocationDTO;
import com.finsight.portfolio.entity.Holding;
import com.finsight.portfolio.entity.Portfolio;
import com.finsight.portfolio.exception.ResourceNotFoundException;
import com.finsight.portfolio.repository.HoldingRepository;
import com.finsight.portfolio.repository.PortfolioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PortfolioValuationService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioValuationService.class);
    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final MarketDataClient marketDataClient;

    public PortfolioValuationService(PortfolioRepository portfolioRepository,
                                     HoldingRepository holdingRepository,
                                     MarketDataClient marketDataClient) {
        this.portfolioRepository = portfolioRepository;
        this.holdingRepository = holdingRepository;
        this.marketDataClient = marketDataClient;
    }

    /**
     * Calculate comprehensive real-time performance breakdown for a portfolio.
     */
    public PortfolioPerformanceDTO calculatePerformance(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + portfolioId));

        List<Holding> holdings = holdingRepository.findByPortfolioId(portfolioId);

        BigDecimal totalInvested = BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        BigDecimal totalCurrentHoldingsValue = BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        BigDecimal dailyChangeAmount = BigDecimal.ZERO.setScale(SCALE, ROUNDING);

        for (Holding holding : holdings) {
            BigDecimal invested = holding.getQuantity().multiply(holding.getAverageBuyPrice()).setScale(SCALE, ROUNDING);
            totalInvested = totalInvested.add(invested);

            // Fetch live quote with graceful fallback
            Optional<StockQuoteDTO> liveQuoteOpt = marketDataClient.getLiveQuote(holding.getSymbol());
            BigDecimal currentPrice;
            BigDecimal quoteDailyChange = BigDecimal.ZERO.setScale(SCALE, ROUNDING);

            if (liveQuoteOpt.isPresent()) {
                StockQuoteDTO quote = liveQuoteOpt.get();
                currentPrice = quote.getCurrentPrice() != null ? quote.getCurrentPrice() : holding.getCurrentPrice();
                if (quote.getChange() != null) {
                    quoteDailyChange = quote.getChange();
                }
            } else {
                currentPrice = holding.getCurrentPrice() != null ? holding.getCurrentPrice() : holding.getAverageBuyPrice();
            }

            BigDecimal holdingValue = holding.getQuantity().multiply(currentPrice).setScale(SCALE, ROUNDING);
            totalCurrentHoldingsValue = totalCurrentHoldingsValue.add(holdingValue);

            BigDecimal holdingDailyChange = holding.getQuantity().multiply(quoteDailyChange).setScale(SCALE, ROUNDING);
            dailyChangeAmount = dailyChangeAmount.add(holdingDailyChange);
        }

        BigDecimal cashBalance = portfolio.getCashBalance() != null ? portfolio.getCashBalance() : BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        BigDecimal totalPortfolioValue = cashBalance.add(totalCurrentHoldingsValue).setScale(SCALE, ROUNDING);
        BigDecimal totalUnrealizedPnL = totalCurrentHoldingsValue.subtract(totalInvested).setScale(SCALE, ROUNDING);

        BigDecimal totalUnrealizedPnLPercent = BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            totalUnrealizedPnLPercent = totalUnrealizedPnL.divide(totalInvested, 6, ROUNDING)
                    .multiply(HUNDRED)
                    .setScale(SCALE, ROUNDING);
        }

        BigDecimal dailyChangePercent = BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        BigDecimal previousHoldingsValue = totalCurrentHoldingsValue.subtract(dailyChangeAmount);
        if (previousHoldingsValue.compareTo(BigDecimal.ZERO) > 0) {
            dailyChangePercent = dailyChangeAmount.divide(previousHoldingsValue, 6, ROUNDING)
                    .multiply(HUNDRED)
                    .setScale(SCALE, ROUNDING);
        }

        return PortfolioPerformanceDTO.builder()
                .portfolioId(portfolio.getId())
                .portfolioName(portfolio.getName())
                .currency(portfolio.getCurrency())
                .cashBalance(cashBalance)
                .totalInvested(totalInvested)
                .totalCurrentValue(totalCurrentHoldingsValue)
                .totalPortfolioValue(totalPortfolioValue)
                .totalUnrealizedPnL(totalUnrealizedPnL)
                .totalUnrealizedPnLPercent(totalUnrealizedPnLPercent)
                .dailyChangeAmount(dailyChangeAmount)
                .dailyChangePercent(dailyChangePercent)
                .asOf(Instant.now())
                .build();
    }

    /**
     * Calculate asset and sector allocation breakdown for a portfolio.
     */
    public PortfolioAllocationResponse calculateAllocation(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + portfolioId));

        List<Holding> holdings = holdingRepository.findByPortfolioId(portfolioId);
        BigDecimal cashBalance = portfolio.getCashBalance() != null ? portfolio.getCashBalance() : BigDecimal.ZERO.setScale(SCALE, ROUNDING);

        BigDecimal totalHoldingsValue = BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        List<StockAllocationDTO> stockAllocations = new ArrayList<>();
        Map<String, BigDecimal> sectorValueMap = new HashMap<>();

        // First pass: Calculate holding values and aggregate by sector
        for (Holding holding : holdings) {
            Optional<StockQuoteDTO> liveQuoteOpt = marketDataClient.getLiveQuote(holding.getSymbol());
            BigDecimal currentPrice = liveQuoteOpt.map(StockQuoteDTO::getCurrentPrice)
                    .orElse(holding.getCurrentPrice() != null ? holding.getCurrentPrice() : holding.getAverageBuyPrice());

            BigDecimal holdingValue = holding.getQuantity().multiply(currentPrice).setScale(SCALE, ROUNDING);
            totalHoldingsValue = totalHoldingsValue.add(holdingValue);

            String sector = holding.getSector() != null ? holding.getSector() : "Other";
            sectorValueMap.put(sector, sectorValueMap.getOrDefault(sector, BigDecimal.ZERO).add(holdingValue));

            StockAllocationDTO stockAlloc = new StockAllocationDTO(
                    holding.getSymbol(),
                    sector,
                    holding.getQuantity(),
                    currentPrice,
                    holdingValue,
                    BigDecimal.ZERO
            );
            stockAllocations.add(stockAlloc);
        }

        BigDecimal totalPortfolioValue = cashBalance.add(totalHoldingsValue).setScale(SCALE, ROUNDING);

        BigDecimal cashWeightPercent = BigDecimal.ZERO.setScale(2, ROUNDING);
        BigDecimal holdingsWeightPercent = BigDecimal.ZERO.setScale(2, ROUNDING);
        List<SectorAllocationDTO> sectorAllocations = new ArrayList<>();

        if (totalPortfolioValue.compareTo(BigDecimal.ZERO) > 0) {
            cashWeightPercent = cashBalance.divide(totalPortfolioValue, 4, ROUNDING)
                    .multiply(HUNDRED)
                    .setScale(2, ROUNDING);

            holdingsWeightPercent = totalHoldingsValue.divide(totalPortfolioValue, 4, ROUNDING)
                    .multiply(HUNDRED)
                    .setScale(2, ROUNDING);

            // Calculate stock weights
            for (StockAllocationDTO stockAlloc : stockAllocations) {
                BigDecimal weight = stockAlloc.getCurrentValue().divide(totalPortfolioValue, 4, ROUNDING)
                        .multiply(HUNDRED)
                        .setScale(2, ROUNDING);
                stockAlloc.setWeightPercent(weight);
            }

            // Calculate sector weights
            for (Map.Entry<String, BigDecimal> entry : sectorValueMap.entrySet()) {
                BigDecimal sectorWeight = entry.getValue().divide(totalPortfolioValue, 4, ROUNDING)
                        .multiply(HUNDRED)
                        .setScale(2, ROUNDING);
                sectorAllocations.add(new SectorAllocationDTO(entry.getKey(), entry.getValue().setScale(SCALE, ROUNDING), sectorWeight));
            }
        }

        return new PortfolioAllocationResponse(
                portfolio.getId(),
                totalPortfolioValue,
                cashBalance,
                cashWeightPercent,
                holdingsWeightPercent,
                sectorAllocations,
                stockAllocations,
                Instant.now()
        );
    }
}

