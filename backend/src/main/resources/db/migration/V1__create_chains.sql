CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE chains (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dealer_id VARCHAR(5) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    website VARCHAR(255),
    logo_url VARCHAR(500),
    color VARCHAR(6),
    country VARCHAR(2) DEFAULT 'DK',
    created_at TIMESTAMPTZ DEFAULT NOW()
);
