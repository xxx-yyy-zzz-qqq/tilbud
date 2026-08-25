DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'catalogs' AND column_name = 'category_ids'
        AND udt_name = '_text'
    ) THEN
        ALTER TABLE catalogs ALTER COLUMN category_ids TYPE TEXT USING category_ids::TEXT;
    END IF;
END $$;
