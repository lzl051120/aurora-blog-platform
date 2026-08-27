# Aurora Blog Platform GitHub 团队协作指南

> 目标：让多人在不共享账号、不直接破坏 `main`、不泄露凭据的前提下，使用 Issue、分支、Pull Request、Review 和 GitHub Actions 协作。
>
> 仓库所有者是 `lzl051120`。在获得其他成员的 GitHub 用户名之前，不发送协作者邀请。

## 1. 团队角色建议

| 角色 | 主要职责 | 推荐权限 |
|---|---|---|
| 仓库所有者 | 成员权限、分支规则、Secret、Release 和最终风险决策 | Admin |
| 维护者 | Issue 分派、PR 合并、版本整理、日常维护 | Maintain |
| 开发者 | 创建分支、提交代码、发起 PR、修复问题 | Write |
| 评审者 | Review、提出修改建议、确认验收证据 | Read 或 Write |
| 外部贡献者 | Fork 后提交 PR | 无需仓库写权限 |

最小权限原则：只给完成职责所需的权限。任何成员都不应索要或共享所有者密码、个人访问 Token、数据库密码或生产 Secret。

## 2. 所有者第一次配置 GitHub

### 2.1 邀请成员

1. 打开仓库；
2. 进入“Settings”；
3. 打开“Collaborators and teams”；
4. 点击“Add people”；
5. 输入成员准确的 GitHub 用户名；
6. 根据职责分配权限；
7. 等待成员接受邀请。

建议先给普通开发者 `Write`，不要默认给 `Admin`。

### 2.2 保护 main 分支

进入“Settings → Rules → Rulesets”，为 `main` 创建规则。建议：

- Require a pull request before merging；
- 至少 1 个 Approval；
- Dismiss stale approvals when new commits are pushed；
- Require conversation resolution before merging；
- Require status checks to pass；
- Block force pushes；
- Restrict deletions；
- 仓库所有者也遵守规则，紧急情况另行记录。

本仓库可设为必需检查的任务：

- `前端质量检查`；
- `后端测试与 WAR`；
- `Docker 与真实浏览器闭环`。

先让这些检查至少成功运行一次，再在 Ruleset 中选择，避免名称尚未出现。

### 2.3 推荐仓库设置

- 开启自动删除已合并分支；
- 默认使用 Squash merge 或 Merge commit，团队统一一种策略；
- 开启 Issues；
- 保留现有 Issue 模板和 PR 模板；
- 需要敏感环境时使用 GitHub Environments 和审批人；
- Secret 只放在 Settings → Secrets and variables，不写进代码。

## 3. 新成员初始化本地环境

### 3.1 克隆并进入仓库

```bash
git clone https://github.com/lzl051120/aurora-blog-platform.git
cd aurora-blog-platform
```

### 3.2 配置自己的身份

```bash
git config user.name "你的姓名或GitHub显示名"
git config user.email "你的GitHub隐私邮箱"
```

查看配置：

```bash
git config user.name
git config user.email
```

每个人使用自己的 GitHub 身份提交，禁止多人共用同一个账号。

### 3.3 生成本机环境

```bash
./scripts/setup-env.sh
docker compose up --build -d
docker compose ps
```

每位成员拥有自己的 `.env` 和本地数据卷。不要把某位成员的 `.env` 发给其他人。

## 4. 标准协作闭环

```text
Issue → 分派负责人 → 新建分支 → 小步提交 → 本地验证
      → 推送分支 → Pull Request → CI → Review
      → 修改与再验证 → 合并 → 删除分支 → 更新 Issue
```

任何用户可见功能、Bug 修复、数据库迁移或重要文档，都应有对应 Issue 或 PR 描述。

## 5. 从 Issue 开始

### 5.1 缺陷 Issue

使用“缺陷报告”模板，至少写：

- 实际结果和期望结果；
- 稳定复现步骤；
- 操作系统、浏览器和 Docker 版本；
- 去除密码、Cookie、Token 后的日志或截图；
- 影响范围和严重程度。

### 5.2 功能建议 Issue

使用“功能建议”模板，至少写：

- 谁遇到了什么问题；
- 建议方案；
- 可观察的验收标准；
- 非目标；
- 风险、成本和依赖条件。

### 5.3 Issue 不应包含

- `.env` 内容；
- 管理员密码、Token、Cookie；
- 未脱敏的用户数据；
- 只有“做个搜索功能”而没有验收标准的模糊描述。

## 6. 创建工作分支

先同步远端 `main`：

```bash
git switch main
git pull --ff-only origin main
```

根据任务创建分支：

```bash
git switch -c feature/article-schedule
```

分支命名规范：

| 类型 | 格式 | 示例 |
|---|---|---|
| 新功能 | `feature/<英文功能>` | `feature/article-schedule` |
| 缺陷 | `fix/<英文问题>` | `fix/login-validation` |
| 文档 | `docs/<英文主题>` | `docs/beginner-deployment` |
| 测试 | `test/<英文范围>` | `test/comment-permission` |
| 重构 | `refactor/<英文范围>` | `refactor/article-service` |

一个分支只处理一个主题。不要在文章功能分支顺手重构认证模块。

## 7. 开发与提交

### 7.1 修改前

```bash
git status
git branch --show-current
```

确认自己不在 `main`，并理解工作区原有修改属于谁。

### 7.2 提交前检查

```bash
git status
git diff
git diff --check
```

确认没有：

- `.env`、Token、密码；
- 数据库运行文件和备份；
- `node_modules`、`target`、构建缓存；
- 与当前 Issue 无关的修改。

### 7.3 中文提交信息

格式是“类型 + 中文范围 + 中文描述”：

```bash
git add backend/src/main/java/com/aurora/blog/service/ArticleService.java
git commit -m "feat(文章): 新增定时发布状态校验"
```

常用类型：

| 类型 | 用途 |
|---|---|
| `feat` | 新功能 |
| `fix` | Bug 修复 |
| `test` | 测试 |
| `docs` | 文档 |
| `refactor` | 不改变行为的重构 |
| `chore` | 工程维护 |
| `ci` | 持续集成 |

按可验证功能拆分提交，不要把数天工作压成一个“大杂烩提交”。

## 8. 本地验证

根据改动范围运行：

```bash
# 前端
cd frontend
npm ci
npm run lint
npm run typecheck
npm run test
npm run build
cd ..

# 完整 Docker
docker compose up --build -d
docker compose ps
curl -fsS http://localhost:8080/api/v1/home
```

涉及主用户路径时执行 Playwright。涉及数据库结构时只能新增 Flyway 迁移，不能修改已发布迁移。

验证失败时不要为了变绿而删除测试、关闭权限、改成假数据或隐藏错误。

## 9. 推送自己的分支

```bash
git push -u origin feature/article-schedule
```

第一次的 `-u` 会建立本地分支与远端分支的跟踪关系。后续可直接执行 `git push`。

推送前应在团队沟通中说明：

- 分支名称和用途；
- 将推送的提交；
- 是否影响数据库、API 或部署；
- 已完成哪些验证。

不要直接 `git push origin main`。

## 10. 创建 Pull Request

在 GitHub 分支页面点击“Compare & pull request”。PR 标题继续使用中文规范，例如：

```text
feat(文章): 支持文章定时发布
```

按仓库模板填写：

1. 目标；
2. 主要改动；
3. 验证证据；
4. 风险与回滚；
5. 界面截图或真实浏览器证据；
6. 关联 Issue，例如 `Closes #12`。

如果仍在讨论方案，可先创建 Draft PR；准备评审后再标记为 Ready for review。

## 11. GitHub Actions 检查

PR 会运行：

| 任务 | 内容 |
|---|---|
| 前端质量检查 | npm 安装、lint、类型、组件测试、生产构建 |
| 后端测试与 WAR | Docker 构建、Maven 测试、WAR 提取 |
| Docker 与真实浏览器闭环 | 完整 Compose、健康检查、三视口 Playwright、WCAG |

红色失败时：

1. 点开失败任务；
2. 定位第一个真正失败的步骤；
3. 在本地复现；
4. 修复后重新运行受影响检查；
5. 新增正常提交并推送；
6. 不要通过跳过测试或修改历史隐藏失败。

## 12. Code Review 规则

评审者重点检查：

- 是否真正解决 Issue；
- API、权限、数据迁移是否兼容；
- 是否泄露 Secret 或用户数据；
- 是否有正常、异常、权限和边界测试；
- 前端是否覆盖加载、空、错误、成功和禁用状态；
- 是否支持键盘、三视口、200% 和 reduced-motion；
- 文档、部署和回滚是否同步；
- 提交是否聚焦、可理解。

Review 操作：

- `Comment`：讨论或非阻塞建议；
- `Approve`：达到合并标准；
- `Request changes`：存在必须修复的问题。

提交新修改后，原 Approval 可按规则失效，评审者需要重新确认。

## 13. 同步 main 与解决冲突

PR 开发期间 `main` 发生变化时，推荐合并最新 `main` 到功能分支，避免对共享历史执行 rebase：

```bash
git fetch origin
git switch feature/article-schedule
git merge origin/main
```

出现冲突后：

```bash
git status
```

逐个打开冲突文件，保留正确内容并删除冲突标记。实际文件中使用半角符号；下面为避免文档检查误判，使用全角符号展示：

```text
＜＜＜＜＜＜＜ 当前分支
＝＝＝＝＝＝＝
＞＞＞＞＞＞＞ origin/main
```

解决后：

```bash
git add 已解决的文件
git commit -m "chore(合并): 同步main并解决文章模块冲突"
git push
```

不要使用 `git reset --hard`、`git clean -fd` 或 `git push --force`“快速解决”冲突。无法判断正确内容时应暂停并邀请文件负责人共同处理。

## 14. 合并 PR

满足以下条件后由有权限成员合并：

- CI 全绿；
- 必需 Review 已通过；
- 所有对话已解决；
- 验收证据充分；
- 风险和回滚写清；
- 没有未授权范围扩张。

建议使用仓库统一的合并策略。合并后删除远端功能分支，并在本地同步：

```bash
git switch main
git pull --ff-only origin main
git branch -d feature/article-schedule
```

`git branch -d` 只删除已合并的本地分支；不要随意使用大写 `-D`。

## 15. 版本发布协作

建议由仓库所有者或维护者发布：

1. 确认 `main` 最新 Actions 全绿；
2. 更新 `CHANGELOG.md` 和中文发布说明；
3. 确认版本号，例如 `v1.1.0`；
4. 创建 GitHub Release；
5. 上传经验证的 WAR、PPT、演示或其他附件；
6. 发布后核验标签、附件和下载；
7. 在 Issue 或讨论区记录已知限制。

未经确认不要覆盖同名 Release、删除标签或重新上传不同内容冒充同一版本。

## 16. 回滚与事故处理

### 普通代码问题

优先创建一个反向提交：

```bash
git revert <有问题的提交号>
git push origin 当前修复分支
```

通过 PR 合并回滚，不改写历史。

### 数据库迁移问题

- 不修改已经发布的 Flyway 文件；
- 新增补偿迁移；
- 先备份，再演练恢复；
- 明确代码与数据库部署顺序；
- 涉及真实生产数据时必须由所有者批准。

### Secret 泄露

1. 立即撤销或轮换泄露凭据；
2. 检查 Actions、Release、Issue、PR 和 Git 历史；
3. 删除展示位置不等于凭据安全，必须先让旧凭据失效；
4. 记录影响范围和后续预防措施；
5. 如需清理 Git 历史，必须单独评估并获得所有者确认。

## 17. 推荐的团队节奏

| 频率 | 活动 | 输出 |
|---|---|---|
| 每个任务开始 | Issue 澄清与分派 | 负责人、边界、验收标准 |
| 每次推送前 | 本地检查 | 可解释的小提交 |
| 每个 PR | CI + Review | 证据、风险、回滚 |
| 每周 | 整理 Issue 和依赖更新 | 下周优先级 |
| 每个版本 | Release 复核 | 更新日志、附件、已知限制 |
| 每月/季度 | 备份恢复与权限审查 | 演练记录、过期权限清理 |

## 18. 新成员练习任务

建议先完成一个只改文档的小任务：

1. 创建 Issue：“补充某条常见问题”；
2. 新建 `docs/update-faq` 分支；
3. 修改一处 Markdown；
4. 使用 `docs(文档): 补充常见问题说明` 提交；
5. 推送并创建 Draft PR；
6. 等待 CI；
7. 邀请 Review；
8. 合并后同步 `main`。

通过后再参与业务代码、数据库或安全相关工作。

## 19. 协作红线

- 不共享 GitHub 账号、Token、管理员密码或 `.env`；
- 不直接向 `main` 开发和推送；
- 不强推、硬重置、批量清理他人文件；
- 不修改已发布的 Flyway 迁移；
- 不关闭测试、安全校验或权限来绕过失败；
- 不把未验证、模拟或静态截图描述为真实全栈验收；
- 不邀请身份未知的协作者；
- 不在未授权情况下部署公网、产生费用或操作生产数据。

相关资料：

- [中文协作规范](../CONTRIBUTING.md)
- [后续优化建议表](optimization-roadmap.md)
- [测试与验收记录](test-report.md)
