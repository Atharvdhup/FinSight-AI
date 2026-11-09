package com.finsight.market.controller;

import com.finsight.market.dto.SimulateTickRequest;
import com.finsight.market.dto.SimulateTickResponse;
import com.finsight.market.service.MarketSimulatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/market/simulator")
public class SimulatorController {

    private static final Logger log = LoggerFactory.getLogger(SimulatorController.class);

    private final MarketSimulatorService marketSimulatorService;

    public SimulatorController(MarketSimulatorService marketSimulatorService) {
        this.marketSimulatorService = marketSimulatorService;
    }

    /**
     * Trigger simulated price fluctuation for a specific stock or the entire market.
     */
    @PostMapping("/tick")
    public ResponseEntity<SimulateTickResponse> triggerTick(@RequestBody(required = false) SimulateTickRequest request) {
        log.info("POST /api/v1/market/simulator/tick triggered with request: {}", request);
        SimulateTickResponse response = marketSimulatorService.simulateTick(request != null ? request : new SimulateTickRequest());
        return ResponseEntity.ok(response);
    }

    /**
     * Convenience endpoint to quickly update a single ticker's price via path variable and query params.
     * Example: POST /api/v1/market/simulator/tick/AAPL?price=190.00
     */
    @PostMapping("/tick/{symbol}")
    public ResponseEntity<SimulateTickResponse> triggerStockTick(
            @PathVariable String symbol,
            @RequestParam(required = false) BigDecimal price,
            @RequestParam(required = false) BigDecimal percentChange) {
        log.info("POST /api/v1/market/simulator/tick/{} with price={}, percentChange={}", symbol, price, percentChange);
        SimulateTickRequest request = new SimulateTickRequest(symbol, price, percentChange);
        SimulateTickResponse response = marketSimulatorService.simulateTick(request);
        return ResponseEntity.ok(response);
    }
}

