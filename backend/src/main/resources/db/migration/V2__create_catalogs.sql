CREATE TYPE catalog_type AS ENUM ('MAIN', 'FOOD', 'NONFOOD', 'SUPPLEMENT');

CREATE TABLE catalogs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    catalog_id VARCHAR(8) NOT NULL UNIQUE,
    chain_id UUID NOT NULL REFERENCES chains(id) ON DELETE CASCADE,
    label VARCHAR(255),
    catalog_type catalog_type,
    category_ids TEXT[],
    run_from TIMESTAMPTZ,
    run_till TIMESTAMPTZ,
    offer_count INTEGER,
    pdf_url VARCHAR(500),
    created_at TIMESTAMPTZ DEFAULT NOW()
);
