package com.finsight.market.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("MarketController Full Integration Tests")
class MarketControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/market/stocks should return all seeded securities")
    void shouldReturnAllSeededStocks() throws Exception {
        mockMvc.perform(get("/api/v1/market/stocks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(8))))
                .andExpect(jsonPath("$[0].symbol").exists())
                .andExpect(jsonPath("$[0].currentPrice").isNumber())
                .andExpect(jsonPath("$[0].sector").isString());
    }

    @Test
    @DisplayName("GET /api/v1/market/stocks/AAPL should return Apple profile and quote")
    void shouldReturnAppleProfile() throws Exception {
        mockMvc.perform(get("/api/v1/market/stocks/AAPL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol", is("AAPL")))
                .andExpect(jsonPath("$.companyName", is("Apple Inc.")))
                .andExpect(jsonPath("$.sector", is("Technology")))
                .andExpect(jsonPath("$.currency", is("USD")))
                .andExpect(jsonPath("$.currentPrice").isNumber())
                .andExpect(jsonPath("$.previousClose").isNumber());
    }

    @Test
    @DisplayName("GET /api/v1/market/stocks/AAPL/history?days=30 should return daily candles")
    void shouldReturnHistoricalCandles() throws Exception {
        mockMvc.perform(get("/api/v1/market/stocks/AAPL/history")
                        .param("days", "30")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].symbol", is("AAPL")))
                .andExpect(jsonPath("$[0].priceDate").exists())
                .andExpect(jsonPath("$[0].openPrice").isNumber())
                .andExpect(jsonPath("$[0].highPrice").isNumber())
                .andExpect(jsonPath("$[0].lowPrice").isNumber())
                .andExpect(jsonPath("$[0].closePrice").isNumber())
                .andExpect(jsonPath("$[0].volume").isNumber());
    }

    @Test
    @DisplayName("GET /api/v1/market/stocks/NONEXISTENT should return 404 ProblemDetail")
    void shouldReturn404OnUnknownStock() throws Exception {
        mockMvc.perform(get("/api/v1/market/stocks/NONEXISTENT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type", is("https://finsight.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Resource Not Found")))
                .andExpect(jsonPath("$.status", is(404)));
    }
}

