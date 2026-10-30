package com.finsight.portfolio.service;

import com.finsight.common.dto.HoldingDTO;
import com.finsight.common.dto.PortfolioDTO;
import com.finsight.common.enums.TransactionType;
import com.finsight.portfolio.dto.CreatePortfolioRequest;
import com.finsight.portfolio.dto.TradeRequest;
import com.finsight.portfolio.dto.TransactionResponse;
import com.finsight.portfolio.entity.Holding;
import com.finsight.portfolio.entity.Portfolio;
import com.finsight.portfolio.entity.Transaction;
import com.finsight.portfolio.exception.InsufficientFundsException;
import com.finsight.portfolio.exception.InsufficientSharesException;
import com.finsight.portfolio.exception.ResourceNotFoundException;
import com.finsight.portfolio.repository.HoldingRepository;
import com.finsight.portfolio.repository.PortfolioRepository;
import com.finsight.portfolio.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Core business service for Portfolio management and trade execution.
 * Enforces transaction atomicity, idempotency checks, and deterministic BigDecimal precision.
 */
@Service
public class PortfolioService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioService.class);

    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final TransactionRepository transactionRepository;

    public PortfolioService(PortfolioRepository portfolioRepository,
                            HoldingRepository holdingRepository,
                            TransactionRepository transactionRepository) {
        this.portfolioRepository = portfolioRepository;
        this.holdingRepository = holdingRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Creates a new investment portfolio for a user.
     */
    @Transactional
    public PortfolioDTO createPortfolio(CreatePortfolioRequest request) {
        log.info("Creating new portfolio for user: {}, name: {}", request.getUserId(), request.getName());

        Portfolio portfolio = new Portfolio(
                request.getUserId(),
                request.getName(),
                request.getCurrency(),
                request.getInitialCash()
        );

        Portfolio saved = portfolioRepository.save(portfolio);
        return mapToPortfolioDTO(saved, new ArrayList<>());
    }

    /**
     * Retrieves portfolio summary including all holdings, cash, and performance calculations.
     */
    @Transactional(readOnly = true)
    public PortfolioDTO getPortfolioSummary(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + portfolioId));

        List<Holding> holdings = holdingRepository.findByPortfolioId(portfolioId);
        return mapToPortfolioDTO(portfolio, holdings);
    }

    /**
     * Retrieves all portfolios belonging to a specific user.
     */
    @Transactional(readOnly = true)
    public List<PortfolioDTO> getPortfoliosByUserId(String userId) {
        List<Portfolio> portfolios = portfolioRepository.findByUserId(userId);
        List<PortfolioDTO> dtos = new ArrayList<>();
        for (Portfolio p : portfolios) {
            List<Holding> holdings = holdingRepository.findByPortfolioId(p.getId());
            dtos.add(mapToPortfolioDTO(p, holdings));
        }
        return dtos;
    }

    /**
     * Retrieves holdings for a portfolio with portfolio percentage weights.
     */
    @Transactional(readOnly = true)
    public List<HoldingDTO> getHoldings(Long portfolioId) {
        if (!portfolioRepository.existsById(portfolioId)) {
            throw new ResourceNotFoundException("Portfolio not found with id: " + portfolioId);
        }

        List<Holding> holdings = holdingRepository.findByPortfolioId(portfolioId);
        BigDecimal totalMarketValue = holdings.stream()
                .map(Holding::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return holdings.stream()
                .map(h -> mapToHoldingDTO(h, totalMarketValue))
                .toList();
    }

    /**
     * Retrieves paginated transaction history for a portfolio.
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(Long portfolioId, Pageable pageable) {
        if (!portfolioRepository.existsById(portfolioId)) {
            throw new ResourceNotFoundException("Portfolio not found with id: " + portfolioId);
        }

        Page<Transaction> transactions = transactionRepository.findByPortfolioIdOrderByExecutedAtDesc(portfolioId, pageable);
        return transactions.map(t -> mapToTransactionResponse(t, "EXECUTED"));
    }

    /**
     * Executes a BUY or SELL trade transaction.
     * Enforces Idempotency to prevent duplicate trade executions and double-spending.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse executeTrade(Long portfolioId, String idempotencyKey, TradeRequest tradeRequest) {
        // 1. Enforce Idempotency Check
        Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.warn("Idempotency key match: trade with key {} was already executed. Returning existing transaction.", idempotencyKey);
            return mapToTransactionResponse(existing.get(), "ALREADY_PROCESSED");
        }

        // 2. Fetch Portfolio
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + portfolioId));

        String symbol = tradeRequest.getSymbol().toUpperCase().trim();
        BigDecimal quantity = tradeRequest.getQuantity().setScale(4, RoundingMode.HALF_EVEN);
        BigDecimal price = tradeRequest.getPrice().setScale(4, RoundingMode.HALF_EVEN);
        BigDecimal fees = tradeRequest.getFees() != null ? tradeRequest.getFees().setScale(4, RoundingMode.HALF_EVEN) : BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);
        BigDecimal grossAmount = quantity.multiply(price).setScale(4, RoundingMode.HALF_EVEN);

        Holding holding = holdingRepository.findByPortfolioIdAndSymbol(portfolioId, symbol).orElse(null);

        // 3. Process BUY vs SELL
        if (tradeRequest.getType() == TransactionType.BUY) {
            BigDecimal totalRequired = grossAmount.add(fees);
            if (portfolio.getCashBalance().compareTo(totalRequired) < 0) {
                throw new InsufficientFundsException("Insufficient cash balance to execute BUY order. Required: "
                        + totalRequired + ", Available: " + portfolio.getCashBalance());
            }

            // Deduct cash
            portfolio.setCashBalance(portfolio.getCashBalance().subtract(totalRequired).setScale(4, RoundingMode.HALF_EVEN));

            if (holding == null) {
                holding = new Holding(portfolio, symbol, quantity, price, price, "Technology");
            } else {
                holding.applyBuy(quantity, price);
            }
            holdingRepository.save(holding);

        } else if (tradeRequest.getType() == TransactionType.SELL) {
            if (holding == null || holding.getQuantity().compareTo(quantity) < 0) {
                BigDecimal owned = holding != null ? holding.getQuantity() : BigDecimal.ZERO;
                throw new InsufficientSharesException("Cannot execute SELL order. Requested: "
                        + quantity + " shares of " + symbol + ", but portfolio only owns: " + owned);
            }

            BigDecimal netProceeds = grossAmount.subtract(fees);

            // Add net proceeds to cash
            portfolio.setCashBalance(portfolio.getCashBalance().add(netProceeds).setScale(4, RoundingMode.HALF_EVEN));

            holding.applySell(quantity);
            if (holding.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
                // If position fully liquidated, remove holding or retain at zero quantity
                holdingRepository.delete(holding);
            } else {
                holdingRepository.save(holding);
            }
        } else {
            throw new IllegalArgumentException("Unsupported transaction type: " + tradeRequest.getType());
        }

        portfolioRepository.save(portfolio);

        // 4. Record Immutable Transaction in Ledger
        Transaction transaction = new Transaction(
                portfolio,
                idempotencyKey,
                symbol,
                tradeRequest.getType(),
                quantity,
                price,
                fees,
                Instant.now()
        );
        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Successfully executed {} trade for portfolio: {}, symbol: {}, shares: {}, price: {}",
                tradeRequest.getType(), portfolioId, symbol, quantity, price);

        return mapToTransactionResponse(savedTransaction, "EXECUTED");
    }

    // Helper Mappers
    private PortfolioDTO mapToPortfolioDTO(Portfolio portfolio, List<Holding> holdings) {
        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalCurrentValue = BigDecimal.ZERO;

        for (Holding h : holdings) {
            totalInvested = totalInvested.add(h.getTotalInvested());
            totalCurrentValue = totalCurrentValue.add(h.getCurrentValue());
        }

        BigDecimal totalUnrealizedPnL = totalCurrentValue.subtract(totalInvested).setScale(4, RoundingMode.HALF_EVEN);
        BigDecimal totalUnrealizedPnLPercent = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);

        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            totalUnrealizedPnLPercent = totalUnrealizedPnL
                    .divide(totalInvested, 6, RoundingMode.HALF_EVEN)
                    .multiply(new BigDecimal("100"))
                    .setScale(4, RoundingMode.HALF_EVEN);
        }

        final BigDecimal finalTotalValue = totalCurrentValue;
        List<HoldingDTO> holdingDTOs = holdings.stream()
                .map(h -> mapToHoldingDTO(h, finalTotalValue))
                .toList();

        return PortfolioDTO.builder()
                .id(portfolio.getId())
                .userId(portfolio.getUserId())
                .name(portfolio.getName())
                .currency(portfolio.getCurrency())
                .cashBalance(portfolio.getCashBalance())
                .totalInvested(totalInvested.setScale(4, RoundingMode.HALF_EVEN))
                .totalCurrentValue(totalCurrentValue.setScale(4, RoundingMode.HALF_EVEN))
                .totalUnrealizedPnL(totalUnrealizedPnL)
                .totalUnrealizedPnLPercent(totalUnrealizedPnLPercent)
                .holdings(holdingDTOs)
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }

    private HoldingDTO mapToHoldingDTO(Holding holding, BigDecimal totalPortfolioMarketValue) {
        BigDecimal weight = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);
        if (totalPortfolioMarketValue != null && totalPortfolioMarketValue.compareTo(BigDecimal.ZERO) > 0) {
            weight = holding.getCurrentValue()
                    .divide(totalPortfolioMarketValue, 6, RoundingMode.HALF_EVEN)
                    .multiply(new BigDecimal("100"))
                    .setScale(4, RoundingMode.HALF_EVEN);
        }

        return HoldingDTO.builder()
                .id(holding.getId())
                .portfolioId(holding.getPortfolio() != null ? holding.getPortfolio().getId() : null)
                .symbol(holding.getSymbol())
                .quantity(holding.getQuantity())
                .averageBuyPrice(holding.getAverageBuyPrice())
                .currentPrice(holding.getCurrentPrice())
                .totalInvested(holding.getTotalInvested())
                .currentValue(holding.getCurrentValue())
                .unrealizedPnL(holding.getUnrealizedPnL())
                .unrealizedPnLPercent(holding.getUnrealizedPnLPercent())
                .portfolioWeightPercent(weight)
                .sector(holding.getSector())
                .updatedAt(holding.getUpdatedAt())
                .build();
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction, String status) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .portfolioId(transaction.getPortfolio() != null ? transaction.getPortfolio().getId() : null)
                .idempotencyKey(transaction.getIdempotencyKey())
                .symbol(transaction.getSymbol())
                .transactionType(transaction.getTransactionType())
                .quantity(transaction.getQuantity())
                .price(transaction.getPrice())
                .totalAmount(transaction.getTotalAmount())
                .fees(transaction.getFees())
                .executedAt(transaction.getExecutedAt())
                .status(status)
                .build();
    }
}
