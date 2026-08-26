# Tilbud Frontend

React 19 + TypeScript + Tailwind CSS + DaisyUI prototype for browsing Danish grocery weekly ads.

## Prerequisites

- Node.js 24+
- Backend running on `http://localhost:8080` (see `../backend/README.md`)

## Start locally

```bash
npm install
npm run dev
```

Vite dev server starts on `http://localhost:5173` and proxies `/api` requests to the backend.

## IntelliJ / WebStorm

1. Open the `frontend/` folder as a project
2. Run `npm install` in the terminal
3. Add npm configuration:
   - Run → Edit Configurations → + → npm
   - Command: `run`
   - Script: `dev`
   - Working directory: `$ProjectFileDir$`
4. Click Run

## Build

```bash
npm run build
```

Output goes to `dist/`.

## Docker

```bash
docker compose up --build
```

Frontend served via nginx on `http://localhost:5173`, proxies `/api` to backend container.
