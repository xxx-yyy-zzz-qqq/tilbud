# Ticket: CI/CD pipeline — GitHub Actions for test, build, lint, deploy prep

**Labels**: `wayfinder:task`

## Question

Set up GitHub Actions workflows:
1. **CI workflow** (on push/PR):
   - Backend: `./mvnw verify` (compile, test, checkstyle/spotbugs, Jacoco coverage)
   - Frontend: `npm ci && npm run lint && npm run typecheck && npm test && npm run build`
   - Docker build test (both images)
2. **CD workflow** (on tag/main):
   - Build & push Docker images to GHCR
   - Deploy to Railway/Fly.io (manual approval step for now)
   - Run DB migrations on deploy
3. **Dependabot** for Java (Maven) and Node.js dependencies
4. **CodeQL** security scanning

**Deliverable**: `.github/workflows/ci.yml`, `.github/workflows/cd.yml`, `.github/dependabot.yml`. Push to `task/ci-cd-pipeline` branch. Link from this ticket.