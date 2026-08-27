#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if [[ ! -f .env ]]; then echo "未找到 .env，请先运行 ./scripts/setup-env.sh" >&2; exit 1; fi
mkdir -p backups
stamp="$(date +%Y%m%d_%H%M%S)"
target="backups/aurora_blog_${stamp}.sql.gz"
docker compose exec -T mysql sh -c 'exec mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction --routines --triggers --set-gtid-purged=OFF aurora_blog' | gzip > "$target"
echo "数据库备份已生成：$target"
