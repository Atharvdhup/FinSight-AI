package com.finsight.portfolio.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.common.enums.TransactionType;
import com.finsight.portfolio.dto.CreatePortfolioRequest;
import com.finsight.portfolio.dto.TradeRequest;
import com.finsight.portfolio.repository.HoldingRepository;
import com.finsight.portfolio.repository.PortfolioRepository;
import com.finsight.portfolio.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("PortfolioController Full Integration Tests")
class PortfolioControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void cleanDatabase() {
        transactionRepository.deleteAll();
        holdingRepository.deleteAll();
        portfolioRepository.deleteAll();
    }

    @Test
    @DisplayName("End-to-End: Create portfolio -> Execute BUY -> Verify Holdings & P&L")
    void shouldExecuteCompleteBuyWorkflow() throws Exception {
        // 1. Create Portfolio via HTTP POST
        CreatePortfolioRequest createReq = new CreatePortfolioRequest("it-user-1", "Integration Portfolio", "USD", new BigDecimal("50000.0000"));

        MvcResult createResult = mockMvc.perform(post("/api/v1/portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name", is("Integration Portfolio")))
                .andExpect(jsonPath("$.cashBalance", is(50000.0000)))
                .andReturn();

        Long portfolioId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();
        assertNotNull(portfolioId);

        // 2. Execute BUY trade for 10 AAPL @ $150.00 (Fees $5.00)
        String idempotencyKey = "it-key-" + UUID.randomUUID();
        TradeRequest buyReq = new TradeRequest("AAPL", TransactionType.BUY, new BigDecimal("10.0000"), new BigDecimal("150.0000"), new BigDecimal("5.0000"));

        mockMvc.perform(post("/api/v1/portfolios/{id}/transactions", portfolioId)
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buyReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("EXECUTED")))
                .andExpect(jsonPath("$.symbol", is("AAPL")))
                .andExpect(jsonPath("$.totalAmount", is(1500.0000)))
                .andExpect(jsonPath("$.fees", is(5.0000)));

        // 3. Verify Portfolio Summary reflects deducted cash and active holding
        mockMvc.perform(get("/api/v1/portfolios/{id}", portfolioId))
                .andExpect(status().isOk())
                // Cash: 50000 - 1505 = 48495.0000
                .andExpect(jsonPath("$.cashBalance", is(48495.0000)))
                .andExpect(jsonPath("$.totalInvested", is(1500.0000)))
                .andExpect(jsonPath("$.holdings", hasSize(1)))
                .andExpect(jsonPath("$.holdings[0].symbol", is("AAPL")))
                .andExpect(jsonPath("$.holdings[0].quantity", is(10.0000)))
                .andExpect(jsonPath("$.holdings[0].averageBuyPrice", is(150.0000)));
    }

    @Test
    @DisplayName("End-to-End Idempotency: Duplicate transaction submission returns HTTP 200 with ALREADY_PROCESSED")
    void shouldHandleDuplicateIdempotentTradeSubmissions() throws Exception {
        // Setup Portfolio
        CreatePortfolioRequest createReq = new CreatePortfolioRequest("it-user-2", "Idempotent Portfolio", "USD", new BigDecimal("20000.0000"));
        MvcResult createResult = mockMvc.perform(post("/api/v1/portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Long portfolioId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        String fixedIdempotencyKey = "fixed-idempotency-key-" + UUID.randomUUID();
        TradeRequest trade = new TradeRequest("MSFT", TransactionType.BUY, new BigDecimal("5.0000"), new BigDecimal("200.0000"), BigDecimal.ZERO);

        // First Submission -> HTTP 201 Created
        mockMvc.perform(post("/api/v1/portfolios/{id}/transactions", portfolioId)
                        .header("Idempotency-Key", fixedIdempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trade)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("EXECUTED")));

        // Duplicate Submission with identical key -> HTTP 200 OK with ALREADY_PROCESSED
        mockMvc.perform(post("/api/v1/portfolios/{id}/transactions", portfolioId)
                        .header("Idempotency-Key", fixedIdempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trade)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ALREADY_PROCESSED")));

        // Assert that exactly ONE transaction was recorded in the database
        assertEquals(1, transactionRepository.findAll().size(), "Database must only contain exactly 1 transaction");
    }

    @Test
    @DisplayName("Error Handling: Negative quantity should trigger Bean Validation failure returning RFC 7807")
    void shouldReturnRfc7807OnValidationFailure() throws Exception {
        // Invalid trade request: negative quantity
        String invalidJson = """
                {
                  "symbol": "AAPL",
                  "type": "BUY",
                  "quantity": -5.00,
                  "price": 150.00
                }
                """;

        mockMvc.perform(post("/api/v1/portfolios/1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type", is("https://finsight.com/errors/validation-failed")))
                .andExpect(jsonPath("$.title", is("Invalid Input Parameters")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors.quantity", containsString("greater than zero")));
    }

    @Test
    @DisplayName("Error Handling: Querying unknown portfolio returns RFC 7807 ProblemDetail 404")
    void shouldReturnRfc7807NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/portfolios/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type", is("https://finsight.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Resource Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", containsString("Portfolio not found with id: 99999")));
    }
}
