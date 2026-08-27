# 验收截图

本目录中的 PNG 由 Playwright 访问本地 Docker 全栈环境后生成。截图只证明对应视口的实际渲染；业务完成结论还需结合自动测试、容器健康、数据库恢复与真实路径结果。

- `home-*-chromium.png`：公开首页在 1440px、768px、390px 三种视口的真实渲染。
- `admin-*-chromium.png`：管理员工作台在三种视口的真实渲染。
- 业务闭环结果、服务健康、重启持久化和恢复演练数据见 `docs/test-report.md`。
