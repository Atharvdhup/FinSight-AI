package com.finsight.market.controller;

import com.finsight.common.dto.StockQuoteDTO;
import com.finsight.market.dto.HistoricalCandleDTO;
import com.finsight.market.dto.StockDetailResponse;
import com.finsight.market.service.MarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/market")
public class MarketController {

    private static final Logger log = LoggerFactory.getLogger(MarketController.class);

    private final MarketDataService marketDataService;

    public MarketController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    /**
     * List all actively traded stocks with their latest quote snapshots.
     */
    @GetMapping("/stocks")
    public ResponseEntity<List<StockDetailResponse>> getAllActiveStocks() {
        log.debug("GET /api/v1/market/stocks - fetching all active securities");
        return ResponseEntity.ok(marketDataService.getAllActiveStocks());
    }

    /**
     * Retrieve ticker details, sector classification, and latest quote snapshot.
     */
    @GetMapping("/stocks/{symbol}")
    public ResponseEntity<StockDetailResponse> getStockBySymbol(@PathVariable String symbol) {
        log.debug("GET /api/v1/market/stocks/{} - fetching stock profile", symbol);
        return ResponseEntity.ok(marketDataService.getStockBySymbol(symbol));
    }

    /**
     * Retrieve daily OHLCV candle historical series.
     * Supports ?days=30 or explicit range ?from=2024-01-01&to=2024-03-31
     */
    @GetMapping("/stocks/{symbol}/history")
    public ResponseEntity<List<HistoricalCandleDTO>> getHistoricalCandles(
            @PathVariable String symbol,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        log.debug("GET /api/v1/market/stocks/{}/history - days={}, from={}, to={}", symbol, days, from, to);
        return ResponseEntity.ok(marketDataService.getHistoricalCandles(symbol, days, from, to));
    }

    /**
     * Standard quote DTO endpoint for internal inter-service communication (e.g. portfolio-service).
     */
    @GetMapping("/quotes/{symbol}")
    public ResponseEntity<StockQuoteDTO> getStockQuote(@PathVariable String symbol) {
        log.debug("GET /api/v1/market/quotes/{} - fetching standard quote DTO", symbol);
        return ResponseEntity.ok(marketDataService.getStockQuoteDTO(symbol));
    }
}
