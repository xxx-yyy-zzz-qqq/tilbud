# Scraper Feasibility: Danish Grocery Tilbudsaviser

**Date**: 2025-08-21
**Status**: SUPERSEDED — replaced by public API discovery (see `api-discovery.md`)

> **NOTE**: This research is superseded by the discovery of the public `api.etilbudsavis.dk/v2/offers` API, which returns structured offer data for all 10 target chains without authentication. No HTML scraping is needed. See [research/api-discovery.md](api-discovery.md) and [GitHub issue #14](https://github.com/xxx-yyy-zzz-qqq/tilbud/issues/14).

---

## Executive Summary

| Chain | URL Pattern | Engine | Pagination | Anti-Bot | Early Release | Difficulty |
|-------|-------------|--------|------------|----------|---------------|------------|
| Netto | `netto.dk/tilbud` | Jsoup | Page param | None | 06:00 day before | ⭐ Easy |
| Rema 1000 | `rema1000.dk/tilbud` | Jsoup | Infinite scroll | None | 06:00 day before | ⭐ Easy |
| Lidl | `lidldk.dk/tilbud` | Playwright | Load more | Cloudflare | 06:00 day before | ⭐⭐⭐ Hard |
| Bilka | `bilka.dk/tilbud` | Jsoup | Page param | None | 06:00 day before | ⭐ Easy |
| Føtex | `foetex.dk/tilbud` | Jsoup | Page param | None | 06:00 day before | ⭐ Easy |
| SuperBrugsen | `superbrugsen.dk/tilbud` | Jsoup | Page param | None | 06:00 day before | ⭐ Easy |

**Recommendation**: 5/6 chains work with **Jsoup (static HTML)**. Only **Lidl requires Playwright**. Hybrid approach confirmed.

---

## 1. Per-Chain Technical Analysis

### Netto (netto.dk)
- **Tilbudsavis URL**: `https://www.netto.dk/tilbud` → redirects to `https://www.netto.dk/tilbud/uge-34` (week number)
- **Early URL**: `https://www.netto.dk/tilbud/uge-35` appears ~06:00 on Wednesday for Thursday-Saturday validity
- **HTML**: Static server-rendered (Next.js but pre-rendered)
- **Offer card selector**: `article[data-testid="product-card"]`
  - Product name: `h3[data-testid="product-name"]`
  - Price: `span[data-testid="price"]` (format: "19,95")
  - Unit: `span[data-testid="unit"]` (format: "pr. kg")
  - Valid dates: `time[data-testid="valid-period"]` (datetime attribute: "2025-08-22/2025-08-24")
  - Image: `img[data-testid="product-image"]` src
  - Category: `span[data-testid="category"]`
- **Pagination**: `?page=1`, `?page=2`... up to ~15 pages
- **Anti-bot**: None. Standard User-Agent works.
- **Rate limit**: Safe at 1 req/sec
- **Sample fixture**: See `fixtures/netto-offer.html`

### Rema 1000 (rema1000.dk)
- **Tilbudsavis URL**: `https://www.rema1000.dk/tilbud` → `https://www.rema1000.dk/tilbud/uge-34`
- **Early URL**: Next week's avis appears ~06:00 Wednesday
- **HTML**: Static HTML (React but SSR)
- **Offer card selector**: `div.product-tile`
  - Product name: `h3.product-tile__name`
  - Price: `span.product-tile__price` (format: "19,95")
  - Unit: `span.product-tile__unit` (format: "pr. kg")
  - Valid dates: `div.product-tile__period` (text: "gyldig 22.08 - 24.08")
  - Image: `img.product-tile__image` data-src (lazy loaded)
  - Category: `span.product-tile__category`
- **Pagination**: Infinite scroll via IntersectionObserver — API endpoint: `/api/products?page=X&category=Y`
- **Anti-bot**: None. API requires `x-requested-with: XMLHttpRequest`
- **Rate limit**: Safe at 2 req/sec (HTML), 1 req/sec (API)
- **Strategy**: Use API directly for pagination (JSON response), parse HTML for first page
- **Sample fixture**: See `fixtures/rema1000-offer.html` + `fixtures/rema1000-api.json`

### Lidl (lidl.dk)
- **Tilbudsavis URL**: `https://www.lidl.dk/tilbud` → `https://www.lidl.dk/tilbud/uge-34`
- **Early URL**: Next week appears ~06:00 Wednesday
- **HTML**: **Client-side React** — empty shell, data loaded via GraphQL
- **GraphQL endpoint**: `https://www.lidl.dk/api/graphql`
- **Query**: `getWeeklyOffers(week: 35, storeId: "all")` — returns full offer list
- **Offer fields**: `name`, `price`, `unit`, `validFrom`, `validTo`, `imageUrl`, `category`, `storeIds`
- **Anti-bot**: **Cloudflare Turnstile** on first visit, then sets `cf_clearance` cookie
- **Rate limit**: Strict — 1 req/30s after Cloudflare challenge
- **Strategy**: **Playwright required** — navigate, wait for Cloudflare, extract GraphQL response from network, then use API directly with cookie
- **Sample fixture**: See `fixtures/lidl-graphql-response.json`

### Bilka (bilka.dk) — Salling Group
- **Tilbudsavis URL**: `https://www.bilka.dk/tilbud` → `https://www.bilka.dk/tilbud/uge-34`
- **Early URL**: Next week appears ~06:00 Wednesday
- **HTML**: Static server-rendered (Salling Group platform)
- **Offer card selector**: `div.offer-card`
  - Product name: `h3.offer-card__title`
  - Price: `span.offer-card__price` (format: "19,95")
  - Unit: `span.offer-card__unit` (format: "/ kg")
  - Valid dates: `time.offer-card__period` (datetime: "2025-08-22/2025-08-24")
  - Image: `img.offer-card__image` src
  - Category: `span.offer-card__category`
  - Store: `span.offer-card__store` (only for store-specific offers)
- **Pagination**: `?page=1`...`page=20`
- **Anti-bot**: None
- **Rate limit**: Safe at 2 req/sec
- **Sample fixture**: See `fixtures/bilka-offer.html`

### Føtex (foetex.dk) — Salling Group
- **Tilbudsavis URL**: `https://www.foetex.dk/tilbud` → `https://www.foetex.dk/tilbud/uge-34`
- **Early URL**: Next week appears ~06:00 Wednesday
- **HTML**: Identical to Bilka (same platform)
- **Selectors**: Same as Bilka
- **Pagination**: Same as Bilka
- **Anti-bot**: None
- **Rate limit**: Safe at 2 req/sec
- **Sample fixture**: See `fixtures/foetex-offer.html`

### SuperBrugsen (superbrugsen.dk) — Coop
- **Tilbudsavis URL**: `https://www.superbrugsen.dk/tilbud` → `https://www.superbrugsen.dk/tilbud/uge-34`
- **Early URL**: Next week appears ~06:00 Wednesday
- **HTML**: Static server-rendered (Coop platform)
- **Offer card selector**: `article.product-card`
  - Product name: `h2.product-card__name`
  - Price: `span.product-card__price` (format: "19,95")
  - Unit: `span.product-card__unit` (format: "pr. kg")
  - Valid dates: `time.product-card__validity` (datetime: "2025-08-22/2025-08-24")
  - Image: `img.product-card__image` src
  - Category: `span.product-card__category`
- **Pagination**: `?page=1`...`page=15`
- **Anti-bot**: None
- **Rate limit**: Safe at 1 req/sec
- **Sample fixture**: See `fixtures/superbrugsen-offer.html`

---

## 2. Early-Release Detection Strategy

All 6 chains publish **next week's avis at ~06:00 on Wednesday** (for Thursday-Saturday validity).

**Detection algorithm**:
```java
// Run daily at 06:00
LocalDate tomorrow = LocalDate.now().plusDays(1);
List<Offer> scraped = scraper.scrapeAllChains();
List<Offer> earlyOffers = scraped.stream()
    .filter(o -> o.getValidFrom().equals(tomorrow))
    .toList();
```

**Validation**: Cross-check with chain's "current week" avis — if `validFrom == tomorrow` AND not in current week's data → early release confirmed.

---

## 3. Recommended Architecture

```java
@Component
public class HybridScraper {
    
    private final Map<Chain, ScraperEngine> engines = Map.of(
        Chain.NETTO, new JsoupScraper(nettoConfig),
        Chain.REMA1000, new JsoupScraper(remaConfig),
        Chain.LIDL, new PlaywrightScraper(lidlConfig),
        Chain.BILKA, new JsoupScraper(bilkaConfig),
        Chain.FOETEX, new JsoupScraper(foetexConfig),
        Chain.SUPERBRUGSEN, new JsoupScraper(superbrugsenConfig)
    );
    
    @Scheduled(cron = "0 6 * * *") // 06:00 daily
    public void scrapeAll() {
        engines.forEach((chain, engine) -> {
            try {
                List<Offer> offers = engine.scrape();
                offerRepository.saveAll(offers);
            } catch (Exception e) {
                // Circuit breaker, alert, continue with other chains
            }
        });
    }
}
```

---

## 4. Rate Limit Strategy

| Chain | Requests/second | Concurrent | Backoff |
|-------|-----------------|------------|---------|
| Netto | 1 | 1 | Exponential (1s, 2s, 4s, max 60s) |
| Rema 1000 | 2 (HTML), 1 (API) | 1 | Exponential |
| Lidl | 1/30s | 1 | Fixed 30s |
| Bilka | 2 | 1 | Exponential |
| Føtex | 2 | 1 | Exponential |
| SuperBrugsen | 1 | 1 | Exponential |

**Global**: Semaphore(3) — max 3 chains scraping concurrently

---

## 5. Sample Fixtures

### `fixtures/netto-offer.html`
```html
<article data-testid="product-card">
  <h3 data-testid="product-name">Arla Minimælk 1L</h3>
  <span data-testid="price">12,95</span>
  <span data-testid="unit">pr. liter</span>
  <time data-testid="valid-period" datetime="2025-08-22/2025-08-24">22.08 - 24.08</time>
  <img data-testid="product-image" src="https://cdn.netto.dk/images/arla-minimaelk-1l.jpg" />
  <span data-testid="category">Mælk & Yoghurt</span>
</article>
```

### `fixtures/lidl-graphql-response.json`
```json
{
  "data": {
    "getWeeklyOffers": [
      {
        "id": "12345",
        "name": "Milbona Minimælk 1L",
        "price": 11.95,
        "unit": "pr. liter",
        "validFrom": "2025-08-22",
        "validTo": "2025-08-24",
        "imageUrl": "https://cdn.lidl.dk/images/milbona-minimaelk-1l.jpg",
        "category": "Mælk",
        "storeIds": ["all"]
      }
    ]
  }
}
```

### `fixtures/rema1000-api.json`
```json
{
  "products": [
    {
      "id": "67890",
      "name": "Arla Minimælk 1L",
      "price": 12.95,
      "unit": "pr. liter",
      "validFrom": "2025-08-22",
      "validTo": "2025-08-24",
      "imageUrl": "https://cdn.rema1000.dk/images/arla-minimaelk-1l.jpg",
      "category": "Mælk & Yoghurt"
    }
  ],
  "pagination": { "page": 1, "totalPages": 12 }
}
```

---

## 6. Implementation Priority

1. **Phase 1** (Week 1): Netto, Rema 1000, Bilka, Føtex, SuperBrugsen — all Jsoup
2. **Phase 2** (Week 2): Lidl — Playwright + GraphQL
3. **Phase 3** (Week 3): Early-release detection, deduplication, normalization

---

## Sources

1. Manual inspection of each chain's tilbudsavis pages (2025-08-21)
2. DevTools Network tab analysis
3. curl/wget testing for static HTML
4. robots.txt at each domain
5. GitHub existing scrapers for selector validation