# Ticket: Local dev environment — Docker Compose, Postgres, Spring Boot, React

**Labels**: `wayfinder:task`

## Question

Set up a working local development environment:
1. **docker-compose.yml** with:
   - PostgreSQL 16 (with pg_trgm extension)
   - pgAdmin 4 (optional)
   - Spring Boot backend (hot reload via Spring DevTools)
   - React frontend (Vite/Next.js with HMR)
2. **Backend**: Spring Boot 3.2+, Java 21, Spring Data JPA, Flyway, Spring Web, Spring Security, Resilience4j, Micrometer/Prometheus
3. **Frontend**: React 18, TypeScript, Vite, TanStack Query, Tailwind + DaisyUI/shadcn, React Hook Form + Zod
4. **Shared**: `.env.example` with all required vars (DB URL, JWT secret, scraper config)
5. **Scripts**: `./dev.sh` starts everything, `./test.sh` runs all tests

**Deliverable**: Working `docker-compose.yml`, backend + frontend projects compiling and talking to each other, README with dev commands. Push to `task/local-dev-env` branch. Link from this ticket.