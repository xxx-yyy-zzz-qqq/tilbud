ALTER TABLE catalogs ALTER COLUMN catalog_type TYPE VARCHAR(20) USING catalog_type::VARCHAR;
DROP TYPE IF EXISTS catalog_type;
