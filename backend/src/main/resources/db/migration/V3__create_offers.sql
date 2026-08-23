CREATE TABLE offers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    offer_id VARCHAR(30) NOT NULL UNIQUE,
    chain_id UUID NOT NULL REFERENCES chains(id) ON DELETE CASCADE,
    catalog_id UUID NOT NULL REFERENCES catalogs(id) ON DELETE CASCADE,
    heading VARCHAR(255) NOT NULL,
    description TEXT,
    price INTEGER NOT NULL,
    pre_price INTEGER,
    currency VARCHAR(3) DEFAULT 'DKK',
    catalog_page INTEGER,
    quantity JSONB,
    images JSONB,
    run_from TIMESTAMPTZ,
    run_till TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
