CREATE INDEX idx_chains_dealer_id ON chains(dealer_id);

CREATE INDEX idx_catalogs_chain_id ON catalogs(chain_id);
CREATE INDEX idx_catalogs_catalog_id ON catalogs(catalog_id);
CREATE INDEX idx_catalogs_run_from ON catalogs(run_from);
CREATE INDEX idx_catalogs_run_till ON catalogs(run_till);

CREATE INDEX idx_offers_chain_id ON offers(chain_id);
CREATE INDEX idx_offers_catalog_id ON offers(catalog_id);
CREATE INDEX idx_offers_price ON offers(price);
CREATE INDEX idx_offers_run_from ON offers(run_from);
CREATE INDEX idx_offers_run_till ON offers(run_till);
CREATE INDEX idx_offers_search ON offers USING GIN(to_tsvector('simple', heading || ' ' || COALESCE(description, '')));
