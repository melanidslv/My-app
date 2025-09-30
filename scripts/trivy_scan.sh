#!/usr/bin/env bash
set -euo pipefail
IMAGE="$1"   # registry/your-app
TAG="$2"
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy:latest image --exit-code 0 --severity MEDIUM,HIGH,CRITICAL \
  --format table "$IMAGE:$TAG" | tee trivy-image-report.txt

# Fail build on HIGH/CRITICAL
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy:latest image --exit-code 1 --severity HIGH,CRITICAL \
  --format json "$IMAGE:$TAG" > trivy-image-report.json || EXIT=$?
exit ${EXIT:-0}
