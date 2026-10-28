-- FinSight AI: Database Initialization Script for local standalone PostgreSQL
-- Creates isolated databases and grants for all microservices

CREATE DATABASE portfolio_db;
CREATE DATABASE market_db;
CREATE DATABASE alert_db;
CREATE DATABASE research_db;

-- Connect to research_db and install pg_trgm for full-text search similarity
\c research_db;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Connect to portfolio_db and install uuid-ossp
\c portfolio_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

