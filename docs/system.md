# System SSOT

## Architecture

Aurora 使用前后端分离的单仓库结构。浏览器统一访问 Nginx `http://localhost:8080`；Nginx 提供 Vue 静态资源，并把 `/api/` 与 `/media/` 转发到 Spring Boot。MySQL 是业务事实源，Redis 保存登录会话、限流计数和热门排行辅助数据。详细图见 [architecture.md](architecture.md)。

## Tech Stack

- Frontend: Vue 3.5、TypeScript、Vite、Element Plus、Tiptap、Pinia、Nginx。
- Backend: Java 17、Spring Boot 4.1.1、MyBatis Starter 4.1.0、Spring Security、Flyway。
- Data: MySQL 8.4、Redis 7.4、上传文件命名卷。
- Infrastructure: Docker Compose、GitHub Actions、Playwright、Vitest。

## Important Commands

```bash
# 生成本机环境
./scripts/setup-env.sh

# 启动/重建
docker compose up --build -d
docker compose ps

# 本地综合检查
./scripts/verify.sh

# 数据库备份与恢复
./scripts/backup-db.sh
./scripts/restore-db.sh backups/aurora_blog_YYYYMMDD_HHMMSS.sql.gz
```

## Data Model

核心实体为用户、分类、文章、评论、留言、媒体和审计记录。MySQL 迁移由 Flyway 管理；已发布迁移不可修改，只能新增补偿或演进迁移。详见 [er-diagram.md](er-diagram.md) 和 `backend/src/main/resources/db/migration/`。

## Integrations

- GitHub：公开源码仓库、Issue/PR、CODEOWNERS、Actions 和 Release。
- 当前没有已验证的公网服务器、对象存储、邮件或第三方登录集成。

## Deployment

当前已验证交付为本地 Docker Compose：`frontend`、`backend`、`mysql`、`redis` 四个服务以及 MySQL、Redis、上传文件三个命名卷。小白步骤见 [beginner-deployment-guide.md](beginner-deployment-guide.md)，维护速查见 [deployment.md](deployment.md)。

## Guardrails

- Auth/security: BCrypt、HttpOnly Cookie、CSRF、RBAC、XSS 清洗、安全上传和 Redis 限流；生产环境必须启用 HTTPS 与 `COOKIE_SECURE=true`。
- Data safety: `.env`、密码、Token、运行数据库、上传卷和备份不进入 Git；禁止在没有备份与明确意图时执行 `docker compose down -v`。
- Cost/latency: 当前本地部署无外部托管成本；新增云服务前需要所有者确认预算和凭据。
- Rollback: 代码使用普通 revert，不强推或重写历史；数据库先备份并通过新增 Flyway 迁移修复。

## Documentation

- 从零部署：[beginner-deployment-guide.md](beginner-deployment-guide.md)
- 优化路线：[optimization-roadmap.md](optimization-roadmap.md)
- 团队协作：[github-team-collaboration.md](github-team-collaboration.md)
