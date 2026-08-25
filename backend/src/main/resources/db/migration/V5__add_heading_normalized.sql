-- Add normalized heading column for full-text search
ALTER TABLE offers ADD COLUMN heading_normalized VARCHAR(255);

-- Populate from existing data: lowercase + strip non-alphanumeric (keep æøå)
UPDATE offers SET heading_normalized = LOWER(
    REGEXP_REPLACE(heading, '[^a-zA-Z0-9æøåÆØÅ]+', ' ', 'g')
);

-- Drop old GIN index (covered heading + description, not needed)
DROP INDEX IF EXISTS idx_offers_search;

-- New GIN index on normalized heading only
CREATE INDEX idx_offers_heading_search
    ON offers USING GIN(to_tsvector('simple', heading_normalized));
