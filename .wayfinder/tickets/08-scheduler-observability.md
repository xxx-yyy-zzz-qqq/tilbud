# Ticket: Scheduler & observability — Spring Boot scheduling, logging, metrics, retry

**Labels**: `wayfinder:grilling`

## Question

Design the operational backbone for the scraper:
1. **Scheduling**: `@Scheduled(cron = "0 6 * * *")` at 06:00 daily. How to handle overlap (previous run still running)? Single-threaded per chain? Parallel with semaphore?
2. **Per-chain scraping**: Sequential vs parallel? Rate limits per chain (requests/sec, concurrent)?
3. **Retry/backoff**: Spring Retry config — max attempts, exponential backoff, which exceptions retryable?
4. **Circuit breaker**: Resilience4j — open after N failures, half-open probe, fallback (skip chain, alert)
5. **Logging**: Structured JSON (Logstash/ECS), correlation ID per scrape run, log offer counts per chain
6. **Metrics**: Micrometer + Prometheus — scrape duration, offers found, offers new/updated, errors by type, chain health
7. **Health checks**: `/actuator/health` — DB, scraper last run status, chain-specific health
8. **Alerting**: Log-based (ERROR level) → webhook/email for now; Prometheus alerts later

**Deliverable**: Design doc (Markdown) with Spring config snippets, Resilience4j config, Micrometer metrics list, log schema. Recorded as resolution comment on this ticket.