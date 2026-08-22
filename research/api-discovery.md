# Public API Discovery: etilbudsavis.dk

**Date**: 2026-08-22
**Status**: Research complete — API replaces scraping entirely

---

## Executive Summary

The `api.etilbudsavis.dk/v2/` API is **public, free, and requires no authentication**. It returns structured offer data for **all 10 target Danish grocery chains** plus hundreds of other retailers. This eliminates the entire scraper layer (Jsoup, Playwright, anti-bot evasion).

**Key finding**: Offers are published 1.6-3.5 days before they become valid — even better than the original "1 day before" goal.

---

## 1. API Endpoints

### Primary: `/v2/offers`
- **URL**: `https://api.etilbudsavis.dk/v2/offers`
- **Method**: GET
- **Auth**: None (public)
- **Pagination**: `?limit=100&offset=N` (max 100 per request)
- **Filtering**: `?dealer_ids=9ba51` (comma-separated dealer IDs)
- **Returns**: Array of offer objects

### Secondary: `/v2/dealers`
- **URL**: `https://api.etilbudsavis.dk/v2/dealers`
- **Method**: GET
- **Auth**: None (public)
- **Pagination**: `?limit=100&offset=N`
- **Returns**: Array of dealer (store chain) objects

### Secondary: `/v2/catalogs`
- **URL**: `https://api.etilbudsavis.dk/v2/catalogs`
- **Method**: GET
- **Auth**: None (public)
- **Pagination**: `?limit=100&offset=N`
- **Returns**: Array of catalog (flyer) objects

### Catalog metadata: `squid-api.tjek.com`
- **URL**: `https://squid-api.tjek.com/v2/catalogs/{catalog_id}`
- **Method**: GET
- **Auth**: None (public)
- **Returns**: Detailed catalog object with page count, offer count, PDF URL

---

## 2. Authentication & Rate Limits

- **Authentication**: None required. No API key, no tokens, no headers.
- **Rate limits**: None detected. Tested with 10 sequential requests at max speed — no 429, no CAPTCHA, no IP blocks.
- **Pagination limit**: Max 100 offers per request. Unfiltered endpoint caps at 1000 total. Must filter by `dealer_ids` to get all offers for a specific chain.

---

## 3. Data Structure

### Offer Object (from `/v2/offers`)

```json
{
  "id": "ROqfvMC7LipN68IFhhBFz",
  "ern": "ern:offer:ROqfvMC7LipN68IFhhBFz",
  "heading": "Coca-Cola eller Fanta sodavand",
  "description": "24x33 cl. + pant. Pr. liter 8,71 Max. 6 stk. pr. variant pr. kunde pr. dag til denne pris.",
  "catalog_page": 6,
  "pricing": {
    "price": 69,
    "pre_price": null,
    "currency": "DKK"
  },
  "quantity": {
    "unit": {
      "symbol": "cl",
      "si": { "symbol": "l", "factor": 0.01 }
    },
    "size": { "from": 33, "to": 33 },
    "pieces": { "from": 24, "to": 24, "min": null, "max": 6 }
  },
  "images": {
    "thumb": "https://image-transformer-api.tjek.com/...",
    "view": "https://image-transformer-api.tjek.com/...",
    "zoom": "https://image-transformer-api.tjek.com/..."
  },
  "links": { "webshop": null },
  "run_from": "2026-08-21T22:00:00+0000",
  "run_till": "2026-08-28T21:59:59+0000",
  "publish": "2026-08-19T05:00:00+0000",
  "dealer_id": "9ba51",
  "dealer": {
    "id": "9ba51",
    "name": "Netto",
    "website": "https://netto.dk/",
    "logo": "https://image-transformer-api.tjek.com/...",
    "color": "FFD950"
  },
  "catalog_id": "RPXhXwQH",
  "branding": {
    "name": "Netto",
    "logo": "https://image-transformer-api.tjek.com/..."
  }
}
```

### Field Mapping

| API Field | Type | Description | DB Column |
|-----------|------|-------------|-----------|
| `id` | string | Unique offer ID | `api_offer_id` |
| `heading` | string | Product name | `product_name` |
| `description` | string | Detailed description | `description` |
| `pricing.price` | integer | Price in øre (69 = 0.69 DKK) | `price` |
| `pricing.pre_price` | integer | Original price in øre (nullable) | `original_price` |
| `pricing.currency` | string | "DKK" | `currency` |
| `run_from` | datetime | Valid from | `valid_from` |
| `run_till` | datetime | Valid to | `valid_to` |
| `publish` | datetime | Published at (early access) | `published_at` |
| `dealer_id` | string | Chain ID | `chain_id` |
| `catalog_id` | string | Flyer ID | `catalog_id` |
| `catalog_page` | integer | Page number in flyer | `catalog_page` |
| `quantity.unit.symbol` | string | Unit (cl, g, pcs, etc.) | `unit` |
| `quantity.size.from` | integer | Quantity from | `quantity_from` |
| `quantity.size.to` | integer | Quantity to | `quantity_to` |
| `quantity.pieces.from` | integer | Pieces from | `pieces_from` |
| `quantity.pieces.to` | integer | Pieces to | `pieces_to` |
| `images.thumb` | string | Thumbnail URL | `image_urls->thumb` |
| `images.view` | string | View URL | `image_urls->view` |
| `images.zoom` | string | Zoom URL | `image_urls->zoom` |
| `links.webshop` | string | Webshop URL (nullable) | `webshop_url` |

### Catalog Object (from `squid-api.tjek.com`)

```json
{
  "id": "RPXhXwQH",
  "label": "Netto uge 35",
  "page_count": 33,
  "offer_count": 205,
  "run_from": "2026-08-21T22:00:00+0000",
  "run_till": "2026-08-28T21:59:59+0000",
  "publish": "2026-08-19T05:00:00+0000",
  "category_ids": ["groceries_discount"],
  "pdf_url": "https://squid-api.tjek.com/v2/catalogs/RPXhXwQH/download",
  "dealer_id": "9ba51",
  "dealer": { "name": "Netto" }
}
```

### Dealer Object (from `/v2/dealers`)

```json
{
  "id": "9ba51",
  "name": "Netto",
  "website": "https://netto.dk/",
  "description": "Netto har altid stærke tilbud...",
  "logo": "https://image-transformer-api.tjek.com/...",
  "color": "FFD950",
  "country": { "id": "DK" },
  "markets": [{ "slug": "Netto", "country_code": "DK" }]
}
```

---

## 4. Early Access Analysis

Offers are published before they become valid. The `publish` field shows when the offer was uploaded to the API, and `run_from` shows when it becomes valid.

| Chain | Dealer ID | Offers | Days Early |
|-------|-----------|--------|------------|
| Netto | 9ba51 | 202 | ~2.7 |
| REMA 1000 | 11deC | 145 | ~3.5 |
| Lidl | 71c90 | 168 | varies |
| Bilka | 93f13 | 72 | ~1.7 |
| Føtex | bdf5A | 93 | ~1.6 |
| SuperBrugsen | 0b1e8 | 100+ | ~2.0 |
| Kvickly | c1edq | 100+ | ~2.0 |
| 365discount | DWZE1w | 100+ | ~2.0 |
| MENY | 267e1m | 51 | ~2.0 |
| SPAR | 88ddE | 31 | ~2.0 |

**Average early access**: 1.6-3.5 days (better than original "1 day before" goal)

---

## 5. Target Chains (Denmark only)

| Chain | Dealer ID | Country | Offers |
|-------|-----------|---------|--------|
| Netto | 9ba51 | DK | 202 |
| REMA 1000 | 11deC | DK | 145 |
| Lidl | 71c90 | DK | 168 |
| Bilka | 93f13 | DK | 72 |
| Føtex | bdf5A | DK | 93 |
| SuperBrugsen | 0b1e8 | DK | 100+ |
| Kvickly | c1edq | DK | 100+ |
| 365discount | DWZE1w | DK | 100+ |
| MENY | 267e1m | DK | 51 |
| SPAR | 88ddE | DK | 31 |

**Note**: The API also returns offers from Norwegian (REMA 1000, KIWI, Meny, Coop Prix, etc.), Swedish (ICA, Willys, Hemköp, etc.), and Finnish (S-market, K-Market, Prisma, etc.) chains. Filter by `dealer_ids` to get only Danish chains.

---

## 6. API Limitations

1. **Max 100 offers per request** — must paginate with `limit=100&offset=N`
2. **Unfiltered endpoint caps at 1000** — must filter by `dealer_ids` to get all offers for a specific chain
3. **No store-level data** — offers are chain-wide, not per-store
4. **No per-offer category** — only catalog-level `category_ids` (e.g., `["groceries_discount"]`)
5. **No brand field** — must infer from product names
6. **No product ID/EAN** — only the API's own offer ID
7. **Offer counts may differ from catalog** — API returns all active offers (may include previous weeks), catalog `offer_count` is for a single catalog
8. **`dealer_ids` filter may exclude nonfood items** embedded in food catalogs (e.g., Netto pages 26-32)
9. **Some chains have no catalogs** in the catalogs endpoint (e.g., Føtex), but offers are still available

---

## 7. Implementation Strategy

### Fetching offers
```java
@Component
public class TilbudsavisApi {
    
    private final HttpClient httpClient;
    private final String baseUrl = "https://api.etilbudsavis.dk/v2";
    
    public List<Offer> fetchOffersByDealer(String dealerId) {
        List<Offer> allOffers = new ArrayList<>();
        int offset = 0;
        while (true) {
            String url = baseUrl + "/offers?dealer_ids=" + dealerId + "&limit=100&offset=" + offset;
            List<Offer> page = httpClient.get(url, OfferListType);
            allOffers.addAll(page);
            if (page.size() < 100) break;
            offset += 100;
        }
        return allOffers;
    }
}
```

### Scheduling
```java
@Scheduled(cron = "0 6 * * *")
public void fetchOffers() {
    List<Chain> chains = chainRepository.findAll();
    chains.parallelStream().forEach(chain -> {
        List<Offer> offers = api.fetchOffersByDealer(chain.getId());
        offerRepository.saveAll(offers);
    });
}
```

---

## 8. Rate Limit Strategy

| Scenario | Requests | Time |
|----------|----------|------|
| 10 chains, sequential | ~100 (10 pages × 10 chains) | ~2 min |
| 10 chains, parallel (3 concurrent) | ~100 | ~30 sec |

**Safe rate**: No limits detected. Conservative: 1 req/sec per chain.

---

## 9. Offer Completeness Analysis

**Critical question**: Does the API return ALL offers from a flyer, or just a subset?

### Two Filters Available

| Filter | Returns | Use case |
|--------|---------|----------|
| `dealer_ids` | ALL active offers (current + old + permanent) | Full catalog |
| `catalog_ids` | Offers for a specific catalog/week only | Weekly flyers |

**Recommendation**: Use `catalog_ids` for weekly offers. Use `dealer_ids` to discover catalog IDs.

### How to Get Catalog IDs

**Catalog IDs are NOT sequential.** They are random 8-character strings (e.g., `rZPphMb7`, `Xotzu3h_`).

Two ways to get the latest catalog:
1. **From offers** (simplest): `GET /v2/offers?dealer_ids={chain}&limit=1` → response has `catalog_id`
2. **From catalogs endpoint**: `GET /v2/catalogs?dealer_ids={chain}` → sort by `run_from` descending

### Food vs Nonfood Differentiation

| Chain | Food | Nonfood | Supplement | Differentiates? |
|-------|------|---------|------------|-----------------|
| Netto | Yes (201) | Yes (49) | No | **YES** |
| Bilka | Yes (202) | Yes (281) | No | **YES** |
| REMA 1000 | Main (107) | - | Indstik (14) | Partial |
| Lidl | Main (224) | - | Weekendavis (208) | Partial |
| SuperBrugsen | Main (149) | - | No | No |
| Kvickly | Main (208) | - | No | No |
| 365discount | Main (143) | - | No | No |
| MENY | Main (125) | - | No | No |
| SPAR | Main (84) | - | No | No |
| Føtex | Main (320) | - | No | **Yes (was incorrectly listed as NO CATALOGS)** |

### Per-Catalog Comparison (catalog_ids filter)

| Chain | Catalog | Type | API | Claimed | Match | % |
|-------|---------|------|-----|---------|-------|---|
| Netto | uge 35 Nonfood | NONFOOD | 49 | 49 | 49 | **100%** |
| Netto | uge 35 | MAIN | 201 | 205 | 201 | **100%** |
| REMA 1000 | Uge 35 | MAIN | 107 | 107 | 107 | **100%** |
| REMA 1000 | Uge 34 Indstik | SUPPLEMENT | 14 | 14 | 14 | **100%** |
| Lidl | avis (uge 35) | MAIN | 224 | 225 | 221 | **99%** |
| Lidl | Weekendavis (uge 34) | SUPPLEMENT | 208 | 210 | 205 | **99%** |
| Bilka | Nonfood Uge 35 | NONFOOD | 281 | 294 | 264 | **94%** |
| Bilka | Food Uge 35 | FOOD | 202 | 208 | 195 | **97%** |
| SuperBrugsen | Uge 34 | MAIN | 149 | 150 | 148 | **99%** |
| Kvickly | Uge 34 | MAIN | 208 | 209 | 207 | **100%** |
| 365discount | Uge 34 | MAIN | 143 | 145 | 143 | **100%** |
| MENY | uge 35 | MAIN | 125 | 126 | 124 | **99%** |
| SPAR | uge 35 | MAIN | 84 | 84 | 83 | **99%** |

### Key Findings

1. **API matches PDF at 94-100%** for ALL catalogs when filtering by `catalog_ids`
2. **Only Netto and Bilka** differentiate between Food and Nonfood catalogs
3. **REMA 1000 and Lidl** have supplement inserts (Indstik/Weekendavis)
4. **ALL 10 chains have catalogs** — Føtex was incorrectly listed as having no catalogs
5. **Bilka Nonfood** has lowest match (94%) - 17 offers not found in PDF
6. **Always filter by `dealer_ids`** when querying catalogs — global endpoint caps at 1000

### Implementation Recommendation

Use `catalog_ids` filter to get offers per catalog:
1. Get latest catalog(s) for each chain: `GET /v2/catalogs?dealer_ids={chain}`
2. For each catalog, get offers: `GET /v2/offers?catalog_ids={catalog_id}`
3. Store catalog metadata (label, type, dates) alongside offers

### Implementation Implications

These findings directly impact the data model and scheduler design:

**Data Model (#4)**:
- Need `catalogs` table with `id`, `label`, `catalog_type`, `run_from`, `run_till`
- `catalog_type` enum: MAIN, FOOD, NONFOOD, SUPPLEMENT
- `catalog_id` in offers table must be nullable (Føtex has no catalogs)
- Price stored as INTEGER (øre) to avoid floating-point issues

**Scheduler (#9)**:
- Fetch order: catalogs first → offers per catalog
- Daily check for new catalogs (compare `run_from` with last fetch)
- Only fetch offers if new catalog detected
- Parallel fetch safe for catalogs (independent)
- Sequential for offers within a chain (pagination depends on previous)

**Search API (#5)**:
- Can filter by `catalog_type` (food/nonfood)
- Can filter by date range using `run_from`/`run_till`
- Can filter by chain using `chain_id`

See `README.md` for full design documentation.
---

## 10. Sources

1. `api.etilbudsavis.dk/v2/offers` — tested 2026-08-22
2. `api.etilbudsavis.dk/v2/dealers` — tested 2026-08-22
3. `api.etilbudsavis.dk/v2/catalogs` — tested 2026-08-22
4. `squid-api.tjek.com/v2/catalogs/{id}` — tested 2026-08-22
5. TilbudsTrolden MCP server (https://github.com/olgasafonova/tilbudstrolden-mcp) — confirms API usage without auth
6. Tjek JS SDK (https://github.com/tjek/tjek-js-sdk) — developer portal at eleaflet.com/developers/apps
