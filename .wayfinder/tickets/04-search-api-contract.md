# Ticket: Search API contract — query params, filters, pagination, response shape

**Labels**: `wayfinder:prototype`

## Question

Define the REST API contract (OpenAPI 3.0 spec) for offer search:
1. **GET /api/offers/search**
   - Query params: `q` (full-text), `chain` (slug[]), `store_id`, `category`, `subcategory`, `min_price`, `max_price`, `valid_from`, `valid_to`, `sort` (price_asc, price_desc, date_asc, date_desc, relevance), `page`, `size`
   - Response: `{ content: Offer[], totalElements, totalPages, number, size }`
2. **GET /api/offers/{id}** — single offer detail
3. **GET /api/chains** — list chains with store counts
4. **GET /api/stores** — list stores with filter by chain, city, lat/lng radius
5. **GET /api/categories** — distinct categories/subcategories

**Deliverable**: OpenAPI spec (YAML), Spring Boot controller stubs with validation, and a Postman/Bruno collection. Push to `prototype/search-api` branch. Link from this ticket.