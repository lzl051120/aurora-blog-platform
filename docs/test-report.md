# 测试与验收记录

最后更新：2026-08-27。结论严格区分自动测试、Docker 实际联通、真实浏览器观察和恢复演练，不以“页面能打开”替代完成验收。

## 最终结果

| 层级 | 检查 | 结果 |
|---|---|---|
| 后端单元 | 认证归一化/BCrypt、输入校验、纯文本清洗、富文本 XSS 清洗 | 4 项通过 |
| 前端静态 | ESLint、Vue TypeScript、Vite production build | 全部通过 |
| 前端组件 | Vitest + Vue Test Utils | 1 项通过 |
| Docker | MySQL、Redis、Spring Boot、Nginx 健康检查 | 4 个运行服务全部 healthy；后端镜像构建阶段执行 Maven 测试 |
| 真实浏览器 | 发布→阅读、注册→留言、评论→审核→公开、媒体上传、取证 | 26 项通过、4 项按设计跳过、0 失败 |
| 无障碍 | axe WCAG 2.2 AA、键盘 skip-link、200% 等效重排 | 1440px、768px、390px 全部通过 |
| 持久化 | Compose 重启、MySQL 数据、上传卷 | 重启后 15 篇文章、19 用户、7 评论、11 留言；最新媒体仍返回 13,057 字节 |
| 恢复 | SQL 备份→临时数据库恢复→表/行核验 | 成功恢复 8 张业务表和备份时的 8 篇文章，随后仅删除临时验证库 |
| PPT | 10 页逐页渲染与自动溢出检测 | 无溢出，视觉复核通过 |
| 演示视频 | Playwright 真实浏览器录制并转为通用 H.264 | 6.04 秒、1440×998、可解码 |

## 真实浏览器覆盖

Playwright 使用 Chromium 串行执行，避免并发测试互相污染。三个视口都执行公开页 WCAG 扫描、键盘跳转、200% 重排、管理员发布、游客阅读搜索、普通用户注册、留言、评论和管理员审核。媒体上传及连贯演示只在桌面执行一次，因此平板和手机各有 2 项被显式跳过，共 4 项；这些不是失败项。

最终命令结果：

```text
Running 30 tests using 1 worker
4 skipped
26 passed (1.1m)
```

## Docker 与恢复证据

- 统一入口：`http://localhost:8080`，API 与静态资源经 Nginx 反向代理。
- 重启后 `frontend`、`backend`、`mysql`、`redis` 均为 `healthy`。
- 重启窗口日志未发现 `exception`、`traceback`、`unhandled`、`fatal` 或 `error` 记录。
- 备份文件保存在本机忽略目录 `backups/`，不提交真实运行数据。
- 从备份恢复到一次性临时库后核验表和行数，成功后删除的只有该临时验证库。

## 缺陷闭环

真实媒体上传首次验证时，后端直接访问成功，但统一入口 `/media/*.png` 返回 404。定位到 Nginx 的静态资源正则规则覆盖了媒体代理位置，修复为 `location ^~ /media/` 后重建前端容器；重启前后同一媒体均返回 13,057 字节。

## 证据边界

- 截图见 `docs/evidence/`，只证明对应视口的真实渲染。
- OpenAPI 见 `docs/openapi.json`，由本地真实后端 `/api-docs` 导出。
- WAR、PPT 与视频见 `deliverables/`；WAR 和视频不写入 Git 历史，将由 CI/Release 交付。
- 本报告证明本地 Docker 一键部署，不代表已经公网部署。
- 正式实验报告 DOCX 按当前要求暂不创建。

## GitHub Actions

公开仓库远端运行 `33060952724` 已成功完成：前端质量检查 26 秒、后端测试与 WAR 53 秒、Docker 与真实浏览器闭环 3 分 6 秒。Actions 还给出旧版官方 Action 使用 Node 20 的弃用提醒，但 GitHub runner 已自动使用 Node 24 执行；该提醒不影响本次成功结论。
