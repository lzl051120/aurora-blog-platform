#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
docker compose config -q
docker compose ps
curl -fsS http://localhost:8080/api/v1/home >/dev/null
(cd frontend && npm run lint && npm run typecheck && npm run test && npm run build)
echo "静态检查、组件测试、Docker 配置和真实 API 均通过。"
