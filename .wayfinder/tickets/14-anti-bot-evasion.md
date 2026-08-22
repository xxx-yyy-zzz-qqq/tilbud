# Ticket: Anti-bot evasion strategy — Cloudflare bypass, proxy rotation, fingerprinting

**Labels**: `wayfinder:research`

## Question

Focus on chains with active anti-bot measures (primarily Lidl/Cloudflare, potentially others):
1. **Lidl Cloudflare Turnstile**: What's the minimum viable bypass?
   - Headless browser (Playwright) with realistic fingerprint?
   - Rotate User-Agent + headers?
   - Residential proxies (ISP, not datacenter)?
   - Cloudflare clearance cookie reuse (how long valid)?
2. **Other chains**: Any evidence of bot detection on Netto, Rema 1000, Bilka, Føtex, SuperBrugsen?
3. **Proxy strategy**: If needed, what's the cost?
   - Datacenter proxies (~$0.50-1/GB) — likely blocked
   - Residential proxies (~$5-15/GB) — higher success
   - ISP proxies (~$2-5/GB) — middle ground
   - Free/proxy lists — unreliable, not recommended
4. **Fingerprinting**: What does Cloudflare check?
   - TLS fingerprint (JA3)
   - Canvas/WebGL fingerprint
   - Audio context
   - Battery API
   - Navigator properties
   - Playwright/Stealth plugin effectiveness
5. **Cookie/session persistence**: Can we reuse `cf_clearance` cookie across runs (24h)?
6. **Cost-benefit per chain**: Is Lidl worth the evasion effort vs skipping?
7. **Legal/ToS risk**: Does evasion increase legal exposure?

**Deliverable**: A research report (Markdown) with evasion matrix per chain, recommended approach, proxy cost estimates, Playwright stealth config, and go/no-go for Lidl. Published on `research/anti-bot-evasion` branch with context pointer from this ticket.