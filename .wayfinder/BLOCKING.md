# Blocking Dependencies — Wayfinder Map: Danish Tilbudsaviser (10 chains, public API)

## Key Design Decisions (from #14)

1. **Use `catalog_ids` filter** for weekly offers (not `dealer_ids`)
2. **Catalog table required** — IDs are random 8-char strings
3. **Catalog type enum**: MAIN, FOOD, NONFOOD, SUPPLEMENT
4. **All 10 chains have catalogs** — catalog_id is NOT nullable
5. **Fetch order**: Catalogs first → offers per catalog

## Ticket Graph

```
01 Legal verdict (research)          14 API discovery (research)          10 Local dev env (task)
       │                                      │                                      │
       ▼                                      ▼                                      ▼
┌──────┴──────┐                      ┌─────────┴─────────┐                   ┌──────┴──────┐
│             │                      │                   │                   │             │
▼             ▼                      ▼                   ▼                   ▼             ▼
03 Data model ─────────────────► 05 Search API ─────────► 06 Frontend UX      11 CI/CD
(prototype)   ◄────────────────── (prototype)             (prototype)         (task)
       ▲             │                   ▲
       │             │                   │
       │             ▼                   │
       │      07 Category norm ◄─────────┘
       │      (grilling)        (needs API data structure)
       │
       ▼
08 Auth & subs ◄────────────────────────┘
(grilling)        (needs users/subscriptions tables)
       │
       ▼
09 Scheduler & obs
(grilling)        (needs API client, error types from #14)
```

## Explicit Blocking Edges

| Ticket | Blocks | Blocked By | Reason |
|--------|--------|------------|--------|
| 01 Legal verdict | 03 Data model | — | ~~Need go/no-go before investing in schema~~ DONE (GO) |
| 14 API discovery | 03, 09 | — | ~~Need API structure, endpoints, error types~~ DONE |
| 10 Local dev env | 11 CI/CD | — | CI needs working build |
| 03 Data model | 05, 08 | 01, 14 | ~~Schema depends on legal go + API fields~~ UNBLOCKED (both done) |
| 05 Search API | 06 | 03 | API contract depends on data model |
| 06 Frontend UX | — | 05 | Frontend consumes search API |
| 07 Category norm | — | 14 | Needs real product names from API |
| 08 Auth & subs | — | 03 | Needs users/subscriptions tables (deprioritized for MVP) |
| 09 Scheduler & obs | — | 14 | ~~Needs API client, error taxonomy from API research~~ UNBLOCKED (done) |
| 11 CI/CD | — | 10 | Needs working docker build from dev env |
| 15 Store-level data | — | — | Future enhancement (external dependency) |

## Rules

- When an issue is closed/resolved, check if it unblocks any other issues
- Remove `blocked` label from newly unblocked issues
- Update this file to reflect current state

## Frontier (Unblocked, Unclaimed, Ready to Start)

1. **04 Data model** — prototype, UNBLOCKED
2. **09 Scheduler & obs** — grilling, UNBLOCKED
3. **10 Local dev env** — task, UNBLOCKED
4. **07 Category norm** — grilling, UNBLOCKED

## Next After Frontier Resolves

- 10 → unblocks 11
- 04 → unblocks 05
- 05 → unblocks 06
- Then 07, 09 (grilling) — can run in parallel
- Then 11 (task)
