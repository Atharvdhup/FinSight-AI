-- ==============================================================================
-- Migration: V1__init_market_schema.sql
-- Description: Initialize schema for Market Data Service (stocks, quotes, historical prices)
-- ==============================================================================

-- 1. Stocks Table: Master table for all tracked securities
CREATE TABLE stocks (
    symbol VARCHAR(12) PRIMARY KEY,
    company_name VARCHAR(150) NOT NULL,
    sector VARCHAR(50) NOT NULL,
    industry VARCHAR(100),
    currency VARCHAR(3) DEFAULT 'USD',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index for searching active stocks by sector
CREATE INDEX idx_stocks_active_sector ON stocks(is_active, sector);

-- 2. Stock Quotes Table: Current market price snapshot per stock (1:1 with stocks)
CREATE TABLE stock_quotes (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(12) NOT NULL REFERENCES stocks(symbol) ON DELETE CASCADE,
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

-- 3. Historical Prices Table: Daily OHLCV candles
CREATE TABLE historical_prices (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(12) NOT NULL REFERENCES stocks(symbol) ON DELETE CASCADE,
    price_date DATE NOT NULL,
    open_price NUMERIC(19, 4) NOT NULL,
    high_price NUMERIC(19, 4) NOT NULL,
    low_price NUMERIC(19, 4) NOT NULL,
    close_price NUMERIC(19, 4) NOT NULL,
    volume BIGINT NOT NULL,
    CONSTRAINT uq_symbol_date UNIQUE (symbol, price_date)
);

-- Compound index for rapid chronological range queries per ticker
CREATE INDEX idx_hist_symbol_date ON historical_prices(symbol, price_date DESC);

