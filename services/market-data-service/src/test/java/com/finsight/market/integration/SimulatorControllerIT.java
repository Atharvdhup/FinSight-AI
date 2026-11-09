package com.finsight.market.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.market.dto.SimulateTickRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("SimulatorController Integration Tests")
class SimulatorControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    @DisplayName("POST /api/v1/market/simulator/tick should update quote for specific stock")
    void shouldSimulateStockTick() throws Exception {
        SimulateTickRequest request = new SimulateTickRequest("AAPL", new BigDecimal("195.5000"), null);

        mockMvc.perform(post("/api/v1/market/simulator/tick")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedCount", is(1)))
                .andExpect(jsonPath("$.updatedQuotes[0].symbol", is("AAPL")))
                .andExpect(jsonPath("$.updatedQuotes[0].currentPrice", is(195.5000)));
    }

    @Test
    @DisplayName("POST /api/v1/market/simulator/tick/AAPL?price=190.00 should update quote via path param")
    void shouldSimulateStockTickViaPathParam() throws Exception {
        mockMvc.perform(post("/api/v1/market/simulator/tick/AAPL")
                        .param("price", "190.0000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedCount", is(1)))
                .andExpect(jsonPath("$.updatedQuotes[0].symbol", is("AAPL")))
                .andExpect(jsonPath("$.updatedQuotes[0].currentPrice", is(190.0000)));
    }

    @Test
    @DisplayName("POST /api/v1/market/simulator/tick with empty body should update all active stocks")
    void shouldSimulateMarketWideTick() throws Exception {
        mockMvc.perform(post("/api/v1/market/simulator/tick")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedCount", greaterThanOrEqualTo(8)))
                .andExpect(jsonPath("$.updatedQuotes", hasSize(greaterThanOrEqualTo(8))));
    }
}