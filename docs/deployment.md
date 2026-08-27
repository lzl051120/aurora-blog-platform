# 部署与运维

第一次接触 Git 或 Docker 时，请优先阅读[从零到一部署指南（小白版）](beginner-deployment-guide.md)。本文保留给已经熟悉命令行的维护者作为速查。

## 本地部署

```bash
./scripts/setup-env.sh
docker compose up --build -d
docker compose ps
curl -fsS http://localhost:8080/api/v1/home
```

所有服务应为 `healthy`。MySQL、Redis 和上传文件分别使用命名卷，`docker compose restart` 不会删除数据。不要使用 `docker compose down -v`，除非明确要删除全部本地数据。

## 生产化前检查

1. 使用受管域名和 HTTPS，设置 `COOKIE_SECURE=true`。
2. 重新生成数据库、Redis、管理员密码，不复用开发 `.env`。
3. 限制 OpenAPI、Actuator 与管理端访问来源。
4. 将备份加密后存到独立位置，并定期执行恢复演练。
5. 为上传目录配置对象存储、病毒扫描和容量告警。

## 故障排查

```bash
docker compose ps
docker compose logs --tail=200 backend
docker compose logs --tail=200 mysql redis
docker inspect aurora-blog-backend-1 --format '{{json .State.Health}}'
```

Redis 短时不可用时公开文章仍可阅读，但登录会话与限流会受影响；MySQL 不可用时后端健康检查失败，Nginx 返回服务错误。
