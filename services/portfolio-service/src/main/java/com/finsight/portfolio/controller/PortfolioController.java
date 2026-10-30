package com.finsight.portfolio.controller;

import com.finsight.common.dto.HoldingDTO;
import com.finsight.common.dto.PortfolioDTO;
import com.finsight.portfolio.dto.CreatePortfolioRequest;
import com.finsight.portfolio.dto.TradeRequest;
import com.finsight.portfolio.dto.TransactionResponse;
import com.finsight.portfolio.service.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller exposing virtual portfolio management and trade execution APIs.
 */
@RestController
@RequestMapping("/api/v1/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    /**
     * Create a new portfolio.
     */
    @PostMapping
    public ResponseEntity<PortfolioDTO> createPortfolio(@Valid @RequestBody CreatePortfolioRequest request) {
        PortfolioDTO created = portfolioService.createPortfolio(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get a portfolio summary by ID including holdings, cash, and P&L metrics.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PortfolioDTO> getPortfolio(@PathVariable Long id) {
        PortfolioDTO portfolio = portfolioService.getPortfolioSummary(id);
        return ResponseEntity.ok(portfolio);
    }

    /**
     * Get all portfolios for a user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PortfolioDTO>> getPortfoliosByUser(@PathVariable String userId) {
        List<PortfolioDTO> portfolios = portfolioService.getPortfoliosByUserId(userId);
        return ResponseEntity.ok(portfolios);
    }

    /**
     * Get active stock holdings with portfolio weights.
     */
    @GetMapping("/{id}/holdings")
    public ResponseEntity<List<HoldingDTO>> getHoldings(@PathVariable Long id) {
        List<HoldingDTO> holdings = portfolioService.getHoldings(id);
        return ResponseEntity.ok(holdings);
    }

    /**
     * Get paginated transaction trade history for a portfolio (newest first).
     */
    @GetMapping("/{id}/transactions")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("executedAt").descending());
        Page<TransactionResponse> transactions = portfolioService.getTransactions(id, pageRequest);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Execute a BUY or SELL trade transaction.
     * Enforces Idempotency-Key header to guarantee at-most-once order execution.
     */
    @PostMapping("/{id}/transactions")
    public ResponseEntity<TransactionResponse> executeTrade(
            @PathVariable Long id,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody TradeRequest tradeRequest) {

        // If client does not provide an idempotency key, generate a fallback UUID
        String effectiveKey = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? idempotencyKey.trim()
                : UUID.randomUUID().toString();

        TransactionResponse response = portfolioService.executeTrade(id, effectiveKey, tradeRequest);

        HttpStatus status = "ALREADY_PROCESSED".equals(response.getStatus())
                ? HttpStatus.OK
                : HttpStatus.CREATED;

        return ResponseEntity.status(status).body(response);
    }
}
