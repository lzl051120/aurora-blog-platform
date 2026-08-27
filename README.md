# Aurora Blog Platform

Aurora 是一个面向个人创作者的现代博客平台。公开端提供阅读、搜索、分类、评论与留言；管理端提供文章编辑、内容审核、用户、分类和媒体管理。项目采用单仓库组织，可通过 Docker Compose 在本机一键运行。

> 当前交付范围是本地 Docker 部署与课程验收，不代表已经部署到公网。正式实验报告 DOCX 按要求暂缓。

## 功能概览

- 公开站：首页精选、文章检索与排序、分类筛选、文章详情、浏览排行、留言簿
- 用户端：注册登录、个人资料、密码修改、评论和留言
- 管理端：数据看板、文章草稿/发布、富文本编辑、分类、评论、留言、用户、媒体管理
- 编辑体验：自动保存、错误恢复、预览、字数统计、封面上传、离开提醒
- 安全：Spring Security、BCrypt、Redis 会话、HttpOnly Cookie、CSRF、限流、XSS 清洗、文件魔数校验、角色授权
- 工程：Flyway 迁移、MyBatis、OpenAPI、健康检查、Docker 持久卷、自动测试与 GitHub Actions

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 17、Spring Boot 4.1.1、MyBatis Starter 4.1.0、Spring Security、Flyway |
| 数据 | MySQL 8.4、Redis 7.4 |
| 前端 | Vue 3.5、TypeScript、Vite、Element Plus、Tiptap、Pinia |
| 交付 | Nginx、Docker Compose、Playwright、Vitest、GitHub Actions |

## 一键运行

前置条件：Docker Desktop（或 Docker Engine + Compose 插件）。本机无需安装 JDK、Maven、MySQL 或 Redis。

```bash
./scripts/setup-env.sh
docker compose up --build -d
docker compose ps
```

统一入口：[http://localhost:8080](http://localhost:8080)。管理员用户名和首次密码只保存在本机 `.env`，该文件不会进入 Git。查看本机管理员用户名：

```bash
sed -n 's/^BOOTSTRAP_ADMIN_USERNAME=//p' .env
```

OpenAPI JSON 位于后端容器内 `/api-docs`，Swagger UI 为 `/docs`；出于最小暴露原则，Nginx 默认不向公开入口代理运维端点。

## 常用命令

```bash
# 查看状态和日志
docker compose ps
docker compose logs -f backend frontend

# 前端质量检查
cd frontend
npm ci
npm run lint
npm run typecheck
npm run test
npm run build

# 真实浏览器验收（先保持 Compose 正常运行）
npx playwright install chromium
set -a; source ../.env; set +a
E2E_ADMIN_USERNAME="$BOOTSTRAP_ADMIN_USERNAME" \
E2E_ADMIN_PASSWORD="$BOOTSTRAP_ADMIN_PASSWORD" npm run test:e2e

# 备份与恢复
./scripts/backup-db.sh
./scripts/restore-db.sh backups/aurora_blog_YYYYMMDD_HHMMSS.sql.gz
```

## 目录结构

```text
backend/          Spring Boot WAR、MyBatis、Flyway 与测试
frontend/         Vue 站点、管理后台、组件测试与 Playwright
database/         可恢复的数据库结构导出与说明
docs/             需求、设计、ER、API、部署和验收证据
scripts/          环境初始化、备份、恢复与验收脚本
.github/          CI、Issue/PR 模板与 CODEOWNERS
deliverables/     课程答辩材料（生成后）
```

## 数据与安全说明

- `.env`、Token、密码、数据库运行卷、上传运行数据和构建缓存均不会提交。
- MySQL 是业务事实源；Redis 保存登录会话、限流计数和热门排行辅助数据。
- 浏览量写入 MySQL；Redis 故障不会阻断文章阅读。
- 上传仅接受 5MB 内且文件头匹配的 JPEG、PNG、WebP。
- 正式环境应启用 HTTPS，并设置 `COOKIE_SECURE=true`。

## 文档

- [需求与验收](docs/requirements.md)
- [系统设计](docs/architecture.md)
- [ER 图](docs/er-diagram.md)
- [API 说明](docs/api.md)
- [部署与运维](docs/deployment.md)
- [测试报告](docs/test-report.md)
- [协作规范](CONTRIBUTING.md)

## 许可证

本仓库用于 Java 后端开发实践课程与个人学习，未经仓库所有者许可不得用于商业发布。
