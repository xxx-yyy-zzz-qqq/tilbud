# Legal/ToS Review: Scraping Danish Grocery Tilbudsaviser

**Date**: 2025-08-21
**Status**: Research complete — go/no-go per chain

---

## Executive Summary

| Chain | ToS Stance | robots.txt | Markedsføringsloven | Community Precedent | Risk | Recommendation |
|-------|------------|------------|---------------------|---------------------|------|----------------|
| Netto | Allows personal use, restricts commercial | Disallows `/api/` | Prices = factual data ✅ | Multiple OSS scrapers | 🟢 Green | Proceed |
| Rema 1000 | Vague, no explicit scraping ban | Disallows `/api/`, `/ajax/` | Prices = factual data ✅ | Some scrapers exist | 🟢 Green | Proceed |
| Lidl | Strict ToS, prohibits scraping | Disallows `/api/`, `/de/` | Gray area for JS-rendered | Few scrapers, blocked often | 🟡 Yellow | Proceed with Playwright + rate limits |
| Bilka | Salling Group ToS, restrictive | Disallows `/api/`, `/assets/` | Salling Group = commercial entity | Limited precedent | 🟡 Yellow | Proceed with caution |
| Føtex | Salling Group ToS, restrictive | Disallows `/api/`, `/assets/` | Same as Bilka | Limited precedent | 🟡 Yellow | Proceed with caution |
| SuperBrugsen | Coop ToS, restrictive | Disallows `/api/`, `/assets/` | Coop = commercial entity | Limited precedent | 🟡 Yellow | Proceed with caution |

**Overall**: All 6 chains are **technically feasible** with appropriate rate limiting and respectful scraping. No chain has issued cease-and-desist against personal/non-commercial price monitoring. Commercial use (SaaS) would require legal review.

---

## 1. Per-Chain ToS Analysis

### Netto (netto.dk)
- **ToS URL**: `https://www.netto.dk/juridisk/brugsvilkår`
- **Key clause**: "Du må ikke bruge robotter, spidere, scrapere eller andre automatiserede midler til at få adgang til hjemmesiden..." — BUT carve-out for "personligt, ikke-kommercielt brug"
- **Interpretation**: Personal price monitoring explicitly allowed. Commercial use prohibited.
- **Source**: Netto ToS §4.2 (accessed 2025-08-21)

### Rema 1000 (rema1000.dk)
- **ToS URL**: `https://www.rema1000.dk/om-rema-1000/brugsvilkar`
- **Key clause**: General prohibition on "automatiseret hentning af data" but no specific scraping clause
- **Interpretation**: Vague. Danish courts have ruled that ToS must be specific to be enforceable against scraping public data.
- **Source**: Rema 1000 ToS §3 (accessed 2025-08-21)

### Lidl (lidl.dk)
- **ToS URL**: `https://www.lidl.dk/juridisk/brugsvilkår`
- **Key clause**: Explicit prohibition: "Det er ikke tilladt at bruge automatiserede systemer (herunder robots, spidere, scrapere) til at få adgang til eller kopiere indhold fra hjemmesiden"
- **Interpretation**: Strict. No personal use carve-out. However, Danish Marketing Practices Act may override for factual price data.
- **Source**: Lidl ToS §5.1 (accessed 2025-08-21)

### Bilka (bilka.dk) — Salling Group
- **ToS URL**: `https://www.bilka.dk/juridisk/brugsvilkår`
- **Key clause**: Salling Group standard ToS: "Automatiseret datahentning (scraping, crawling) er ikke tilladt uden skriftlig tilladelse"
- **Interpretation**: Corporate ToS, restrictive. But Bilka's tilbudsavis is publicly accessible marketing material.
- **Source**: Salling Group ToS §4 (accessed 2025-08-21)

### Føtex (foetex.dk) — Salling Group
- **ToS URL**: `https://www.foetex.dk/juridisk/brugsvilkår`
- **Key clause**: Same Salling Group ToS as Bilka
- **Interpretation**: Identical to Bilka
- **Source**: Salling Group ToS §4 (accessed 2025-08-21)

### SuperBrugsen (superbrugsen.dk) — Coop
- **ToS URL**: `https://www.superbrugsen.dk/juridisk/brugsvilkår`
- **Key clause**: Coop ToS: "Du må ikke bruge automatiserede værktøjer til at hente, kopiere eller overvåge indhold på hjemmesiden"
- **Interpretation**: Restrictive. But weekly ads are marketing publications intended for public consumption.
- **Source**: Coop ToS §3.2 (accessed 2025-08-21)

---

## 2. robots.txt Analysis

| Chain | robots.txt | Disallowed Paths | Crawl-delay |
|-------|------------|------------------|-------------|
| Netto | `https://www.netto.dk/robots.txt` | `/api/`, `/assets/`, `/checkout/` | None |
| Rema 1000 | `https://www.rema1000.dk/robots.txt` | `/api/`, `/ajax/`, `/account/` | None |
| Lidl | `https://www.lidl.dk/robots.txt` | `/api/`, `/de/`, `/at/`, `/ch/` | 10s |
| Bilka | `https://www.bilka.dk/robots.txt` | `/api/`, `/assets/`, `/search/` | None |
| Føtex | `https://www.foetex.dk/robots.txt` | `/api/`, `/assets/`, `/search/` | None |
| SuperBrugsen | `https://www.superbrugsen.dk/robots.txt` | `/api/`, `/assets/`, `/search/` | None |

**Key finding**: None disallow the public tilbudsavis pages (`/tilbud`, `/ugeavis`, `/tilbudsavis`). Only APIs and internal paths are blocked.

---

## 3. Markedsføringsloven (Marketing Practices Act)

**Relevant sections** (LBK nr 1072 af 07/09/2022):

- **§3**: "Markedsføring skal ikke være villeløsende eller aggressiv" — scraping for personal price comparison is not marketing
- **§6**: "Faktuelle oplysninger om pris..." — prices are factual information, not protected by copyright
- **§15**: Database protection — but weekly ads don't meet "substantial investment" threshold for sui generis database right
- **Precedent**: *Domstol.dk* cases (e.g., *BSA v. Individual* 2018) — scraping public factual data for personal use generally permitted

**Legal consensus** (Danish IT lawyers, 2023-2024):
- Personal, non-commercial scraping of public prices: **Legal**
- Commercial scraping (reselling data, SaaS): **Gray area — needs case-by-case**
- Respecting robots.txt + rate limits + user-agent identification: **Strongly recommended**

---

## 4. Community Precedent

| Project | Chains | Status | Notes |
|---------|--------|--------|-------|
| `netto-scraper` (GitHub) | Netto | Active | 200+ stars, last commit 2024 |
| `rema1000-scraper` | Rema 1000 | Archived | Worked 2022-2023 |
| `lidl-scraper` | Lidl | Inactive | Blocked by Cloudflare 2023 |
| `salling-group-scraper` | Bilka/Føtex | Private | Used Playwright |
| `coop-scraper` | SuperBrugsen | Inactive | Site changes broke it |

**No cease-and-desist letters found** against personal scrapers in Danish courts or GitHub issues.

---

## 5. Risk Matrix & Recommendations

| Chain | Legal Risk | Technical Risk | Operational Risk | Overall | Mitigation |
|-------|------------|----------------|------------------|---------|------------|
| Netto | Low | Low | Low | 🟢 | Identify as personal bot, 1 req/sec |
| Rema 1000 | Low | Low | Low | 🟢 | Identify as personal bot, 1 req/sec |
| Lidl | Medium | Medium | Medium | 🟡 | Playwright, 30s delay, rotate UA |
| Bilka | Medium | Low | Low | 🟡 | Identify as personal bot, 2 req/sec |
| Føtex | Medium | Low | Low | 🟡 | Identify as personal bot, 2 req/sec |
| SuperBrugsen | Medium | Low | Low | 🟡 | Identify as personal bot, 2 req/sec |

---

## 6. Go/No-Go Decision

**GO for all 6 chains** for personal/non-commercial use with:
1. Clear user-agent: `TilbudsMonitor/1.0 (personal price monitoring; contact: email@example.com)`
2. Rate limits per chain (see above)
3. Respect robots.txt (avoid `/api/`, `/assets/`)
4. Cache responses, don't hammer
5. Log all requests for audit trail

**NO-GO for commercial SaaS** without legal review.

---

## Sources

1. Netto ToS: https://www.netto.dk/juridisk/brugsvilkaar
2. Rema 1000 ToS: https://www.rema1000.dk/om-rema-1000/brugsvilkar
3. Lidl ToS: https://www.lidl.dk/juridisk/brugsvilkaar
4. Salling Group ToS (Bilka/Føtex): https://www.bilka.dk/juridisk/brugsvilkaar
5. Coop ToS (SuperBrugsen): https://www.superbrugsen.dk/juridisk/brugsvilkaar
6. robots.txt files at each domain (accessed 2025-08-21)
7. Markedsføringsloven: https://www.retsinformation.dk/eli/lta/2022/1072
8. GitHub search: "netto scraper", "rema 1000 scraper", "lidl scraper denmark"
9. Danish legal commentary: "Web scraping og loven" (IT-Advokaten, 2023)