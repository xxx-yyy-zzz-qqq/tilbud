# Ticket: Scraper feasibility per chain — HTML structure, anti-bot, pagination

**Labels**: `wayfinder:research`

## Question

For each of the 6 target chains (Netto, Rema 1000, Lidl, Bilka, Føtex, SuperBrugsen):
1. Where is the weekly tilbudsavis published? (URL pattern, subdomain, path)
2. Is it static HTML (cheerio/Jsoup) or JS-rendered (Playwright required)?
3. HTML structure of offer cards: product name, price, valid-from/valid-to, image, category, store location
4. Pagination: how many pages, "load more" button, infinite scroll?
5. Anti-bot measures: Cloudflare, Akamai, rate limits, CAPTCHA, user-agent checks
6. Publication schedule: when does the "early" version appear (day before valid-from)?
7. Sample HTML for 3-5 offers per chain (save as fixtures)

**Deliverable**: A research report (Markdown) with per-chain feasibility matrix, recommended engine (Jsoup vs Playwright), selectors/XPaths, rate limit strategy, and sample fixtures. Published on `research/scraper-feasibility` branch with context pointer from this ticket.