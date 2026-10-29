-- ==============================================================================
-- FinSight AI: Portfolio Service Baseline Schema Migration (V1)
-- Strict numeric precision NUMERIC(19,4) to guarantee zero IEEE-754 rounding errors
-- ==============================================================================

-- 1. Portfolios Table
CREATE TABLE portfolios (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    name VARCHAR(100) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    cash_balance NUMERIC(19, 4) NOT NULL DEFAULT 100000.0000,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_portfolios_user_id ON portfolios(user_id);

-- 2. Holdings Table (Positions in Portfolio)
CREATE TABLE holdings (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    symbol VARCHAR(12) NOT NULL,
    quantity NUMERIC(19, 4) NOT NULL CHECK (quantity >= 0),
    average_buy_price NUMERIC(19, 4) NOT NULL CHECK (average_buy_price >= 0),
    current_price NUMERIC(19, 4) NOT NULL CHECK (current_price >= 0),
    total_invested NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    current_value NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    unrealized_pnl NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    unrealized_pnl_pct NUMERIC(8, 4) NOT NULL DEFAULT 0.0000,
    sector VARCHAR(50),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_portfolio_symbol UNIQUE (portfolio_id, symbol)
);

CREATE INDEX idx_holdings_portfolio ON holdings(portfolio_id);
CREATE INDEX idx_holdings_symbol ON holdings(symbol);

-- 3. Transactions Table (Immutable Trade Audit Ledger)
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    idempotency_key VARCHAR(64) NOT NULL UNIQUE,
    symbol VARCHAR(12) NOT NULL,
    transaction_type VARCHAR(10) NOT NULL CHECK (transaction_type IN ('BUY', 'SELL')),
    quantity NUMERIC(19, 4) NOT NULL CHECK (quantity > 0),
    price NUMERIC(19, 4) NOT NULL CHECK (price > 0),
    total_amount NUMERIC(19, 4) NOT NULL CHECK (total_amount >= 0),
    fees NUMERIC(19, 4) NOT NULL DEFAULT 0.0000 CHECK (fees >= 0),
    executed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_portfolio ON transactions(portfolio_id);
CREATE INDEX idx_transactions_symbol ON transactions(symbol);
CREATE INDEX idx_transactions_executed_at ON transactions(portfolio_id, executed_at DESC);

