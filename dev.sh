#!/bin/bash
set -e

echo "Starting Tilbud development environment..."

# Copy .env.example to .env if it doesn't exist
if [ ! -f .env ]; then
    echo "Creating .env from .env.example..."
    cp .env.example .env
fi

# Start services
echo "Starting Docker Compose..."
docker compose up --build

# On Ctrl+C, stop all services
trap 'echo "Stopping..."; docker compose down; exit 0' INT TERM
