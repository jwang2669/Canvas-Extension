#!/usr/bin/env sh
# Loads .env from infra/ (team standard). Run from apps/backend.
# Usage: ./run.sh   or from repo root: cd apps/backend && ./run.sh

cd "$(dirname "$0")"
if [ -f ../../infra/.env ]; then
  set -a
  . ../../infra/.env
  set +a
fi
exec ./gradlew bootRun
