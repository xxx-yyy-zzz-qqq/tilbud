# Ticket: User auth & subscriptions — model, flows, notification preferences

**Labels**: `wayfinder:grilling`

## Question

Design the auth and subscription system:
1. **Auth**: Email/password (Spring Security + JWT), OAuth2 (Google, GitHub) later? Magic links?
2. **Subscription model**: One user → many subscriptions. Each subscription = saved search query + notification preferences.
3. **Notification preferences per subscription**: channels (email, webhook), frequency (instant on match, daily digest at 07:00), active/paused
4. **Matching logic**: When scraper runs, find all active subscriptions where offer matches query → queue notifications
5. **Unsubscribe/manage**: UI for users to edit/delete subscriptions, pause, test notification
6. **API keys**: For webhook channel, user generates API key (hashed storage), used to sign webhook payloads

**Deliverable**: Design doc (Markdown) with ERD, API endpoints, JWT claims, webhook payload schema, and flow diagrams. Recorded as resolution comment on this ticket.