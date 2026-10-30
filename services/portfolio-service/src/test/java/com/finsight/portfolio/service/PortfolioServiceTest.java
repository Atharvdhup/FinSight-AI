package com.finsight.portfolio.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioService Business Logic Unit Tests")
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    private Portfolio testPortfolio;

    @BeforeEach
    void setUp() {
        testPortfolio = new Portfolio("user-1", "Test Growth Portfolio", "USD", new BigDecimal("10000.0000"));
        testPortfolio.setId(101L);
    }

    @Test
    @DisplayName("createPortfolio should persist and return mapped DTO")
    void shouldCreatePortfolioSuccessfully() {
        CreatePortfolioRequest request = new CreatePortfolioRequest("user-1", "Test Growth Portfolio", "USD", new BigDecimal("10000.0000"));

        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(invocation -> {
            Portfolio p = invocation.getArgument(0);
            p.setId(101L);
            return p;
        });

        PortfolioDTO response = portfolioService.createPortfolio(request);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("user-1", response.getUserId());
        assertEquals(new BigDecimal("10000.0000"), response.getCashBalance());
        verify(portfolioRepository).save(any(Portfolio.class));
    }

    @Test
    @DisplayName("executeTrade with BUY should deduct cash, create holding, and save transaction")
    void shouldExecuteBuyTradeSuccessfully() {
        TradeRequest trade = new TradeRequest("AAPL", TransactionType.BUY, new BigDecimal("10.0000"), new BigDecimal("150.0000"), new BigDecimal("5.0000"));
        String idempotencyKey = "key-buy-001";

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(portfolioRepository.findById(101L)).thenReturn(Optional.of(testPortfolio));
        when(holdingRepository.findByPortfolioIdAndSymbol(101L, "AAPL")).thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        TransactionResponse response = portfolioService.executeTrade(101L, idempotencyKey, trade);

        assertNotNull(response);
        assertEquals("EXECUTED", response.getStatus());
        assertEquals("AAPL", response.getSymbol());
        assertEquals(TransactionType.BUY, response.getTransactionType());
        assertEquals(new BigDecimal("1500.0000"), response.getTotalAmount());
        // Initial cash: 10000 - (1500 + 5) = 8495.0000
        assertEquals(new BigDecimal("8495.0000"), testPortfolio.getCashBalance());

        verify(holdingRepository).save(any(Holding.class));
        verify(transactionRepository).save(any(Transaction.class));
        verify(portfolioRepository).save(testPortfolio);
    }

    @Test
    @DisplayName("executeTrade with BUY should throw InsufficientFundsException when cash is low")
    void shouldThrowInsufficientFundsOnBuy() {
        testPortfolio.setCashBalance(new BigDecimal("500.0000"));
        TradeRequest trade = new TradeRequest("AAPL", TransactionType.BUY, new BigDecimal("10.0000"), new BigDecimal("150.0000"), new BigDecimal("5.0000"));
        String idempotencyKey = "key-buy-fail";

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(portfolioRepository.findById(101L)).thenReturn(Optional.of(testPortfolio));

        assertThrows(InsufficientFundsException.class, () -> {
            portfolioService.executeTrade(101L, idempotencyKey, trade);
        });

        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(holdingRepository, never()).save(any(Holding.class));
    }

    @Test
    @DisplayName("executeTrade should enforce Idempotency: return existing transaction on duplicate submission")
    void shouldEnforceIdempotencyAndReturnExistingTransaction() {
        String idempotencyKey = "duplicate-key-100";
        Transaction existingTransaction = new Transaction(testPortfolio, idempotencyKey, "AAPL",
                TransactionType.BUY, new BigDecimal("10.0000"), new BigDecimal("150.0000"),
                new BigDecimal("5.0000"), Instant.now());
        existingTransaction.setId(99L);

        TradeRequest trade = new TradeRequest("AAPL", TransactionType.BUY, new BigDecimal("10.0000"), new BigDecimal("150.0000"), new BigDecimal("5.0000"));

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existingTransaction));

        TransactionResponse response = portfolioService.executeTrade(101L, idempotencyKey, trade);

        assertNotNull(response);
        assertEquals(99L, response.getId());
        assertEquals("ALREADY_PROCESSED", response.getStatus());

        // Verify that NO state was mutated and NO new entities were saved
        verify(portfolioRepository, never()).findById(any());
        verify(holdingRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("executeTrade with SELL should add cash and reduce holding quantity")
    void shouldExecuteSellTradeSuccessfully() {
        Holding existingHolding = new Holding(testPortfolio, "AAPL", new BigDecimal("20.0000"),
                new BigDecimal("150.0000"), new BigDecimal("180.0000"), "Technology");

        TradeRequest trade = new TradeRequest("AAPL", TransactionType.SELL, new BigDecimal("10.0000"), new BigDecimal("180.0000"), new BigDecimal("5.0000"));
        String idempotencyKey = "key-sell-001";

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(portfolioRepository.findById(101L)).thenReturn(Optional.of(testPortfolio));
        when(holdingRepository.findByPortfolioIdAndSymbol(101L, "AAPL")).thenReturn(Optional.of(existingHolding));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(2L);
            return t;
        });

        TransactionResponse response = portfolioService.executeTrade(101L, idempotencyKey, trade);

        assertNotNull(response);
        assertEquals("EXECUTED", response.getStatus());
        assertEquals(new BigDecimal("10.0000"), existingHolding.getQuantity(), "Remaining shares should be 10");
        // Cash: 10000 + (1800 - 5) = 11795.0000
        assertEquals(new BigDecimal("11795.0000"), testPortfolio.getCashBalance());

        verify(holdingRepository).save(existingHolding);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("executeTrade with SELL should delete holding when position is fully liquidated")
    void shouldDeleteHoldingOnCompleteLiquidation() {
        Holding existingHolding = new Holding(testPortfolio, "AAPL", new BigDecimal("10.0000"),
                new BigDecimal("150.0000"), new BigDecimal("180.0000"), "Technology");

        // Selling ALL 10 shares
        TradeRequest trade = new TradeRequest("AAPL", TransactionType.SELL, new BigDecimal("10.0000"), new BigDecimal("180.0000"), BigDecimal.ZERO);
        String idempotencyKey = "key-liquidate-001";

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(portfolioRepository.findById(101L)).thenReturn(Optional.of(testPortfolio));
        when(holdingRepository.findByPortfolioIdAndSymbol(101L, "AAPL")).thenReturn(Optional.of(existingHolding));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        portfolioService.executeTrade(101L, idempotencyKey, trade);

        // Verify that holding was deleted from repository
        verify(holdingRepository).delete(existingHolding);
        verify(holdingRepository, never()).save(existingHolding);
    }

    @Test
    @DisplayName("executeTrade with SELL should throw InsufficientSharesException when selling more shares than owned")
    void shouldThrowInsufficientSharesOnOverselling() {
        Holding existingHolding = new Holding(testPortfolio, "AAPL", new BigDecimal("5.0000"),
                new BigDecimal("150.0000"), new BigDecimal("180.0000"), "Technology");

        TradeRequest trade = new TradeRequest("AAPL", TransactionType.SELL, new BigDecimal("10.0000"), new BigDecimal("180.0000"), BigDecimal.ZERO);
        String idempotencyKey = "key-sell-fail";

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(portfolioRepository.findById(101L)).thenReturn(Optional.of(testPortfolio));
        when(holdingRepository.findByPortfolioIdAndSymbol(101L, "AAPL")).thenReturn(Optional.of(existingHolding));

        assertThrows(InsufficientSharesException.class, () -> {
            portfolioService.executeTrade(101L, idempotencyKey, trade);
        });

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("getPortfolioSummary should throw ResourceNotFoundException for unknown ID")
    void shouldThrowResourceNotFoundException() {
        when(portfolioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            portfolioService.getPortfolioSummary(999L);
        });
    }
}

