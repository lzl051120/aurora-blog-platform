#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
backup="${1:-}"
if [[ -z "$backup" || ! -f "$backup" ]]; then echo "用法：./scripts/restore-db.sh backups/文件.sql.gz" >&2; exit 1; fi
echo "即将把 $backup 恢复到本地 aurora_blog；现有同名表会按备份内容重建。"
read -r -p "输入 RESTORE 确认：" answer
[[ "$answer" == "RESTORE" ]] || { echo "已取消"; exit 1; }
gzip -dc "$backup" | docker compose exec -T mysql sh -c 'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" aurora_blog'
echo "数据库恢复完成。"
