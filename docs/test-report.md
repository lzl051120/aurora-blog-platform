# 测试与验收记录

最后更新：2026-08-27。结论会区分“自动测试通过”“Docker 实际联通”和“真实浏览器观察”，不以单纯页面可打开代替验收。

| 层级 | 检查 | 当前结果 |
|---|---|---|
| 后端单元 | 认证归一化/BCrypt、输入校验、纯文本清洗、富文本 XSS 清洗 | 4 项通过 |
| 前端静态 | ESLint、Vue TypeScript、Vite production build | 通过 |
| 前端组件 | Vitest + Vue Test Utils | 1 项通过 |
| Docker | MySQL、Redis、Spring Boot、Nginx 健康检查 | 5 服务通过（含构建） |
| 真实浏览器 | 发布→搜索阅读、注册→留言、评论→审核→公开 | 桌面路径通过，完整多视口待最终重跑 |
| 无障碍 | axe WCAG 2.2 AA、键盘 skip-link、200% 等效重排 | 桌面/平板通过；手机名称修复后待重跑 |
| 持久化 | Compose 重启、MySQL 数据、上传卷 | 待最终演练 |
| 恢复 | SQL 备份→临时数据库恢复→表/行核验 | 待执行 |

失败项会保留在本地 Playwright trace 中，不把失败截图当成功证据。最终证据见 `docs/evidence/`。
