# Wayfinder Map: Danish Tilbudsaviser (10 chains, public API)

## Destination

A working pipeline that fetches Danish grocery tilbudsaviser (10 chains) daily via the public etilbudsavis.dk API, stores offers in PostgreSQL, exposes a search API (Spring Boot + Virtual Threads), and serves a React/TypeScript frontend — with console logging for notifications in v1, email/webhook later.

## Notes

- **Domain**: Danish grocery price monitoring, early ad detection (1.6-3.5 days before valid-from)
- **Stack**: Java 21 Spring Boot (MVC + Virtual Threads) + PostgreSQL + React/TypeScript frontend
- **Data source**: Public `api.etilbudsavis.dk/v2/offers` API (no auth, no scraping)
- **Catalog metadata**: `squid-api.tjek.com/v2/catalogs/{id}` (public)
- **Deployment**: Local machine only for now; Railway/Fly.io later
- **Notifications**: Console/log only for v1; Email (Resend) + Webhook for v2
- **Skills every session should consult**: `/grilling`, `/domain-modeling`, `/research` (for legal/technical feasibility), `/prototype` (for UI/UX)
- **Legal stance**: ToS review + Markedsføringsloven research + community precedent first; lawyer only if gray zone

## Decisions so far

<!-- the index — one line per closed ticket: enough to judge relevance, then zoom the link for the detail the ticket holds -->

- [Legal verdict — ToS review + Markedsføringsloven](https://github.com/xxx-yyy-zzz-qqq/tilbud/issues/2) — GO for all 6 chains (personal use). Lidl/Bilka/Føtex/SuperBrugsen = 🟡 Yellow. Netto/Rema 1000 = 🟢 Green. No cease-and-desist precedent found (research ticket closed)
- [Scraper feasibility per chain](https://github.com/xxx-yyy-zzz-qqq/tilbud/issues/3) — SUPERSEDED by public API discovery. See #14.
- [Public API discovery](https://github.com/xxx-yyy-zzz-qqq/tilbud/issues/14) — `api.etilbudsavis.dk/v2/offers` returns structured offer data for all 10 chains. Public, no auth, no rate limits. Early access 1.6-3.5 days. Returns ALL active offers including multiple weeks. (research ticket closed)
- [Language/runtime split](.wayfinder/tickets/02-scraper-feasibility.md) — TypeScript frontend, Java Spring Boot backend (polyglot) (decided in grilling)
- [Database](https://github.com/xxx-yyy-zzz-qqq/tilbud/issues/4) — PostgreSQL with JSONB + pg_trgm/tsvector for Danish full-text search (prototype ticket open)
- [API framework](https://github.com/xxx-yyy-zzz-qqq/tilbud/issues/5) — Spring MVC + Virtual Threads (Java 21) (prototype ticket open)
- [Deployment target](https://github.com/xxx-yyy-zzz-qqq/tilbud/issues/10) — Local machine only for now (task ticket open)
- [Notification channels v1](https://github.com/xxx-yyy-zzz-qqq/tilbud/issues/9) — Console/log only; Email + Webhook later (grilling ticket open)
- [Store-level data](https://github.com/xxx-yyy-zzz-qqq/tilbud/issues/15) — Skipped for v1 (API only returns chain-level data). Future enhancement. (task ticket open)

## Target Chains (10)

| Chain | Dealer ID | Offers | Early Access |
|-------|-----------|--------|--------------|
| Netto | 9ba51 | 202 | ~2.7 days |
| REMA 1000 | 11deC | 145 | ~3.5 days |
| Lidl | 71c90 | 168 | varies |
| Bilka | 93f13 | 72 | ~1.7 days |
| Føtex | bdf5A | 93 | ~1.6 days |
| SuperBrugsen | 0b1e8 | 100+ | ~2.0 days |
| Kvickly | c1edq | 100+ | ~2.0 days |
| 365discount | DWZE1w | 100+ | ~2.0 days |
| MENY | 267e1m | 51 | ~2.0 days |
| SPAR | 88ddE | 31 | ~2.0 days |

## Not yet specified

<!-- see "Fog of war": in-scope fog you can't ticket yet; graduates as the frontier advances -->

- **Testing strategy**: Unit/integration tests for API client, data model, API, frontend; testcontainers for Postgres
- **GDPR considerations**: Personal data in subscriptions, right to deletion, data processing agreements
- **Category normalization**: Inferred from product names (API doesn't provide per-offer categories)

## Out of scope

<!-- see "Out of scope": work ruled beyond the destination; closed, never graduates -->

- Mobile app (PWA or native) — v2+
- Push notifications — requires mobile/PWA, v2+
- Non-grocery chains (Elgiganten, Bauhaus, Matas, Normal, etc.) — explicit follow-on effort
- Price history charts / analytics dashboard — v2+
- Multi-user SaaS with billing — explicit follow-on effort
- ML-based deal scoring / "is this actually a good price" — v2+
- Store-level pricing (API doesn't support it) — see #15
