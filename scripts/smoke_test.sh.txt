#!/usr/bin/env bash
set -euo pipefail
URL="$1" # e.g., http://staging.example.com:8080/actuator/health
for i in {1..30}; do
  code=$(curl -s -o /dev/null -w "%{http_code}" "$URL" || true)
  if [ "$code" = "200" ]; then
    echo "OK"
    exit 0
  fi
  sleep 2
done
echo "Smoke test failed: $URL"
exit 1
