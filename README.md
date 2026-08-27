# Tilbudsaviser

**Disclaimer**: This project fetches data from the public `api.etilbudsavis.dk/v2` API, operated by [Tjek A/S](https://tjek.com) (Denmark). Tjek's published [Terms and Conditions](https://tjek.com/terms) (Section 8) restrict API use to a Customer's own platforms and prohibit third-party systematic fetching. However, those terms appear to govern commercial Customers with signed agreements — the public, unauthenticated API endpoint used here is not explicitly covered. No separate ToS exists for the public endpoint. This project is for personal/educational use only. Users are responsible for their own compliance.


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

## Quick Start

```bash
# Clone the repo
git clone https://github.com/xxx-yyy-zzz-qqq/tilbud.git
cd tilbud

# Start all services
./run.sh
```

This starts:
- **PostgreSQL** on `localhost:5433`
- **Spring Boot backend** on `localhost:8080`
- **React frontend** on `localhost:5173`

`.env.example` is committed with default values. `run.sh` copies it to `.env` automatically on first run. Your local `.env` is gitignored — edit it to override defaults (e.g., different ports or credentials).

## Frontend

React SPA with Tailwind CSS and DaisyUI. Two main pages:

- **Landing page** (`/`) — chain table with logos, catalog count, offer count. Date filter narrows to chains with offers valid on selected date. Search navigates to results page.
- **Search page** (`/search?q=...&date=...`) — offer table with image, heading, price, validity, chain. Sortable columns, client-side date filtering, offer deduplication.

## Data Source

Public API: `api.etilbudsavis.dk/v2`

### Endpoints Used

| Endpoint | Purpose | Auth |
|----------|---------|------|
| `GET /v2/dealers` | Get all dealers (chains) | None |
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

All chains are fetched in parallel using virtual threads. Each chain gets its own virtual thread via `Executors.newVirtualThreadPerTaskExecutor()`. A semaphore limits concurrent API calls to avoid overwhelming the external API (which enforces HTTP/2 stream limits). The concurrency level is configurable via `ingestion.fetch-concurrency` (default:10). HikariCP connection pool is sized to match (default:15).

### Resilience

- **HTTP timeouts**: Connect 5s, Read 15s — no chain hangs forever
- **Retry**: 3 attempts with exponential backoff (1s, 2s, 4s)
- **⚠️ Silent failures**: If a chain's catalog or offer fetch fails after retries, the error is logged in the backend but **not surfaced in the frontend**. The chain simply appears with 0 offers. Check backend logs to diagnose.

## Search

The search uses PostgreSQL full-text search on offer headings.

### How It Works

1. **User input**: One or more words (e.g., "arla minimælk")
2. **Normalization**: Input is lowercased, punctuation stripped → `arla | minimælk`
3. **Matching**: OR logic — any offer matching **one or more** words appears in results
4. **Sorting**: Results sorted by price ascending (cheapest first)

### PostgreSQL Functions Used

| Function | Purpose |
|----------|---------|
| `to_tsvector('simple', text)` | Converts text to normalized tokens (lowercase, stripped) |
| `to_tsquery('simple', 'word1 \| word2')` | Converts search words to query format with OR operator |
| `@@` operator | Matches documents against query — returns true if any word matches |

### Example

```
Search: "arla minimælk"

Normalized: 'arla' | 'minimælk'

Results:
1. "Arla D-mælk" — matches "arla"
2. "Arla Cream Cheese" — matches "arla"
3. "Minimælk 0.5L" — matches "minimælk"
4. "Arla Minimælk 1L" — matches both words

Sorted by: price ASC (cheapest first)
```

### GIN Index

Searches use a **GIN index** (Generalized Inverted Index) on `to_tsvector('simple', heading_normalized)`. A GIN index maps each word to the documents containing it — like a book index at the back of a textbook. This makes `@@` queries O(1) per word lookup regardless of table size, instead of scanning every row like `ILIKE` would.

```sql
CREATE INDEX idx_offers_heading_search
    ON offers USING GIN(to_tsvector('simple', heading_normalized));
```

## Observability

### Health Checks

Actuator exposes `/actuator/health` with database connectivity status.

### Metrics (Prometheus)

| Metric | Type | Description |
|--------|------|-------------|
| `etilbudsavis_fetch_duration_seconds` | Timer | Time to fetch all chains |
| `etilbudsavis_offers_fetched_total` | Counter | Total offers fetched |
| `etilbudsavis_offers_inserted_total` | Counter | New offers inserted |
| `etilbudsavis_fetch_errors_total` | Counter | Fetch errors |

### Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/v1/ingestion/trigger` | POST | Trigger manual re-fetch |
| `/api/v1/ingestion/status` | GET | Get fetch status and last run results |
| `/actuator/health` | GET | Health check |
| `/actuator/prometheus` | GET | Prometheus metrics |

## External API Shape

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
- **Frontend**: React 19, TypeScript, Vite, Tailwind CSS v4, DaisyUI
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

