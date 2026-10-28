# FinSight AI — AI-Powered Investment Research & Portfolio Intelligence Platform

> A production-grade, event-driven fintech platform combining Spring Boot microservices, Apache Kafka, PostgreSQL database-per-service isolation, deterministic `BigDecimal` math, grounded AI tool calling, and a modern React dashboard.

---

## 🏛 System Architecture

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

## 📦 Project Modules

| Module | Port | Technology | Purpose |
|---|---|---|---|
| [`common-dto`](./common-dto) | N/A | Java 21, Jackson, Lombok | Shared DTOs, Event envelopes (`BaseEvent`, `EventHeader`), and Domain Enums |
| [`gateway-service`](./services/gateway-service) | `8080` | Spring Cloud Gateway, Netty | Single entry point, CORS, JWT authentication & route dispatch |
| [`portfolio-service`](./services/portfolio-service) | `8081` | Spring Boot, JPA, Flyway, PostgreSQL | Virtual portfolio management, positions, P&L, Transactional Outbox |
| [`market-data-service`](./services/market-data-service) | `8082` | Spring Boot, Spring Kafka, PostgreSQL | Market quotes, historical candles, Kafka price producer, simulator |
| [`alert-service`](./services/alert-service) | `8084` | Spring Boot, Spring Kafka, PostgreSQL | Threshold rule engine, Idempotent Kafka consumer, notifications, DLT |
| [`research-service`](./services/research-service) | `8083` | Spring Boot, JPA, TSVECTOR, LLM Tools | Full-Text Search over SEC filings, AI tool calling, research reports |
| [`frontend`](./frontend) | `5173` / `3000` | React 18, TypeScript, TailwindCSS | Real-time portfolio, AI assistant, event monitor, and health dashboard |

---

## 🚀 Quickstart & Local Setup

### Prerequisites
- **Java:** JDK 21+ (`java -version`)
- **Maven:** Bundled via `./mvnw`
- **Docker:** Docker Compose (`docker compose version`)
- **Node.js:** Node 20+ (for frontend dashboard)

### 1. Start Infrastructure (PostgreSQL & Kafka)
```bash
docker compose -f docker/docker-compose.dev.yml up -d
```

### 2. Build All Services
```bash
./mvnw clean install -DskipTests
```

### 3. Verify Health
- Gateway: http://localhost:8080/actuator/health
- Portfolio Service: http://localhost:8081/actuator/health
- Market Data Service: http://localhost:8082/actuator/health
- Research Service: http://localhost:8083/actuator/health
- Alert Service: http://localhost:8084/actuator/health

---

## 📚 Documentation
- [Project Architecture & Specifications](file:///Users/atharvdhup/FinSightProject/project.md)
- [21-Day Implementation Roadmap](file:///Users/atharvdhup/FinSightProject/roadmap.md)
