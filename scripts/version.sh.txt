#!/usr/bin/env bash
set -euo pipefail
GIT_SHA=${GIT_COMMIT:-$(git rev-parse --short HEAD)}
BUILD_NUM=${BUILD_NUMBER:-local}
echo "1.0.${BUILD_NUM}-${GIT_SHA}"
