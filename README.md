# Tilbud — Danish Grocery Weekly Ads

Search engine for Danish grocery store weekly offers (tilbudsaviser).

## Architecture

```
┌─────────────┐     ┌──────────────┐     ┌──────────────┐
│   React UI  │────▶│  Spring Boot │────▶│  PostgreSQL  │
│  (Search)   │     │   (API)      │     │   (Data)     │
└─────────────┘     └──────┬───────┘     └──────────────┘
                           │
                    ┌──────▼───────┐
                    │  etilbudsavis │
                    │  .dk API     │
                    └──────────────┘
```

## Data Source

Public API: `api.etilbudsavis.dk/v2`

### Endpoints Used

| Endpoint | Purpose | Auth |
|----------|---------|------|
| `GET /v2/catalogs?dealer_ids={chain}` | Get catalogs for a chain | None |
| `GET /v2/offers?catalog_ids={catalog_id}` | Get offers for a catalog | None |
| `GET /v2/offers?dealer_ids={chain}` | Get ALL offers (superset) | None |

### Key Findings

- **Use `catalog_ids` filter** for weekly offers (not `dealer_ids`)
- `dealer_ids` returns all active offers including previous weeks and permanent price drops
- `catalog_ids` returns offers for a specific catalog/week only
- API matches PDF flyers at 94-100% across all chains
- Catalog IDs are random 8-character strings (not sequential)

## Target Chains

| Chain | Dealer ID | Food | Nonfood | Supplement |
|-------|-----------|------|---------|------------|
| Netto | `9ba51` | Yes | Yes | No |
| REMA 1000 | `11deC` | Yes | No | Indstik |
| Lidl | `71c90` | Yes | No | Weekendavis |
| Bilka | `93f13` | Yes | Yes | No |
| Føtex | `bdf5A` | Yes | No | No |
| SuperBrugsen | `0b1e8` | Yes | No | No |
| Kvickly | `c1edq` | Yes | No | No |
| 365discount | `DWZE1w` | Yes | No | No |
| MENY | `267e1m` | Yes | No | No |
| SPAR | `88ddE` | Yes | No | No |

## Data Model (Draft)

### chains

```sql
CREATE TABLE chains (
    id VARCHAR(5) PRIMARY KEY,          -- dealer_id, e.g., "9ba51"
    name VARCHAR(100) NOT NULL,         -- e.g., "Netto"
    website VARCHAR(255),
    logo_url VARCHAR(500),
    color VARCHAR(6),                   -- hex color, e.g., "FFD700"
    country VARCHAR(2) DEFAULT 'DK',
    created_at TIMESTAMPTZ DEFAULT NOW()
);
```

### catalogs

```sql
CREATE TYPE catalog_type AS ENUM ('MAIN', 'FOOD', 'NONFOOD', 'SUPPLEMENT');

CREATE TABLE catalogs (
    id VARCHAR(8) PRIMARY KEY,          -- random string, e.g., "rZPphMb7"
    chain_id VARCHAR(5) NOT NULL,       -- FK to chains
    label VARCHAR(255),                 -- e.g., "Uge 35"
    catalog_type catalog_type,
    run_from TIMESTAMPTZ,
    run_till TIMESTAMPTZ,
    offer_count INTEGER,                -- claimed count from API
    category_ids TEXT[],                -- e.g., ["groceries_discount"]
    created_at TIMESTAMPTZ DEFAULT NOW(),
    FOREIGN KEY (chain_id) REFERENCES chains(id)
);
```

### offers

```sql
CREATE TABLE offers (
    id VARCHAR(30) PRIMARY KEY,         -- API offer ID
    chain_id VARCHAR(5) NOT NULL,       -- FK to chains
    catalog_id VARCHAR(8) NOT NULL,     -- FK to catalogs
    heading VARCHAR(255) NOT NULL,      -- product name
    description TEXT,                   -- full description
    price INTEGER NOT NULL,             -- price in øre (integer)
    pre_price INTEGER,                  -- original price in øre (nullable)
    currency VARCHAR(3) DEFAULT 'DKK',
    catalog_page INTEGER,               -- page number in flyer
    run_from TIMESTAMPTZ,
    run_till TIMESTAMPTZ,
    image_url VARCHAR(500),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    FOREIGN KEY (chain_id) REFERENCES chains(id),
    FOREIGN KEY (catalog_id) REFERENCES catalogs(id)
);
```

### searches (future)

```sql
CREATE TABLE searches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    query JSONB NOT NULL,               -- {"chains": [...], "categories": [...], "max_price": 50}
    name VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
```

## Fetch Strategy

1. **Daily at 06:00**: Check for new catalogs
   - `GET /v2/catalogs?dealer_ids={chain}`
   - Compare `run_from` with last fetched
   - If new catalog exists, proceed to step 2

2. **Fetch offers per catalog**:
   - `GET /v2/offers?catalog_ids={catalog_id}&limit=100`
   - Paginate with `offset` until empty response
   - Store in `offers` table

3. **Cleanup** (optional):
   - Archive offers where `run_till < NOW()`
   - Keep for historical analysis

## API Response Shape

### Offer

```json
{
  "id": "CaNj3vyaLsBGqHPDZrcre",
  "heading": "Yoggi Yoghurt",
  "description": "Flere varianter. 1 kg. Max. 4 stk. pr. kunde pr. dag. Herefter er prisen 20,95 pr. stk. MAX. 4 STK. SKARP PRIS",
  "catalog_page": 1,
  "pricing": {
    "price": 9,                    // price in øre
    "pre_price": 20.95,           // original price (nullable)
    "currency": "DKK"
  },
  "quantity": {
    "unit": { "symbol": "kg" },
    "size": { "from": 1, "to": 1 },
    "pieces": { "from": 1, "to": 1, "max": 4 }
  },
  "run_from": "2026-08-20T22:00:00+0000",
  "run_till": "2026-08-27T21:59:59+0000",
  "catalog_id": "Xotzu3h_",
  "dealer_id": "88ddE",
  "images": {
    "thumb": "https://...",
    "view": "https://...",
    "zoom": "https://..."
  }
}
```

### Catalog

```json
{
  "id": "rZPphMb7",
  "label": "Uge 35",
  "dealer_id": "11deC",
  "run_from": "2026-08-22T22:00:00+0000",
  "run_till": "2026-08-29T21:59:59+0000",
  "offer_count": 107,
  "category_ids": ["groceries_discount"]
}
```

## Tech Stack

- **Backend**: Java 21, Spring Boot 3.x, Spring MVC, Virtual Threads
- **Database**: PostgreSQL 16
- **Frontend**: React 19, TypeScript, Vite
- **Build**: Maven, Docker Compose
- **Testing**: JUnit 5, Testcontainers, Playwright

## Research

See `research/` directory for API discovery, legal review, and feasibility studies.

Key files:
- `research/api-discovery.md` — Full API documentation and offer completeness analysis
- `research/legal-tos-review.md` — Legal/ToS research findings
- `research/scraper-feasibility.md` — Superseded by public API

## GitHub Issues

- #1: Wayfinder Map
- #4: Data model
- #5: Search API contract
- #6: Frontend search UX
- #7: Category normalization
- #9: Scheduler & observability
- #10: Local dev environment
- #11: CI/CD pipeline
- #14: API research (closed with findings)
