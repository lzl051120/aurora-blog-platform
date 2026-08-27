# REST API 摘要

统一前缀 `/api/v1`。写请求必须携带 `X-XSRF-TOKEN`；浏览器客户端会先请求 `/auth/csrf`。完整 OpenAPI 可在运行中的后端 `/api-docs` 获取。

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/home` | 公开 | 精选、最新、分类和统计 |
| GET | `/posts` | 公开 | 搜索、分类、最新/热门、分页 |
| GET | `/posts/{slug}` | 公开 | 详情、浏览计数与公开评论 |
| GET/POST | `/auth/csrf`、`/auth/register`、`/auth/login` | 公开 | CSRF、注册与登录 |
| POST | `/auth/logout` | 登录 | 注销会话 |
| GET/PUT | `/profile` | 登录 | 查看与修改资料 |
| PUT | `/profile/password` | 登录 | 修改密码 |
| POST | `/posts/{id}/comments` | 登录 | 提交待审评论 |
| GET/POST | `/guestbook` | 公开/登录 | 查看公开留言、提交待审留言 |
| GET/POST/PUT/DELETE | `/admin/posts/**` | 管理员 | 文章管理 |
| POST/PUT/DELETE | `/admin/categories/**` | 管理员 | 分类管理 |
| GET/PATCH/DELETE | `/admin/comments/**` | 管理员 | 评论审核 |
| GET/PATCH/DELETE | `/admin/guestbook/**` | 管理员 | 留言审核 |
| GET/PATCH | `/admin/users/**` | 管理员 | 用户状态管理 |
| GET/POST | `/admin/media` | 管理员 | 媒体列表与安全上传 |

常见状态：`400` 参数错误、`401` 未登录/认证失败、`403` 权限或 CSRF、`404` 不存在、`409` 唯一冲突、`429` 频率限制。
