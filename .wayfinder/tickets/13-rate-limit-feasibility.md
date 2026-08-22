# Ticket: Rate limit feasibility per chain — actual thresholds, scrape duration, evasion needs

**Labels**: `wayfinder:research`

## Question

For each of the 6 target chains (Netto, Rema 1000, Lidl, Bilka, Føtex, SuperBrugsen):
1. **Actual rate limits**: What's the exact threshold (requests/second, requests/minute, burst) before 429/CAPTCHA/IP ban? Test empirically.
2. **Scrape duration**: How long to fetch ALL offers for a chain (~200-500 offers)? Include pagination, detail pages if needed.
3. **Total pipeline time**: Can we scrape all 6 chains sequentially within their limits in <10 minutes total?
4. **Concurrency limits**: What's safe parallelism per chain? Global across chains?
5. **Error responses**: What happens when exceeded — 429 with Retry-After? CAPTCHA challenge? Immediate IP ban? Temporary block (minutes/hours)?
6. **Recovery**: After rate limit, how long until reset? Does it persist across IP/session?
7. **API vs HTML**: For Rema 1000 (has API), Lidl (GraphQL) — do API endpoints have different/higher limits than HTML?
8. **GDPR / legal rate limits**: Any special considerations for automated access to price data?

**Deliverable**: A research report (Markdown) with per-chain rate limit matrix, empirical test results, recommended scrape schedule (sequential vs parallel), backoff strategy, and total pipeline time estimate. Published on `research/rate-limit-feasibility` branch with context pointer from this ticket.