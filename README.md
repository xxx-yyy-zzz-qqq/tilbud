# Tilbud — Danish Grocery Weekly Ads

**Disclaimer**: This project fetches data from a public API (`api.etilbudsavis.dk`). The API provider's terms of service may restrict commercial use or redistribution of the data. This project is for personal/educational use only. Users are responsible for complying with the API provider's terms.

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

## Search

The search uses PostgreSQL full-text search on offer headings.

### How It Works

1. **User input**: One or more words (e.g., "arla minimælk")
2. **Normalization**: Input is lowercased, punctuation stripped → `arla | minimælk`
3. **Matching**: OR logic — any offer matching **one or more** words appears in results
4. **Ranking**: Offers matching more words rank higher (`ts_rank_cd` score)
5. **Sorting**: Within same rank, sorted by price ascending (cheapest first)

### PostgreSQL Functions Used

| Function | Purpose |
|----------|---------|
| `to_tsvector('simple', text)` | Converts text to normalized tokens (lowercase, stripped) |
| `to_tsquery('simple', 'word1 \| word2')` | Converts search words to query format with OR operator |
| `@@` operator | Matches documents against query — returns true if any word matches |
| `ts_rank_cd()` | Computes rank score (0-1) based on term density — more matching words = higher rank |

### Example

```
Search: "arla minimælk"

Normalized: 'arla' | 'minimælk'

Results:
1. "Arla Minimælk 1L" — matches both words (rank: 0.6)
2. "Arla D-mælk" — matches "arla" (rank: 0.3)
3. "Minimælk 0.5L" — matches "minimælk" (rank: 0.3)
4. "Arla Cream Cheese" — matches "arla" (rank: 0.3)

Sorted by: rank DESC, price ASC (cheapest first within same rank)
```

### GIN Index

Searches use a **GIN index** (Generalized Inverted Index) on `to_tsvector('simple', heading_normalized)`. A GIN index maps each word to the documents containing it — like a book index at the back of a textbook. This makes `@@` queries O(1) per word lookup regardless of table size, instead of scanning every row like `ILIKE` would.

```sql
CREATE INDEX idx_offers_heading_search
    ON offers USING GIN(to_tsvector('simple', heading_normalized));
```

## Data Source

Public API: `api.etilbudsavis.dk/v2`

### Endpoints Used

| Endpoint | Purpose | Auth |
|----------|---------|------|
| `GET /v2/catalogs?dealer_ids={chain}` | Get catalogs for a chain | None |
| `GET /v2/offers?catalog_ids={catalog_id}` | Get offers for a catalog | None |

## Fetch Strategy

Data is fetched from the external API once on application startup. No automatic scheduling — user triggers re-fetch manually via the frontend "Hent igen" button.

### Trigger

| Trigger | Purpose |
|---------|---------|
| Application startup | Load initial data |
| Manual re-fetch | `POST /api/v1/ingestion/trigger` (frontend button) |

### Delete Strategy

Per-chain delete-all-then-insert:
1. Delete ALL catalogs for a chain (via JdbcTemplate, bypasses Hibernate)
2. Fetch latest catalogs from API
3. Create catalogs + insert offers only for non-empty results
4. ON DELETE CASCADE handles offer cleanup

### Parallelism

All chains are fetched in parallel using virtual threads (one per chain, semaphore limits concurrent API calls).

### Resilience

- **HTTP timeouts**: Connect 5s, Read 15s — no chain hangs forever
- **Retry**: 3 attempts with exponential backoff (1s, 2s, 4s)
- **Per-catalog error handling**: One failed catalog doesn't kill the whole chain

## Observability

### Health Checks

Actuator exposes `/actuator/health` with database connectivity status.

### Metrics (Prometheus)

| Metric | Type | Description |
|--------|------|-------------|
| `etilbudsavis_fetch_duration_seconds` | Histogram | Time to fetch one chain |
| `etilbudsavis_offers_fetched_total` | Counter | Total offers fetched |
| `etilbudsavis_offers_inserted_total` | Counter | New offers inserted |
| `etilbudsavis_fetch_errors_total` | Counter | Fetch errors |
| `etilbudsavis_fetch_in_progress` | Gauge | 1 if fetch running |

Histogram buckets: `[30, 60, 120, 300, 600]` seconds

### Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/v1/ingestion/trigger` | POST | Trigger manual re-fetch |
| `/api/v1/ingestion/status` | GET | Get fetch status and last run results |
| `/actuator/health` | GET | Health check |
| `/actuator/prometheus` | GET | Prometheus metrics |

## API Response Shape

### Offer

```json
{
  "id": "CaNj3vyaLsBGqHPDZrcre",
  "heading": "Yoggi Yoghurt",
  "description": "Flere varianter. 1 kg. Max. 4 stk. pr. kunde pr. dag. Herefter er prisen 20,95 pr. stk. MAX. 4 STK. SKARP PRIS",
  "catalog_page": 1,
  "pricing": {
    "price": 9,                    // price in kr (integer)
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

- **Backend**: Java 25, Spring Boot 4.1.0, Spring MVC, Virtual Threads
- **Build**: Gradle 9.7.1 (Kotlin DSL)
- **Database**: PostgreSQL 16, Flyway migrations
- **Frontend**: React 19, TypeScript, Vite
- **Docker**: Docker Compose
- **Testing**: JUnit 5, H2, MockMvc
- **Observability**: Micrometer, Prometheus, Resilience4j
- **CI/CD**: GitHub Actions, GitHub Container Registry
- **API Collection**: Bruno

## Local Development

### Prerequisites

- Docker & Docker Compose
- Java 25 (for local development without Docker)
- Node.js 24 (for frontend development without Docker)

### Quick Start

```bash
# Clone the repo
git clone https://github.com/xxx-yyy-zzz-qqq/tilbud.git
cd tilbud

# Copy environment variables
cp .env.example .env

# Start all services
./run.sh
```

This starts:
- **PostgreSQL** on `localhost:5433`
- **Spring Boot backend** on `localhost:8080`
- **React frontend** on `localhost:5173`

### Development Commands

```bash
# Start all services
docker compose up

# Start in background
docker compose up -d

# Stop all services
docker compose down

# View logs
docker compose logs -f backend
docker compose logs -f frontend

# Rebuild after changes
docker compose up --build

# Run database migrations manually
docker compose exec backend java -jar app.jar

# Access PostgreSQL
docker compose exec db psql -U tilbud -d tilbud
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
./gradlew bootRun

# Build JAR
./gradlew build

# Run tests
./gradlew test
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


