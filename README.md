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
- **All 10 chains have catalogs** (Føtex was incorrectly listed as having no catalogs)
- Always filter by `dealer_ids` when querying catalogs — global endpoint caps at 1000

### API vs PDF Comparison

Comparison of API offers (filtered by `catalog_ids`) against actual PDF flyers:

| Chain | Catalog | Type | API | Claimed | Match | % | Notes |
|-------|---------|------|-----|---------|-------|---|-------|
| Netto | uge 35 Nonfood | NONFOOD | 49 | 49 | 49 | **100%** | |
| Netto | uge 35 | MAIN | 201 | 205 | 201 | **100%** | |
| REMA 1000 | Uge 35 | MAIN | 107 | 107 | 107 | **100%** | |
| REMA 1000 | Uge 34 Indstik | SUPPLEMENT | 14 | 14 | 14 | **100%** | |
| Lidl | avis (uge 35) | MAIN | 224 | 225 | 221 | **99%** | 3 missing (banan, strygejern, røgalarm) |
| Lidl | Weekendavis (uge 34) | SUPPLEMENT | 208 | 210 | 205 | **99%** | 3 missing |
| Bilka | Nonfood Uge 35 | NONFOOD | 281 | 294 | 264 | **94%** | 17 missing (lingeri, kølebokse) |
| Bilka | Food Uge 35 | FOOD | 202 | 208 | 195 | **97%** | 7 missing (spiritus, hårpleje) |
| Føtex | Uge 34/35 | MAIN | 320 | 331 | 316 | **99%** | 4 missing (nonfood: Oral-B, TV, Nilfisk) |
| SuperBrugsen | Uge 34 | MAIN | 149 | 150 | 148 | **99%** | 1 missing |
| Kvickly | Uge 34 | MAIN | 208 | 209 | 207 | **100%** | 1 missing |
| 365discount | Uge 34 | MAIN | 143 | 145 | 143 | **100%** | |
| MENY | uge 35 | MAIN | 125 | 126 | 124 | **99%** | 1 missing |
| SPAR | uge 35 | MAIN | 84 | 84 | 83 | **99%** | 1 missing |

**Summary**: 94-100% match across all chains. API is the source of truth.

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

- **Backend**: Java 25, Spring Boot 4.0, Spring MVC, Virtual Threads
- **Database**: PostgreSQL 16, Flyway migrations
- **Frontend**: React 19, TypeScript, Vite
- **Build**: Maven, Docker Compose
- **Testing**: JUnit 5, Testcontainers

## Local Development

### Prerequisites

- Docker & Docker Compose
- Java 25 (for local development without Docker)
- Node.js 20+ (for frontend development without Docker)

### Quick Start

```bash
# Clone the repo
git clone https://github.com/xxx-yyy-zzz-qqq/tilbud.git
cd tilbud

# Copy environment variables
cp .env.example .env

# Start all services
./dev.sh
```

This starts:
- **PostgreSQL** on `localhost:5432`
- **Spring Boot backend** on `localhost:8080`
- **React frontend** on `localhost:5173`

### Development Commands

```bash
# Start all services
docker-compose up

# Start in background
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f backend
docker-compose logs -f frontend

# Rebuild after changes
docker-compose up --build

# Run database migrations manually
docker-compose exec backend java -jar app.jar

# Access PostgreSQL
docker-compose exec db psql -U tilbud -d tilbud
```

### Frontend Development

```bash
cd frontend

# Install dependencies
npm install

# Start dev server (with hot reload)
npm run dev

# Build for production
npm run build

# Run tests
npm test
```

### Backend Development

```bash
cd backend

# Run Spring Boot locally
./mvnw spring-boot:run

# Build JAR
./mvnw clean package

# Run tests
./mvnw test
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `POSTGRES_DB` | `tilbud` | Database name |
| `POSTGRES_USER` | `tilbud` | Database user |
| `POSTGRES_PASSWORD` | `tilbud` | Database password |
| `API_BASE_URL` | `https://api.etilbudsavis.dk/v2` | External API URL |
| `VITE_API_URL` | `http://localhost:8080` | Backend API URL for frontend |

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
