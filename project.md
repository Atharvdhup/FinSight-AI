# FinSight AI — Project Specification & Architecture Blueprint

**Version:** 1.0.0  
**Target Environment:** Java 21, Spring Boot 3.3+, React 18 + TypeScript, Apache Kafka (KRaft), PostgreSQL 16, Docker Compose, AWS (ECS Fargate, RDS, ECR)  
**Author / Lead Engineer:** Atharv Dhup  
**Architecture Domain:** Event-Driven Microservices, Financial Intelligence, AI Orchestration  

---

## 1. Executive Overview

**FinSight AI** is a production-grade, event-driven fintech platform designed to bridge enterprise microservices engineering with practical, evidence-grounded AI intelligence. Unlike trivial CRUD apps or thin LLM wrappers, FinSight AI implements:

1. **Autonomous Service Boundaries:** Independent Spring Boot services (Portfolio, Market Data, Research, Alert), each strictly owning its database schema.
2. **Event-Driven Decoupling via Apache Kafka:** Asynchronous price fan-out, threshold alerts, valuation recalculations, dead-letter queues (DLQ), and idempotent consumption.
3. **Enterprise Transactional Outbox Pattern:** Guarantees dual-write consistency between relational state changes and Kafka events without distributed transactions (2PC).
4. **Deterministic Financial Math:** Strict `BigDecimal` representation with standard financial rounding rules (`HALF_EVEN` / Bankers' Rounding) preventing IEEE-754 floating-point inaccuracies.
5. **Grounded AI Orchestration:** Tool-calling workflow where the LLM does not calculate numbers or hallucinate balance sheets; it retrieves structured deterministic metrics, executes vector/full-text search over indexed research filings, and produces cited, evidence-backed investment analyses.
6. **Production Observability & Cloud Readiness:** Containerized via Docker Compose, automated via GitHub Actions CI/CD, and designed for AWS ECS Fargate, RDS, and Secrets Manager.

---

## 2. System Architecture

```
                                  +----------------------------+
                                  |   React 18 + TS Dashboard  |
                                  |   (Vite, Tailwind, Redux)  |
                                  +--------------+-------------+
                                                 |
                                         HTTPS / REST / WS
                                                 |
                                                 v
                                  +----------------------------+
                                  |    Spring Cloud Gateway    |
                                  |      (Port: 8080)          |
                                  |   JWT Auth / Rate Limiting |
                                  +--------------+-------------+
                                                 |
         +---------------------------------------+---------------------------------------+
         |                                       |                                       |
         v                                       v                                       v
+-------------------------+             +-------------------------+             +-------------------------+
|    Portfolio Service    |             |   Market Data Service   |             |    Research & AI Svc    |
|       (Port: 8081)      |             |       (Port: 8082)      |             |       (Port: 8083)      |
|  - Holdings & Trades    |             |  - Stock Ingestion      |             |  - Reports & Filings    |
|  - Position/P&L Calc    |             |  - Price History/Feeds  |             |  - LLM Tool Calling     |
|  - Outbox Publisher     |             |  - Price Publisher      |             |  - RAG Doc Search       |
|  DB: portfolio_db (Pg)  |             |  DB: market_db (Pg)     |             |  DB: research_db (Pg)   |
+------------+------------+             +------------+------------+             +------------+------------+
             |                                       |                                       |
             |                                       |                                       |
             +--------------------+                  v                                       |
                                  |       +---------------------+                            |
                                  |       |    Apache Kafka     |                            |
                                  +------>|       (KRaft)       |<---------------------------+
                                          +----------+----------+
                                                     |
                                                     v
                                        +-------------------------+
                                        |      Alert Service      |
                                        |       (Port: 8084)      |
                                        |  - Rule Evaluation      |
                                        |  - Idempotent Consumer  |
                                        |  - Notification State   |
                                        |  DB: alert_db (Pg)      |
                                        +-------------------------+
```

---

## 3. Microservice Specifications & Schemas

### 3.1. Spring Cloud Gateway (`gateway-service:8080`)
- **Role:** Single unified API entry point, route dispatching, CORS handling, JWT authentication filter, request rate-limiting.
- **Routes:**
  - `/api/v1/portfolios/**` -> `http://portfolio-service:8081`
  - `/api/v1/market/**` -> `http://market-data-service:8082`
  - `/api/v1/research/**` -> `http://research-service:8083`
  - `/api/v1/alerts/**` -> `http://alert-service:8084`

---

### 3.2. Portfolio Service (`portfolio-service:8081`)
- **Primary Domain:** Virtual portfolios, buy/sell trade execution, cost-basis calculation (FIFO/Weighted Avg), real-time valuation, allocation by sector/stock.
- **Database:** `portfolio_db` (PostgreSQL)

#### Database Schema:
```sql
CREATE TABLE portfolios (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    name VARCHAR(100) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE holdings (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    symbol VARCHAR(12) NOT NULL,
    quantity NUMERIC(19, 4) NOT NULL,
    average_buy_price NUMERIC(19, 4) NOT NULL,
    current_price NUMERIC(19, 4) NOT NULL,
    total_invested NUMERIC(19, 4) NOT NULL,
    current_value NUMERIC(19, 4) NOT NULL,
    unrealized_pnl NUMERIC(19, 4) NOT NULL,
    unrealized_pnl_pct NUMERIC(8, 4) NOT NULL,
    sector VARCHAR(50),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_portfolio_symbol UNIQUE (portfolio_id, symbol)
);

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    idempotency_key VARCHAR(64) NOT NULL UNIQUE,
    symbol VARCHAR(12) NOT NULL,
    transaction_type VARCHAR(10) NOT NULL CHECK (transaction_type IN ('BUY', 'SELL')),
    quantity NUMERIC(19, 4) NOT NULL,
    price NUMERIC(19, 4) NOT NULL,
    total_amount NUMERIC(19, 4) NOT NULL,
    fees NUMERIC(19, 4) DEFAULT 0.00,
    executed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    topic VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, PUBLISHED, FAILED
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_outbox_pending ON outbox_events(status, created_at) WHERE status = 'PENDING';
```

#### Key APIs:
- `POST /api/v1/portfolios` - Create portfolio
- `GET  /api/v1/portfolios/{id}` - Get portfolio summary (totals, cash, invested, P&L)
- `POST /api/v1/portfolios/{id}/transactions` - Execute trade (Idempotency-Key header enforced)
- `GET  /api/v1/portfolios/{id}/holdings` - List positions with weights
- `GET  /api/v1/portfolios/{id}/performance` - Performance breakdown
- `GET  /api/v1/portfolios/{id}/allocation` - Sector and asset allocation metrics

---

### 3.3. Market Data Service (`market-data-service:8082`)
- **Primary Domain:** Ingests market quotes, maintains historical OHLCV candles, provides price lookups, publishes price updates to Kafka, provides market simulation endpoints.
- **Database:** `market_db` (PostgreSQL)

#### Database Schema:
```sql
CREATE TABLE stocks (
    symbol VARCHAR(12) PRIMARY KEY,
    company_name VARCHAR(150) NOT NULL,
    sector VARCHAR(50) NOT NULL,
    industry VARCHAR(100),
    currency VARCHAR(3) DEFAULT 'USD',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE stock_quotes (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(12) NOT NULL REFERENCES stocks(symbol),
    current_price NUMERIC(19, 4) NOT NULL,
    open_price NUMERIC(19, 4),
    high_price NUMERIC(19, 4),
    low_price NUMERIC(19, 4),
    previous_close NUMERIC(19, 4),
    change_amount NUMERIC(19, 4),
    change_pct NUMERIC(8, 4),
    volume BIGINT,
    market_time TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_stock_quote UNIQUE (symbol)
);

CREATE TABLE historical_prices (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(12) NOT NULL REFERENCES stocks(symbol),
    price_date DATE NOT NULL,
    open_price NUMERIC(19, 4) NOT NULL,
    high_price NUMERIC(19, 4) NOT NULL,
    low_price NUMERIC(19, 4) NOT NULL,
    close_price NUMERIC(19, 4) NOT NULL,
    volume BIGINT NOT NULL,
    CONSTRAINT uq_symbol_date UNIQUE (symbol, price_date)
);
CREATE INDEX idx_hist_symbol_date ON historical_prices(symbol, price_date DESC);
```

#### Key APIs:
- `GET  /api/v1/market/stocks` - List active stocks
- `GET  /api/v1/market/stocks/{symbol}` - Current price & company details
- `GET  /api/v1/market/stocks/{symbol}/history?from=YYYY-MM-DD&to=YYYY-MM-DD` - Candle data
- `POST /api/v1/market/simulator/tick` - Trigger simulated or manual price fluctuation (publishes to Kafka)

---

### 3.4. Alert Service (`alert-service:8084`)
- **Primary Domain:** Asynchronous consumer of `market-price-updates`. Evaluates user rules (`GREATER_THAN`, `LESS_THAN`, `% CHANGE`). Dispatches alerts and tracks notification delivery.
- **Database:** `alert_db` (PostgreSQL)

#### Database Schema:
```sql
CREATE TABLE alert_rules (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    symbol VARCHAR(12) NOT NULL,
    condition_type VARCHAR(20) NOT NULL, -- ABOVE, BELOW, PCT_CHANGE_UP, PCT_CHANGE_DOWN
    threshold_value NUMERIC(19, 4) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    triggered_count INT DEFAULT 0,
    last_triggered_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE alert_notifications (
    id BIGSERIAL PRIMARY KEY,
    alert_rule_id BIGINT REFERENCES alert_rules(id) ON DELETE SET NULL,
    user_id VARCHAR(64) NOT NULL,
    symbol VARCHAR(12) NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    triggered_price NUMERIC(19, 4) NOT NULL,
    threshold_price NUMERIC(19, 4) NOT NULL,
    status VARCHAR(20) DEFAULT 'UNREAD', -- UNREAD, READ, DISMISSED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Idempotent Consumer Audit Table
CREATE TABLE processed_events (
    event_id VARCHAR(64) PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    consumer_group VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

#### Key APIs:
- `POST /api/v1/alerts/rules` - Create alert rule
- `GET  /api/v1/alerts/rules` - Get user rules
- `DELETE /api/v1/alerts/rules/{id}` - Deactivate rule
- `GET  /api/v1/alerts/notifications` - Retrieve recent notification history
- `PATCH /api/v1/alerts/notifications/{id}/read` - Mark notification as read

---

### 3.5. Research & AI Capability Service (`research-service:8083`)
- **Primary Domain:** Financial documents store (10-K, 10-Q, quarterly summaries), PostgreSQL Full-Text Search / RAG index, LLM tool-calling orchestration engine, research report generator.
- **Database:** `research_db` (PostgreSQL)

#### Database Schema:
```sql
CREATE TABLE research_documents (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(12) NOT NULL,
    doc_type VARCHAR(30) NOT NULL, -- 10-K, 10-Q, EARNINGS_CALL, ANALYST_NOTE
    fiscal_period VARCHAR(20) NOT NULL, -- e.g. Q3-2024, FY-2023
    title VARCHAR(200) NOT NULL,
    source_url VARCHAR(500),
    content TEXT NOT NULL,
    content_vector TSVECTOR GENERATED ALWAYS AS (to_tsvector('english', title || ' ' || content)) STORED,
    published_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_research_doc_vector ON research_documents USING GIN(content_vector);
CREATE INDEX idx_research_symbol ON research_documents(symbol);

CREATE TABLE research_reports (
    id UUID PRIMARY KEY,
    symbol VARCHAR(12) NOT NULL,
    report_title VARCHAR(255) NOT NULL,
    executive_summary TEXT NOT NULL,
    financial_analysis JSONB NOT NULL,
    risks_and_headwinds JSONB NOT NULL,
    catalysts JSONB NOT NULL,
    cited_sources JSONB NOT NULL,
    model_name VARCHAR(50) NOT NULL,
    generated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

#### AI Agent Tool Specification:
The AI Engine employs a structured tool-calling architecture:

```
                  +-----------------------------------+
                  |         User Prompt / Query       |
                  +-----------------+-----------------+
                                    |
                                    v
                  +-----------------------------------+
                  |     AI Orchestration Controller   |
                  +-----------------+-----------------+
                                    |
                     Tool Calling Decision via LLM
                                    |
            +-----------------------+-----------------------+
            |                                               |
            v                                               v
+-----------------------+                       +-----------------------+
| PortfolioMetricsTool  |                       | DocumentSearchTool    |
| - getPortfolioSummary |                       | - searchFilings(q)    |
| - getTopHoldings      |                       | - getDocumentById     |
| - getSectorExposures  |                       +-----------------------+
+-----------+-----------+                                   |
            |                                               |
            v                                               v
  portfolio_service REST                         research_db TSVECTOR Query
            |                                               |
            +-----------------------+-----------------------+
                                    |
                                    v
                  +-----------------------------------+
                  |     LLM Response Synthesizer      |
                  |  - Grounded claims only           |
                  |  - Explicit metric citations      |
                  |  - Source document attributions   |
                  +-----------------+-----------------+
                                    |
                                    v
                  +-----------------------------------+
                  |     JSON / Formatted Output       |
                  +-----------------------------------+
```

#### Key APIs:
- `POST /api/v1/research/assistant/chat` - Natural language Q&A with tool calling
- `POST /api/v1/research/companies/{symbol}/reports` - Generate structured report
- `GET  /api/v1/research/reports/{reportId}` - Retrieve generated report
- `GET  /api/v1/research/documents/search?symbol=INFY&query=risk` - Search filings

---

## 4. Apache Kafka Architecture & Event Design

### 4.1. Kafka Topics
| Topic Name | Partitions | Replication | Cleanup Policy | Producer(s) | Consumer(s) |
|---|---|---|---|---|---|
| `market-price-updates` | 3 | 1 (dev) / 3 (prod) | delete (retention: 24h) | Market Data Service | Alert Service, Portfolio Service |
| `portfolio-events` | 3 | 1 (dev) / 3 (prod) | delete (retention: 7d) | Portfolio Service (Outbox) | Research Service, Audit/Analytics |
| `alert-notifications`| 2 | 1 (dev) / 3 (prod) | delete (retention: 7d) | Alert Service | Notification Gateway / UI WebSocket |
| `market-price-updates.DLT` | 1 | 1 (dev) / 3 (prod) | delete (retention: 14d) | Alert Service Error Handler | Operations / Dead Letter Monitor |

### 4.2. Event Payload Schemas

#### Event: `StockPriceUpdatedEvent`
```json
{
  "eventId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
  "eventType": "StockPriceUpdated",
  "timestamp": "2026-10-29T10:15:30Z",
  "source": "market-data-service",
  "version": "1.0",
  "data": {
    "symbol": "AAPL",
    "price": 182.50,
    "previousClose": 180.20,
    "change": 2.30,
    "changePercent": 1.2764,
    "currency": "USD",
    "volume": 4230190
  }
}
```
*Partition Key:* `symbol` (guarantees per-ticker event sequencing across partitions).

#### Event: `TransactionExecutedEvent`
```json
{
  "eventId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "eventType": "TransactionExecuted",
  "timestamp": "2026-10-29T10:15:35Z",
  "source": "portfolio-service",
  "version": "1.0",
  "data": {
    "portfolioId": 101,
    "transactionId": 5042,
    "symbol": "MSFT",
    "transactionType": "BUY",
    "quantity": 25.0000,
    "price": 415.2000,
    "totalAmount": 10380.0000,
    "fees": 2.5000,
    "executedAt": "2026-10-29T10:15:34Z"
  }
}
```
*Partition Key:* `portfolioId` string.

---

## 5. Enterprise Reliability & Failure Mitigation Patterns

### 5.1. Transactional Outbox Pattern
**Problem:** In the Portfolio Service, saving a trade to PostgreSQL and publishing to Kafka is not atomically atomic. Network dropouts or JVM crashes between the DB commit and Kafka send cause lost events (dual-write problem).  
**Solution:**
1. Within a single `@Transactional` DB transaction, the transaction record is inserted into `transactions` and an event row is inserted into `outbox_events` with status `PENDING`.
2. A dedicated publisher worker (polling with Spring `@Scheduled` + `SELECT FOR UPDATE SKIP LOCKED` or CDC) reads `PENDING` records, publishes to Kafka with an ACK guarantee (`acks=all`), and marks the record `PUBLISHED`.
3. If Kafka is down, outbox records remain safely in PostgreSQL and retry with exponential backoff.

### 5.2. Idempotent Consumer Pattern
**Problem:** Kafka delivers messages with *at-least-once* semantics. Rebalances, producer retries, or network timeouts can cause the Alert Service to consume the same price update twice.  
**Solution:**
1. Every event carries a deterministic `eventId`.
2. The consumer wraps processing in a database transaction and attempts an `INSERT INTO processed_events (event_id, consumer_group)`.
3. If an existing `event_id` is found (Unique constraint violation), the event is acknowledged and immediately skipped without re-evaluating rules or sending duplicate alerts.

### 5.3. Dead Letter Topic (DLT) & Retries
- Configured via Spring Kafka `DefaultErrorHandler` paired with a `FixedBackOff` (3 attempts, 1000ms delay).
- When unrecoverable deserialization errors or repeated database failures occur, the message is routed to `.DLT` for inspection.
- The UI Screen 4 (Event Monitoring) queries the DLT audit to surface failed messages to the user.

### 5.4. Deterministic Financial Precision
- **No `double` or `float` for currencies or shares.** All financial quantities, prices, fees, and calculations use `java.math.BigDecimal`.
- Calculations enforce explicit scale and rounding:
  ```java
  BigDecimal pnl = currentPrice.subtract(buyPrice)
      .multiply(quantity)
      .setScale(4, RoundingMode.HALF_EVEN);
  ```

---

## 6. Frontend Architecture (React 18 + TypeScript)

The UI is built with Vite, React 18, TypeScript, TailwindCSS, Lucide Icons, and Recharts.

### 5 Primary Application Views:
1. **Portfolio Dashboard:** Real-time net worth, invested balance, total unrealized P&L, daily returns, sector allocation pie chart, holding breakdown table with quick trade actions.
2. **AI Research Assistant:** Chat and analytical panel supporting interactive questions, prompt suggestions ("Why is my portfolio down?", "Compare AAPL vs MSFT"), structured evidence breakdown, and tool call transparency.
3. **Stock Research & Filings:** Real-time quote cards, historical candlestick/line chart with time ranges (1W, 1M, 1Y), key valuation ratios, indexed filings list, and a "Generate AI Research Report" button.
4. **Event Streaming Monitor:** Real-time stream of Kafka events passing between microservices (price ticks, transactions, alerts, retries, DLT items) proving the distributed pipeline is active.
5. **System Health & Observability:** Microservice uptime status, Spring Boot Actuator health checks, Kafka broker connectivity, and database latency.

---

## 7. Cloud Deployment Architecture (AWS)

```
                       +-------------------------+
                       |    Internet Gateway     |
                       +------------+------------+
                                    |
                                    v
                       +-------------------------+
                       | Application Load Balancer
                       +------------+------------+
                                    |
              +---------------------+---------------------+
              |                                           |
              v                                           v
+---------------------------+               +---------------------------+
|    AWS ECS Fargate: UI    |               |  AWS ECS Fargate: Gateway |
|    Dockerized React/Nginx |               |  Spring Cloud Gateway     |
+---------------------------+               +-------------+-------------+
                                                          |
                      +-------------------+---------------+-------------------+
                      |                   |                                   |
                      v                   v                                   v
          +-----------------------+ +-----------------------+ +-----------------------+
          | ECS: Portfolio Service| | ECS: Market Service   | | ECS: Research Service |
          +-----------+-----------+ +-----------+-----------+ +-----------+-----------+
                      |                         |                         |
                      +-------------------+     |     +-------------------+
                                          |     |     |
                                          v     v     v
                                    +-----------------------+
                                    | AWS RDS PostgreSQL 16 |
                                    | (Multi-database / DB) |
                                    +-----------------------+
```

- **Amazon ECR:** Private Docker registries for each microservice image.
- **Amazon ECS (Fargate):** Serverless container execution with auto-restart, health-checking, and zero EC2 OS maintenance.
- **Amazon RDS PostgreSQL:** Multi-AZ managed database instance with automated daily backups.
- **AWS Secrets Manager:** Centralized store for DB credentials, JWT secrets, and AI API keys.
- **AWS CloudWatch:** Centralized log aggregation across all container stdout/stderr streams.

