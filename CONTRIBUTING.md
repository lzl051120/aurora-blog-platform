# 中文协作规范

第一次参与仓库协作时，请先阅读[GitHub 团队协作指南](docs/github-team-collaboration.md)，其中包含成员权限、Issue、分支、PR、Review、冲突解决和发布的完整流程。

## 分支

- `main`：始终保持可构建、可验收。
- `feature/<英文功能>`：新功能，例如 `feature/article-editor`。
- `fix/<英文问题>`：缺陷修复，例如 `fix/login-validation`。
- `docs/<英文主题>`：文档与课程材料。

## 提交

提交信息采用“类型 + 中文范围 + 中文描述”：

```text
feat(文章): 新增文章发布与草稿保存功能
fix(认证): 修复停用账号登录异常
test(评论): 增加评论审核真实路径测试
docs(部署): 补充Docker一键部署说明
```

提交前必须执行 `git status`、检查差异，并确认没有 `.env`、密码、Token、运行数据库、上传文件或缓存。禁止强制推送、硬重置和无确认的历史重写。

## Pull Request

PR 请说明目标、主要改动、验证命令、界面截图和风险/回滚方式。至少满足：前端 lint、类型、单测、构建通过；后端单测和 WAR 构建通过；涉及真实路径时附 Playwright 证据。

## 代码约定

- REST API 统一位于 `/api/v1`，错误返回可理解的中文信息。
- 数据结构变更只能新增 Flyway 迁移，不修改已经发布的迁移。
- 前端必须覆盖加载、空、错误、成功、禁用和确认状态。
- 交互必须支持键盘、reduced-motion、390/768/1440px 与 WCAG 2.2 AA。
