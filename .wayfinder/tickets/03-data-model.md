# Ticket: Data model — PostgreSQL schema for offers, stores, chains, search, users

**Labels**: `wayfinder:prototype`

## Question

Design the PostgreSQL schema (SQL migration + JPA entities) covering:
1. **Chains**: id, name, slug, logo_url, website_url, scraper_config (JSONB)
2. **Stores**: id, chain_id, name, address, city, zip, lat, lng, store_code (chain's internal ID)
3. **Offers**: id, chain_id, store_id (nullable for chain-wide), product_name, normalized_name, brand, price, unit, quantity, valid_from, valid_to, category, subcategory, image_url, source_url, scraped_at, raw_data (JSONB)
4. **Search indexes**: pg_trgm on product_name/normalized_name, tsvector (danish) on product_name + brand + category, B-tree on valid_from, price, chain_id
5. **Users**: id, email, password_hash, created_at
6. **Subscriptions**: id, user_id, search_query (JSONB), chains[], max_price, categories[], notification_channels (email, webhook), frequency (instant, daily_digest), active
7. **Notification log**: id, subscription_id, offer_id, channel, sent_at, status

**Deliverable**: A prototype Spring Boot project with Flyway migrations, JPA entities, repositories, and a test data seeder. Push to `prototype/data-model` branch. Link from this ticket.