#!/usr/bin/env bash
set -euo pipefail
HOST="$1"               # staging.example.com or prod.example.com
COMPOSE="$2"            # docker-compose.staging.yml or docker-compose.prod.yml
IMAGE="$3"              # registry/your-app
TAG="$4"                # e.g. 1.0.23-a1b2c3d

ssh -o StrictHostKeyChecking=no "$HOST" "
  export IMAGE='$IMAGE'
  export TAG='$TAG'
  docker pull \$IMAGE:\$TAG || true
  docker compose -f ~/My-app/$COMPOSE down || true
  docker compose -f ~/My-app/$COMPOSE up -d
"
