# FinSight AI — 21-Day Step-by-Step Implementation Roadmap

This roadmap is an exhaustive, production-grade guide designed for pair-programming and incremental daily development. Each day has a clear objective, actionable checklist, file targets, verification commands, and interview defense talking points.

---

## Roadmap Overview at a Glance

| Week | Focus | Key Deliverables |
|---|---|---|
| **Week 1 (Days 1–7)** | **Deterministic Financial Core & React Dashboard** | Multi-service repo, PostgreSQL, Portfolio Service with `BigDecimal` math, Market Data Service, Portfolio Valuation, and React Dashboard. |
| **Week 2 (Days 8–14)** | **Gateway, Kafka Event Streaming & AI Engine** | Spring Cloud Gateway, JWT Security, Kafka Producer/Consumer pipeline, Alert Service, DLT & Retries, PostgreSQL TSVECTOR RAG, and AI Tool-Calling Agent. |
| **Week 3 (Days 15–21)** | **Distributed Patterns, Observability, CI/CD & Cloud** | Transactional Outbox Pattern, AI Chat & Event Monitoring UI, Docker Compose orchestration, GitHub Actions CI/CD, and AWS ECS/RDS deployment specs. |

---

# Week 1: Deterministic Financial Core & Interactive Dashboard

---

### Day 1: Architecture Setup, Repository Structure & Database Infrastructure
- **Objective:** Establish the multi-module Maven/Gradle project, Dockerized PostgreSQL 16 databases, and baseline project structure.
- **Checklist:**
  1. Initialize root multi-module Spring Boot project with root `pom.xml` / `build.gradle` (modules: `common-dto`, `gateway-service`, `portfolio-service`, `market-data-service`, `alert-service`, `research-service`).
  2. Setup `docker-compose.dev.yml` to spin up isolated PostgreSQL databases:
     - `portfolio_db` (Port: 5433 -> 5432)
     - `market_db` (Port: 5434 -> 5432)
     - `alert_db` (Port: 5435 -> 5432)
     - `research_db` (Port: 5436 -> 5432)
  3. Create shared `common-dto` module for reusable cross-service DTOs and Kafka event envelopes (`BaseEvent`, `EventHeader`).
  4. Configure `.gitignore`, `.editorconfig`, and root README.
- **Target Files:**
  - `pom.xml` (root parent with dependency management: Spring Boot 3.3+, Java 21, Lombok, MapStruct)
  - `docker/docker-compose.dev.yml` (Postgres instances with initialization scripts)
  - `common-dto/src/main/java/com/finsight/common/event/BaseEvent.java`
- **Verification:**
  - Run `docker compose -f docker/docker-compose.dev.yml up -d`
  - Verify DB connectivity: `pg_isready -h localhost -p 5433`
- **SDE-2 Concept:** Multi-repo vs. Monorepo trade-offs, Database-per-service isolation principles.

---

### Day 2: Portfolio Domain Modeling & High-Precision Financial Entities
- **Objective:** Model portfolio entities with explicit `BigDecimal` arithmetic, Flyway database migrations, and repository layer.
- **Checklist:**
  1. Configure Flyway in `portfolio-service` for repeatable schema versioning (`V1__init_portfolio_schema.sql`).
  2. Create JPA entities:
     - `Portfolio` (id, userId, name, currency, cashBalance)
     - `Holding` (id, portfolioId, symbol, quantity, averageBuyPrice, currentPrice, totalInvested, currentValue, unrealizedPnL, unrealizedPnLPercent, sector)
     - `Transaction` (id, portfolioId, idempotencyKey, symbol, transactionType, quantity, price, totalAmount, fees, executedAt)
  3. Enforce database constraints: unique `(portfolio_id, symbol)`, unique `idempotency_key`.
  4. Write JPA Repositories with custom query methods (e.g. `findByPortfolioIdAndSymbol`).
- **Target Files:**
  - `services/portfolio-service/src/main/resources/db/migration/V1__init_portfolio_schema.sql`
  - `services/portfolio-service/src/main/java/com/finsight/portfolio/entity/Portfolio.java`
  - `services/portfolio-service/src/main/java/com/finsight/portfolio/entity/Holding.java`
  - `services/portfolio-service/src/main/java/com/finsight/portfolio/entity/Transaction.java`
  - `services/portfolio-service/src/main/java/com/finsight/portfolio/repository/HoldingRepository.java`
- **Verification:**
  - Start `portfolio-service` and inspect Flyway table creations via `psql -h localhost -p 5433 -U postgres -d portfolio_db -c '\dt'`.
- **SDE-2 Concept:** Why `Double`/`Float` causes catastrophic rounding errors in financial ledgers; Banker's rounding (`RoundingMode.HALF_EVEN`).

---

### Day 3: Portfolio Business Logic & Transaction Processing
- **Objective:** Implement trade execution (BUY/SELL), weighted-average cost basis calculations, and REST APIs.
- **Checklist:**
  1. Implement `PortfolioService`:
     - `createPortfolio(userId, name, currency)`
     - `executeTrade(portfolioId, TradeRequest)`:
       - Validate portfolio existence and cash/share balances.
       - Calculate weighted average buy price on BUY:
         $$\text{New Avg Price} = \frac{(\text{Old Qty} \times \text{Old Avg Price}) + (\text{Trade Qty} \times \text{Trade Price})}{\text{Old Qty} + \text{Trade Qty}}$$
       - Calculate realized P&L on SELL and reduce share quantity.
       - Enforce transaction idempotency: return existing transaction if `idempotency_key` exists.
  2. Implement REST Controller with strict validation (`@Valid`, `@NotNull`, `@Positive`).
  3. Implement global `@RestControllerAdvice` error handler returning RFC 7807 `ProblemDetail`.
- **Target Files:**
  - `services/portfolio-service/src/main/java/com/finsight/portfolio/service/PortfolioService.java`
  - `services/portfolio-service/src/main/java/com/finsight/portfolio/controller/PortfolioController.java`
  - `services/portfolio-service/src/main/java/com/finsight/portfolio/exception/GlobalExceptionHandler.java`
- **Verification:**
  - `curl -X POST http://localhost:8081/api/v1/portfolios -H "Content-Type: application/json" -d '{"userId":"user-1","name":"Core Growth"}'`
  - `curl -X POST http://localhost:8081/api/v1/portfolios/1/transactions -H "Idempotency-Key: trade-001" -d '{"symbol":"AAPL","type":"BUY","quantity":10,"price":150.00}'`
- **SDE-2 Concept:** Idempotency keys in payment/order flows, Spring transaction propagation (`@Transactional(isolation = Isolation.READ_COMMITTED)`).

---

### Day 4: Unit & Integration Testing with Testcontainers
- **Objective:** Ensure bulletproof portfolio calculation correctness with JUnit 5, Mockito, and Testcontainers.
- **Checklist:**
  1. Write unit tests for calculation math (`PortfolioCalculationServiceTest`):
     - Multiple BUYs at varying prices verifying weighted average.
     - Partial SELLs verifying realized P&L and residual holding values.
     - Zero-share liquidation removing or archiving holding.
  2. Write integration tests using `Testcontainers` for PostgreSQL (`PortfolioControllerIntegrationTest`):
     - Duplicate transaction submissions with identical `idempotencyKey` asserting HTTP 200/409 with exact same payload.
     - Negative quantity/price validation failure tests.
- **Target Files:**
  - `services/portfolio-service/src/test/java/com/finsight/portfolio/service/PortfolioCalculationTest.java`
  - `services/portfolio-service/src/test/java/com/finsight/portfolio/integration/PortfolioControllerIT.java`
- **Verification:**
  - Run `./mvnw test -pl services/portfolio-service` -> 100% green tests.
- **SDE-2 Concept:** Deterministic testing vs mocking DB dialect behaviors with Testcontainers.

---

### Day 5: Market Data Service & Historical Stock Ingestion
- **Objective:** Build `market-data-service` to manage stock profiles, historical candles, and live quote lookups.
- **Checklist:**
  1. Flyway migration `V1__init_market_schema.sql` (`stocks`, `stock_quotes`, `historical_prices`).
  2. Implement seed data loader:
     - Pre-populate blue-chip tickers (AAPL, MSFT, GOOGL, NVDA, AMZN, INFY, TCS, RELIANCE) with sector classifications.
     - Populate 90 days of synthetic/sample OHLCV historical daily candles.
  3. Implement REST endpoints:
     - `GET /api/v1/market/stocks` - list active stocks with current price
     - `GET /api/v1/market/stocks/{symbol}` - ticker details & snapshot
     - `GET /api/v1/market/stocks/{symbol}/history` - daily OHLCV series
- **Target Files:**
  - `services/market-data-service/src/main/resources/db/migration/V1__init_market_schema.sql`
  - `services/market-data-service/src/main/java/com/finsight/market/service/MarketDataService.java`
  - `services/market-data-service/src/main/java/com/finsight/market/controller/MarketController.java`
  - `services/market-data-service/src/main/resources/data/seed-stocks.json`
- **Verification:**
  - `curl http://localhost:8082/api/v1/market/stocks/AAPL`
  - `curl "http://localhost:8082/api/v1/market/stocks/AAPL/history?days=30"`
- **SDE-2 Concept:** Caching strategies for market data (TTL, read-heavy vs write-heavy characteristics).

---

### Day 6: Real-Time Valuation Engine & Market Simulation
- **Objective:** Connect Market Data to Portfolio Service for real-time portfolio valuation and P&L recalculation.
- **Checklist:**
  1. Add valuation calculation endpoints in `PortfolioService`:
     - `GET /api/v1/portfolios/{id}/performance` - total invested, total current value, unrealized gain, daily change.
     - `GET /api/v1/portfolios/{id}/allocation` - percentage breakdown by stock and by sector.
  2. Implement `MarketPriceSimulator`:
     - Endpoint `POST /api/v1/market/simulator/tick` to simulate realistic percentage price movements (-3% to +3%).
     - Update `stock_quotes` table with new price, high/low, and timestamps.
- **Target Files:**
  - `services/portfolio-service/src/main/java/com/finsight/portfolio/dto/PortfolioPerformanceDTO.java`
  - `services/portfolio-service/src/main/java/com/finsight/portfolio/dto/SectorAllocationDTO.java`
  - `services/market-data-service/src/main/java/com/finsight/market/service/MarketSimulatorService.java`
- **Verification:**
  - Trigger price tick for AAPL to $190.00.
  - Query portfolio performance and verify total value and unrealized P&L reflect new price accurately.
- **SDE-2 Concept:** Decoupling read models from write models; eventual consistency expectations.

---

### Day 7: React 18 + TypeScript Dashboard (Core Views)
- **Objective:** Build the modern React dashboard with Vite, TailwindCSS, Lucide Icons, and Recharts.
- **Checklist:**
  1. Initialize `frontend/` using Vite + React + TypeScript.
  2. Setup Tailwind CSS, Axios client with base configuration, and React Router.
  3. Build **Screen 1: Portfolio Dashboard**:
     - Metric cards: Portfolio Value, Invested Capital, Total Unrealized P&L, Daily Return %.
     - Holdings Data Table with symbol, shares, average price, current price, total value, P&L badge.
     - Sector Allocation Donut Chart (Recharts).
     - Buy/Sell Trade Modal dialog with validation.
  4. Build **Screen 3: Stock Research**:
     - Ticker selector, interactive OHLCV Line/Candle chart, key valuation metrics.
- **Target Files:**
  - `frontend/src/pages/PortfolioDashboard.tsx`
  - `frontend/src/pages/StockResearchPage.tsx`
  - `frontend/src/components/HoldingsTable.tsx`
  - `frontend/src/components/AllocationChart.tsx`
  - `frontend/src/components/TradeModal.tsx`
  - `frontend/src/api/client.ts`
- **Verification:**
  - Run `npm run dev` in `frontend/`. Open browser at `http://localhost:5173`. Execute a trade and verify live update.
- **SDE-2 Concept:** Optimistic UI updates vs server-reconciliation; state normalization in frontend clients.

---

# Week 2: Spring Cloud Gateway, Kafka Event Streaming & AI Engine

---

### Day 8: Spring Cloud Gateway & Centralized Routing
- **Objective:** Establish the API Gateway layer, route configurations, CORS filters, and local container orchestration.
- **Checklist:**
  1. Implement `gateway-service` using Spring Cloud Gateway (reactive Netty-based).
  2. Configure `application.yml` routes for all downstream services with path rewriting and timeouts.
  3. Configure global CORS policy allowing React dev server (`localhost:5173`) and production origins.
  4. Configure Spring Boot Actuator health checks across services.
- **Target Files:**
  - `services/gateway-service/pom.xml`
  - `services/gateway-service/src/main/resources/application.yml`
  - `services/gateway-service/src/main/java/com/finsight/gateway/config/CorsConfig.java`
- **Verification:**
  - Call `curl http://localhost:8080/api/v1/portfolios/1` and verify routing to `portfolio-service:8081`.
- **SDE-2 Concept:** Reverse proxies vs API Gateways; Netty event-loop architecture vs Tomcat thread-per-request model.

---

### Day 9: JWT Authentication & Distributed Security Context
- **Objective:** Secure the microservices architecture using JWT tokens, stateless Spring Security, and gateway token propagation.
- **Checklist:**
  1. Implement JWT generation and validation utility in `common-security` / `gateway-service`.
  2. Configure Gateway Global Filter to intercept requests, validate `Authorization: Bearer <token>`, and inject `X-User-Id` / `X-User-Roles` headers to downstream services.
  3. In `portfolio-service`, configure Spring Security filter to accept user headers propagated from Gateway.
  4. Add `/api/v1/auth/login` mock/demo token generator for developer and demo usage.
- **Target Files:**
  - `services/gateway-service/src/main/java/com/finsight/gateway/filter/JwtAuthenticationFilter.java`
  - `services/portfolio-service/src/main/java/com/finsight/portfolio/security/HeaderAuthFilter.java`
- **Verification:**
  - Send unauthenticated request to Gateway -> Receive HTTP 401 Unauthorized.
  - Send request with valid JWT -> Downstream receives `X-User-Id` header and returns HTTP 200.
- **SDE-2 Concept:** Token relay vs Gateway-offloaded authentication; zero-trust internal network boundaries.

---

### Day 10: Apache Kafka Broker Integration & Price Producer
- **Objective:** Spin up Apache Kafka (KRaft mode) and implement event publishing in `market-data-service`.
- **Checklist:**
  1. Add Kafka (KRaft mode, no Zookeeper dependency) to `docker-compose.dev.yml` (Port: 9092).
  2. Configure Spring Kafka Producer in `market-data-service`:
     - Key serializer: `StringSerializer`
     - Value serializer: `JsonSerializer`
     - Delivery guarantees: `acks=all`, `retries=3`, `enable.idempotence=true`
  3. Publish `StockPriceUpdatedEvent` when simulated ticks or real quotes arrive:
     - Partition key set to `symbol` to ensure per-ticker ordering.
  4. Verify topics created with proper partitions: `market-price-updates` (3 partitions).
- **Target Files:**
  - `docker/docker-compose.dev.yml` (Kafka KRaft container definition)
  - `services/market-data-service/src/main/java/com/finsight/market/event/MarketPriceProducer.java`
  - `common-dto/src/main/java/com/finsight/common/event/StockPriceUpdatedEvent.java`
- **Verification:**
  - Trigger price update. Inspect topic with Kafka CLI:
    `docker exec -it kafka kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic market-price-updates --from-beginning`
- **SDE-2 Concept:** At-least-once delivery, partition hashing (`murmur2`), and ordering guarantees within partitions.

---

### Day 11: Alert Service & Idempotent Kafka Consumer
- **Objective:** Build `alert-service` to consume price events, evaluate threshold rules, and guarantee idempotency.
- **Checklist:**
  1. Setup `alert-service` with Flyway migrations (`alert_rules`, `alert_notifications`, `processed_events`).
  2. Implement Spring `@KafkaListener(topics = "market-price-updates", groupId = "alert-service-group")`.
  3. Implement **Idempotent Consumer Logic**:
     - Extract `eventId`.
     - Check `processed_events` table; if duplicate, ACK and skip.
     - Insert `eventId` within the same transaction that inserts triggered notifications.
  4. Implement Rule Evaluator:
     - Check if price crossed `ABOVE` or `BELOW` threshold.
     - If triggered, create `AlertNotification` record.
  5. Provide notification query API: `GET /api/v1/alerts/notifications`.
- **Target Files:**
  - `services/alert-service/src/main/resources/db/migration/V1__init_alert_schema.sql`
  - `services/alert-service/src/main/java/com/finsight/alert/consumer/MarketPriceConsumer.java`
  - `services/alert-service/src/main/java/com/finsight/alert/service/AlertRuleEvaluator.java`
  - `services/alert-service/src/main/java/com/finsight/alert/entity/ProcessedEvent.java`
- **Verification:**
  - Create alert: "Notify if AAPL > 185.00".
  - Publish price update event for AAPL at $190.00.
  - Verify alert notification is created. Re-publish exact same event -> Verify no duplicate notification.
- **SDE-2 Concept:** Idempotency patterns: Unique DB constraints vs distributed Redis locks; Consumer group rebalances.

---

### Day 12: Kafka Reliability: Error Handlers, Retries & Dead Letter Topic (DLT)
- **Objective:** Implement resilient exception handling, retry policies with exponential backoff, and DLT routing.
- **Checklist:**
  1. Configure `DefaultErrorHandler` in `alert-service`:
     - 3 retry attempts with exponential backoff (initial interval: 1s, multiplier: 2.0).
     - Route permanently failing messages to `market-price-updates.DLT` topic.
  2. Implement DLT consumer listener to record failed poison pills into an audit log table for admin inspection.
  3. Add test endpoint to simulate corrupt message or transient database exception.
- **Target Files:**
  - `services/alert-service/src/main/java/com/finsight/alert/config/KafkaConsumerConfig.java`
  - `services/alert-service/src/main/java/com/finsight/alert/consumer/DeadLetterQueueConsumer.java`
- **Verification:**
  - Post malformed JSON event to `market-price-updates`.
  - Watch application logs for 3 retries followed by message routing to `.DLT`.
- **SDE-2 Concept:** Poison-pill message handling; backpressure; managing consumer group lag during downstream outages.

---

### Day 13: Research Service & PostgreSQL Full-Text Search (TSVECTOR)
- **Objective:** Build `research-service` document indexer using PostgreSQL Full-Text Search for SEC filings and company reports.
- **Checklist:**
  1. Setup `research-service` with Flyway (`research_documents`, `research_reports`).
  2. Implement `to_tsvector` full-text search with `GIN` index over filings content.
  3. Ingest real-world sample filings data (extracts from 10-K and earnings calls for AAPL, MSFT, INFY, NVDA) covering risks, revenue, and headwinds.
  4. Implement repository query using native SQL `ts_rank` and `plainto_tsquery`:
     ```sql
     SELECT *, ts_rank(content_vector, plainto_tsquery(:query)) as rank 
     FROM research_documents 
     WHERE content_vector @@ plainto_tsquery(:query) 
     ORDER BY rank DESC LIMIT 5;
     ```
  5. Provide search API: `GET /api/v1/research/documents/search?query=...`.
- **Target Files:**
  - `services/research-service/src/main/resources/db/migration/V1__init_research_schema.sql`
  - `services/research-service/src/main/java/com/finsight/research/repository/ResearchDocumentRepository.java`
  - `services/research-service/src/main/java/com/finsight/research/service/DocumentSearchService.java`
- **Verification:**
  - `curl "http://localhost:8083/api/v1/research/documents/search?query=supply+chain+risks"`
  - Verify ranked search results with matched excerpts and citations.
- **SDE-2 Concept:** Full-Text Search vs Vector Embeddings (pgvector); latency, cost, and lexical accuracy trade-offs.

---

### Day 14: Grounded AI Agent Engine with Tool Calling
- **Objective:** Implement AI orchestration layer using LLM tool/function calling grounded strictly in real application data.
- **Checklist:**
  1. Implement AI provider client (OpenAI / Anthropic / Gemini API with structured tools).
  2. Define and register backend tools:
     - `getPortfolioSummary(portfolioId)`: Calls Portfolio Service for deterministic totals & P&L.
     - `getConcentratedHoldings(portfolioId)`: Identifies positions exceeding 25% portfolio weight.
     - `getSectorExposure(portfolioId)`: Retrieves sector concentration breakdown.
     - `searchResearchFilings(symbol, query)`: Retrieves ranked filing snippets from PostgreSQL.
  3. Build LLM prompt orchestration loop:
     - Inject system instructions forbidding mathematical speculation or invented numbers.
     - Handle model tool-call requests, execute local Java tools, feed outputs back, and synthesize response.
  4. Provide chat endpoint: `POST /api/v1/research/assistant/chat`.
- **Target Files:**
  - `services/research-service/src/main/java/com/finsight/research/ai/AiAgentService.java`
  - `services/research-service/src/main/java/com/finsight/research/ai/tools/PortfolioAnalysisTools.java`
  - `services/research-service/src/main/java/com/finsight/research/ai/tools/ResearchDocumentTools.java`
  - `services/research-service/src/main/java/com/finsight/research/controller/AiAssistantController.java`
- **Verification:**
  - Ask: *"Why is my portfolio down this week and what are my concentration risks?"*
  - Verify LLM invokes `getPortfolioSummary` and `getConcentratedHoldings`, then cites actual holdings and percentages.
- **SDE-2 Concept:** Grounded RAG vs LLM Hallucinations; Function calling loop architecture and tool timeout safety.

---

# Week 3: Distributed Reliability, Event UI, CI/CD & Production Polish

---

### Day 15: Transactional Outbox Pattern in Portfolio Service
- **Objective:** Eliminate dual-write anomalies by implementing the Transactional Outbox Pattern with a scheduled poller.
- **Checklist:**
  1. Add `outbox_events` table migration to `portfolio-service`.
  2. When executing a trade, save `Transaction`, update `Holding`, and save `OutboxEvent(PENDING)` in the **same DB transaction**.
  3. Implement `OutboxPublisherWorker`:
     - `@Scheduled(fixedDelay = 2000)` worker.
     - Query pending events: `SELECT * FROM outbox_events WHERE status = 'PENDING' ORDER BY created_at FOR UPDATE SKIP LOCKED LIMIT 20`.
     - Send to Kafka `portfolio-events` topic.
     - On successful Kafka callback ACK, update status to `PUBLISHED` and record `published_at`.
     - On error, increment `retry_count` and apply exponential delay.
- **Target Files:**
  - `services/portfolio-service/src/main/resources/db/migration/V2__create_outbox_table.sql`
  - `services/portfolio-service/src/main/java/com/finsight/portfolio/outbox/OutboxEvent.java`
  - `services/portfolio-service/src/main/java/com/finsight/portfolio/outbox/OutboxPublisherWorker.java`
- **Verification:**
  - Temporarily pause Kafka broker (`docker stop kafka`). Execute trade in Portfolio.
  - Verify DB trade succeeds and outbox table has `PENDING` record.
  - Start Kafka broker (`docker start kafka`). Watch poller drain outbox to `PUBLISHED`.
- **SDE-2 Concept:** Two-Phase Commit (2PC) vs Transactional Outbox vs Change Data Capture (Debezium).

---

### Day 16: React AI Research Assistant & Report Generator UI
- **Objective:** Build the interactive AI assistant interface and company research report view in the React dashboard.
- **Checklist:**
  1. Build **Screen 2: AI Research Assistant**:
     - Chat stream window with Markdown formatting, syntax highlighting, and metric callout badges.
     - Prompt suggestion pills ("Analyze my portfolio risks", "Explain my top holding's earnings", "Check tech exposure").
     - **"Tools Executed" collapsible drawer** showing what backend tools the agent invoked and with what parameters.
     - Cited source pill badges linking to filing source snippets.
  2. Build Company Research Report Generator:
     - Button: "Generate Research Report for [Symbol]".
     - Render structured report: Executive Summary, Key Ratios, Risks, Catalysts, and Source Citations.
- **Target Files:**
  - `frontend/src/pages/AiAssistantPage.tsx`
  - `frontend/src/components/ChatMessage.tsx`
  - `frontend/src/components/ToolCallBadge.tsx`
  - `frontend/src/components/ResearchReportModal.tsx`
- **Verification:**
  - Run query in UI, click on "Tools Executed" drawer, and verify transparency of underlying Java tool data.
- **SDE-2 Concept:** Responsible AI interfaces; UX patterns for surfacing grounding, provenance, and confidence to end users.

---

### Day 17: Kafka Event Streaming Monitor & Health UI
- **Objective:** Build live operational monitoring screens demonstrating the distributed Kafka pipeline and microservice health.
- **Checklist:**
  1. Build **Screen 4: Event Monitoring**:
     - Live feed of events flowing through Kafka (`market-price-updates`, `portfolio-events`, `alert-notifications`).
     - Visual badge by event type and topic.
     - Payload inspector modal to view raw JSON events.
     - DLT alert indicator for failed messages.
  2. Build **Screen 5: System Health**:
     - Service availability cards (Gateway, Portfolio, Market, Research, Alert).
     - Database connection pool stats and Kafka broker status.
- **Target Files:**
  - `frontend/src/pages/EventMonitorPage.tsx`
  - `frontend/src/pages/SystemHealthPage.tsx`
  - `frontend/src/components/EventPayloadViewer.tsx`
- **Verification:**
  - Execute a trade, trigger a market tick, and watch events appear sequentially in the Event Monitor table.
- **SDE-2 Concept:** Operational observability in distributed microservices; making asynchronous architecture visible.

---

### Day 18: Docker Multi-Stage Optimization & Unified Deployment
- **Objective:** Create optimized, secure multi-stage Dockerfiles for all microservices and unified `docker-compose.yml`.
- **Checklist:**
  1. Create multi-stage `Dockerfile` for each Spring Boot service:
     - Stage 1: Maven build with cached dependencies.
     - Stage 2: Eclipse Temurin JRE 21 Alpine, non-root user (`appuser:1001`), optimized JVM flags (`-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`).
  2. Create multi-stage `Dockerfile` for React frontend (Vite build -> Nginx Alpine reverse proxy).
  3. Assemble root `docker-compose.yml` linking all 5 services, Gateway, React UI, Kafka, and PostgreSQL databases with dependency health checks.
- **Target Files:**
  - `services/portfolio-service/Dockerfile`
  - `services/market-data-service/Dockerfile`
  - `services/research-service/Dockerfile`
  - `services/alert-service/Dockerfile`
  - `services/gateway-service/Dockerfile`
  - `frontend/Dockerfile`
  - `docker-compose.yml`
- **Verification:**
  - Run `docker compose up --build -d` from empty state.
  - Verify all 8 containers healthy via `docker compose ps`. Access UI at `http://localhost:3000`.
- **SDE-2 Concept:** Container security (non-root users), JVM container ergonomics, layer caching, and health-check order dependencies.

---

### Day 19: GitHub Actions Automated CI/CD Pipeline
- **Objective:** Implement automated build, testing, linting, and container build validation on pull requests and pushes.
- **Checklist:**
  1. Create `.github/workflows/ci.yml`:
     - Step 1: Checkout repository.
     - Step 2: Set up JDK 21 and Node.js 20.
     - Step 3: Run Spring Boot unit & integration tests (`mvn clean verify`).
     - Step 4: Run frontend TypeScript compile & lint (`npm run build`).
     - Step 5: Build Docker images to verify Dockerfile syntax without pushing.
  2. Create `.github/workflows/cd-ecr.yml` (template for pushing tagged images to AWS ECR on main branch merge).
- **Target Files:**
  - `.github/workflows/ci.yml`
  - `.github/workflows/cd-ecr.yml`
- **Verification:**
  - Push branch or trigger workflow with `act` or via GitHub Actions web console. Confirm all jobs pass.
- **SDE-2 Concept:** CI test pyramid; fail-fast build strategies; OIDC credentials vs static AWS access keys.

---

### Day 20: AWS Cloud Infrastructure Blueprint (ECS Fargate + RDS)
- **Objective:** Document and script AWS cloud deployment architecture using Terraform / CloudFormation specifications.
- **Checklist:**
  1. Create infrastructure specification files:
     - Amazon ECR repositories configuration.
     - Amazon ECS Task Definitions (Fargate, 0.5 vCPU / 1GB RAM per service).
     - Amazon RDS PostgreSQL 16 instance specification.
     - Application Load Balancer (ALB) routing rules for Gateway and Frontend.
     - AWS Secrets Manager integration for database credentials and LLM keys.
  2. Create cost optimization and teardown script to prevent unexpected AWS cloud charges.
- **Target Files:**
  - `infrastructure/aws/ecs-task-definitions.json`
  - `infrastructure/aws/alb-routing.json`
  - `infrastructure/aws/README.md` (Step-by-step AWS deployment & teardown guide)
- **Verification:**
  - Validate task definitions with AWS CLI dry-run or linting tools.
- **SDE-2 Concept:** Cloud cost economics; Fargate vs EC2 auto-scaling; zero-downtime rolling updates.

---

### Day 21: Polish, Demo Dataset Seeding & SDE-2 Interview Prep Guide
- **Objective:** Final end-to-end audit, demo walkthrough dataset, comprehensive documentation, and interview defense manual.
- **Checklist:**
  1. Write automated demo seeder script:
     - Creates realistic portfolio with 8 holdings (Tech, Finance, Healthcare, Energy).
     - Generates 15 historical transactions.
     - Configures 3 active alert thresholds.
  2. Write production `README.md` with high-resolution architecture diagrams, API curl cheatsheets, and quickstart commands.
  3. Compile **SDE-2 Interview Defense Guide** in `docs/interview-qa.md`:
     - Dual-write & Transactional Outbox rationale.
     - Idempotent consumer design & failure modes.
     - BigDecimal vs IEEE-754 precision.
     - Kafka partition rebalances & consumer group scaling.
     - AI Tool-calling vs ungrounded LLM chatbots.
- **Target Files:**
  - `README.md`
  - `scripts/seed-demo-data.sh`
  - `docs/interview-qa.md`
  - `docs/architecture-diagram.png`
- **Verification:**
  - Execute clean checkout test: Clone -> `docker compose up` -> run `./scripts/seed-demo-data.sh` -> Open UI -> Everything works seamlessly out-of-the-box.
- **SDE-2 Concept:** Comprehensive technical storytelling; communicating trade-offs, edge cases, and post-incident learnings.

