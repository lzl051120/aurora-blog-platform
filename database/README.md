# 数据库脚本

`aurora_blog_schema.sql` 是从实际运行的 MySQL 8.4 容器导出的结构快照；应用启动仍以 `backend/src/main/resources/db/migration/` 下的 Flyway 迁移为权威来源。

初始化新环境请优先启动 Docker Compose，让 Flyway 自动建表和种子分类。备份与恢复使用根目录脚本；备份文件包含真实数据，默认位于已被 Git 忽略的 `backups/`，不得提交公开仓库。

```bash
./scripts/backup-db.sh
./scripts/restore-db.sh backups/aurora_blog_YYYYMMDD_HHMMSS.sql.gz
```
