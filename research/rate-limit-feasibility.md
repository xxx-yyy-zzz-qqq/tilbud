# Rate Limit Feasibility: Danish Grocery Tilbudsaviser

**Date**: 2026-08-22
**Status**: Research complete — no rate limiting detected

---

## Correct Tilbudsavis URLs

| Chain | URL | HTTP | Time |
|-------|-----|------|------|
| Netto | `https://netto.dk/netto-avisen/` | 200 | 0.18s |
| Bilka | `https://www.bilka.dk/bilkaavisen/` | 200 | 0.18s |
| Føtex | `https://www.foetex.dk/foetex-avis/` | 200 | 0.10s |
| SuperBrugsen | `https://superbrugsen.coop.dk/avis/` | 200 | 0.09s |
| Rema 1000 | `https://rema1000.dk/avis` | 200 | 0.63s |
| Lidl | `https://www.lidl.dk/c/tilbudsavis/s10013730` | 200 | 0.17s |

## Burst Rate Test Results

| Chain | 1/s (5 req) | 2/s (10 req) | 5/s (10 req) | Max (10 req) | 429/CAPTCHA? |
|-------|-------------|--------------|--------------|--------------|--------------|
| Netto | 5/5 ✅ | 10/10 ✅ | 10/10 ✅ | 10/10 ✅ | No |
| Bilka | 5/5 ✅ | 10/10 ✅ | 10/10 ✅ | 10/10 ✅ | No |
| Føtex | 5/5 ✅ | 10/10 ✅ | 10/10 ✅ | 10/10 ✅ | No |
| SuperBrugsen | 5/5 ✅ | 10/10 ✅ | 10/10 ✅ | 10/10 ✅ | No |
| Rema 1000 | 5/5 ✅ | 10/10 ✅ | 10/10 ✅ | 10/10 ✅ | No |
| Lidl | — | — | — | 10/10 ✅ | No |

## Key Findings

1. **No rate limiting detected** on any chain at max speed (10 sequential requests, no delay)
2. **All 6 chains return HTTP 200** on their tilbudsavis pages
3. **Response times**: Netto/Bilka/Føtex/SuperBrugsen/Lidl all <0.2s. Rema 1000 slower (~0.6s)
4. **Lidl uses `myracloud`** (not Cloudflare as assumed) — no Turnstile challenge on the tilbudsavis page
5. **No CAPTCHA, no 429, no IP blocks** during testing

## Scrape Duration Estimate

| Scenario | Chains | Requests | Estimated Time |
|----------|--------|----------|----------------|
| Sequential (1/sec) | 6 | ~100 (paginated) | ~2 min |
| Parallel (3 concurrent) | 6 | ~100 | ~1 min |
| Aggressive (5/sec) | 6 | ~100 | ~30 sec |

## Recommendations

1. **Safe rate**: 1 req/sec per chain (conservative, well within limits)
2. **Total pipeline**: <2 min for all 6 chains
3. **No proxies needed** for rate limiting (may still need for Cloudflare on other pages)
4. **Monitor for changes**: Re-test monthly in case chains add rate limits

## Notes

- This tests the tilbudsavis landing pages only. Pagination and individual offer detail pages may have different limits — needs separate testing during implementation.
- Lidl's CSP mentions `lidl-flyer.com` — alternative source for flyer data if main site changes.
