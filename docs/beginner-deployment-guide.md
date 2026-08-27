# Aurora Blog Platform 从零到一部署指南（小白版）

> 适用对象：第一次接触 Git、Docker 或前后端项目的同学。请按顺序执行，不要跳步。
>
> 本指南完成的是**本机 Docker 部署**，最终访问地址为 `http://localhost:8080`。它不代表公网服务器已经部署完成。

## 1. 你最终会得到什么

完成本指南后，你的电脑会运行 4 个容器：

| 服务 | 作用 | 是否直接暴露给浏览器 |
|---|---|---|
| `frontend` | Nginx 与 Vue 页面，统一入口 | 是，端口 `8080` |
| `backend` | Spring Boot REST API | 否，由 Nginx 转发 |
| `mysql` | 保存用户、文章、评论等业务数据 | 否 |
| `redis` | 保存登录会话、限流和热门排行辅助数据 | 否 |

Docker 还会创建 3 个命名卷：

- `mysql_data`：MySQL 数据；
- `redis_data`：Redis 持久化数据；
- `uploads_data`：上传的图片。

普通的重启、停止或重新构建不会删除这些卷。

## 2. 先认识几个词

| 名词 | 小白解释 |
|---|---|
| Git | 下载代码、记录修改和参与团队协作的工具 |
| GitHub | 在线保存 Git 仓库和进行 Issue、PR、Review 的平台 |
| Docker 镜像 | 安装好运行环境的“软件安装包” |
| Docker 容器 | 镜像真正运行起来后的进程 |
| Docker Compose | 按 `docker-compose.yml` 一次启动多个容器 |
| `.env` | 只保存在本机的密码和环境变量文件，严禁提交到 Git |
| 健康检查 | Docker 用来判断服务是否真正可用的自动检查 |
| 命名卷 | Docker 管理的持久化数据目录，容器重建后仍保留 |

## 3. 电脑要求

建议最低配置：

- 64 位 Windows 10/11、macOS 12+ 或常见 64 位 Linux；
- 8GB 内存，建议 16GB；
- 至少 10GB 可用磁盘空间；
- 能访问 GitHub 和 Docker 镜像仓库的网络；
- 当前用户有安装软件和启动 Docker 的权限。

项目通过多阶段 Docker 构建 Java 和前端，因此**无需单独安装 JDK、Maven、MySQL、Redis 或 Node.js**。

## 4. 安装必需软件

### 4.1 安装 Git

- Windows：安装 [Git for Windows](https://git-scm.com/download/win)，安装时保留默认选项即可。
- macOS：终端执行 `git --version`，系统可能提示安装 Command Line Tools；也可以通过 Homebrew 安装。
- Linux：使用系统软件包管理器安装 `git`。

安装完成后打开终端：

```bash
git --version
```

看到类似 `git version 2.x.x` 即成功。

### 4.2 安装 Docker

桌面电脑推荐安装 Docker Desktop：

- Windows：启用 WSL 2 后安装 Docker Desktop；
- macOS：根据 Intel 或 Apple 芯片选择对应版本；
- Linux：安装 Docker Engine 和 Docker Compose 插件。

启动 Docker Desktop，等菜单栏或任务栏中的 Docker 图标显示为正常运行，再执行：

```bash
docker version
docker compose version
```

两个命令都能显示版本号才继续。

> 如果提示无法连接 Docker daemon，通常是 Docker Desktop 没启动，不是项目代码错误。

## 5. 下载项目

选择一个容易找到的目录，在终端执行：

```bash
git clone https://github.com/lzl051120/aurora-blog-platform.git
cd aurora-blog-platform
```

确认当前目录正确：

```bash
pwd
ls
```

你应该能看到 `README.md`、`docker-compose.yml`、`backend`、`frontend` 和 `scripts`。

如果不想安装 Git，也可以在 GitHub 仓库点击“Code → Download ZIP”，解压后进入目录。但后续更新和团队协作仍建议使用 Git 克隆。

## 6. 生成本机密码

### 6.1 macOS、Linux、Git Bash 或 WSL

在仓库根目录执行：

```bash
./scripts/setup-env.sh
```

如果提示没有执行权限：

```bash
chmod +x ./scripts/setup-env.sh
./scripts/setup-env.sh
```

脚本会生成本机专用的 `.env`，并在终端显示管理员用户名和随机密码。请把管理员密码保存到自己的密码管理器。

### 6.2 如果提示找不到 `openssl`

先安装 OpenSSL，或复制模板后手工填写**不同的随机密码**：

```bash
cp .env.example .env
```

然后用文本编辑器打开 `.env`，替换所有“请使用……”占位文字。管理员密码至少 12 位，并混合大小写字母、数字和符号。

### 6.3 安全检查

执行：

```bash
git status --short
```

正常情况下不应出现 `.env`，因为它已被 `.gitignore` 忽略。

> 不要截图传播 `.env`，不要把它发到群聊、Issue、PR 或提交记录中，也不要多人共用管理员密码。

## 7. 启动完整系统

先检查 Compose 配置：

```bash
docker compose config -q
```

没有输出通常代表配置合法。然后执行：

```bash
docker compose up --build -d
```

参数含义：

- `up`：创建并启动服务；
- `--build`：根据当前源码重新构建前后端；
- `-d`：在后台运行，不占住终端。

第一次需要下载基础镜像和依赖，可能持续几分钟。不要因为终端暂时没有新输出就关闭 Docker。

## 8. 确认所有服务健康

执行：

```bash
docker compose ps
```

等待 `frontend`、`backend`、`mysql`、`redis` 的状态都出现 `healthy`。后端会等待 MySQL 和 Redis 就绪，因此启动顺序不同是正常现象。

再验证真实 API：

```bash
curl -fsS http://localhost:8080/api/v1/home
```

Windows PowerShell 没有可用 `curl` 时可以执行：

```powershell
Invoke-WebRequest http://localhost:8080/api/v1/home
```

返回 JSON 或 HTTP 200 代表前端 Nginx 已经成功转发到后端。

## 9. 第一次登录和功能检查

1. 浏览器打开 [http://localhost:8080](http://localhost:8080)；
2. 点击“登录”；
3. 用户名默认为 `admin`；
4. 密码是执行 `setup-env.sh` 时生成的随机密码；
5. 登录后进入管理后台；
6. 新建并发布一篇测试文章；
7. 退出登录，在公开文章列表搜索刚才的标题；
8. 注册一个普通账号，尝试评论或留言；
9. 管理员重新登录并审核内容。

忘记密码时，可在只允许自己观看的终端中查看：

```bash
awk -F= '/^BOOTSTRAP_ADMIN_PASSWORD=/{print $2}' .env
```

注意：管理员仅在数据库初始化时创建。已有数据库中修改 `.env` 的管理员密码不会自动覆盖原密码。

## 10. 日常使用命令

| 目的 | 命令 | 是否影响数据 |
|---|---|---|
| 查看状态 | `docker compose ps` | 否 |
| 查看后端日志 | `docker compose logs --tail=200 backend` | 否 |
| 持续查看日志 | `docker compose logs -f backend frontend` | 否，按 `Ctrl+C` 只退出查看 |
| 停止服务 | `docker compose stop` | 不删除容器和卷 |
| 再次启动 | `docker compose start` | 不删除数据 |
| 重启全部服务 | `docker compose restart` | 不删除数据 |
| 重新构建 | `docker compose up --build -d` | 不删除命名卷 |
| 停止并删除容器 | `docker compose down` | 不删除命名卷 |
| 查看磁盘卷 | `docker volume ls` | 否 |

> **危险操作：** `docker compose down -v` 会删除 MySQL、Redis 和上传文件卷。除非已经备份且明确要清空全部本地数据，否则不要执行。

## 11. 数据库备份与恢复

### 11.1 备份

保持 MySQL 容器运行，在仓库根目录执行：

```bash
./scripts/backup-db.sh
```

备份会出现在 `backups/aurora_blog_日期_时间.sql.gz`。该目录被 Git 忽略，请把重要备份复制到另一块磁盘或加密存储。

### 11.2 恢复

恢复会按备份内容重建当前业务表，先确认备份文件正确：

```bash
./scripts/restore-db.sh backups/aurora_blog_YYYYMMDD_HHMMSS.sql.gz
```

脚本要求输入大写 `RESTORE` 才继续。恢复前建议再做一次当前数据库备份。

## 12. 更新到仓库最新代码

先确认自己没有未保存修改：

```bash
git status
```

如果工作区干净：

```bash
git pull --ff-only origin main
docker compose up --build -d
docker compose ps
```

`--ff-only` 会在本地历史与远端分叉时停止，避免小白在不知情的情况下产生合并提交。团队成员不要直接在 `main` 开发，完整流程见 [GitHub 团队协作指南](github-team-collaboration.md)。

## 13. 一键检查

系统运行后执行：

```bash
./scripts/verify.sh
```

它会检查 Compose 配置、容器状态、真实首页 API，以及前端 lint、类型、组件测试和生产构建。首次执行前端检查需要本机已有 `frontend/node_modules`；若没有，请在 `frontend` 目录执行 `npm ci`，或直接依赖 GitHub Actions 完成完整远端验证。

## 14. 常见问题

| 现象 | 常见原因 | 处理办法 |
|---|---|---|
| `docker: command not found` | Docker 未安装或终端未重新打开 | 安装 Docker Desktop，重启终端 |
| `Cannot connect to Docker daemon` | Docker Desktop 未启动 | 启动 Docker Desktop并等待就绪 |
| `permission denied: setup-env.sh` | 脚本没有执行权限 | 执行 `chmod +x ./scripts/setup-env.sh` |
| `port is already allocated` | 8080 被其他程序占用 | 停止占用程序，或把 Compose 中 `8080:80` 改成例如 `8088:80` |
| MySQL 长时间不健康 | 磁盘不足、旧卷配置不兼容或密码变化 | 查看 `docker compose logs --tail=200 mysql`，不要直接删卷 |
| 后端不健康 | MySQL/Redis 未就绪或环境变量错误 | 查看 backend、mysql、redis 三类日志 |
| 页面打开但接口报错 | 后端不健康或 Nginx 转发失败 | 先执行 `docker compose ps` 和真实 API 检查 |
| 管理员无法登录 | 密码输错、登录限流或旧数据库已有管理员 | 检查 `.env`，稍后重试；不要通过改库明文密码 |
| 拉取镜像很慢 | 网络或镜像仓库连接问题 | 更换稳定网络后重试，不要反复删除项目 |
| 修改代码后页面没变化 | 容器仍是旧镜像 | 执行 `docker compose up --build -d` |

端口排查：

```bash
# macOS / Linux
lsof -nP -iTCP:8080 -sTCP:LISTEN

# Windows PowerShell
netstat -ano | findstr :8080
```

## 15. 查看更完整的技术资料

- [部署与运维说明](deployment.md)
- [系统架构](architecture.md)
- [API 说明](api.md)
- [数据库 ER 图](er-diagram.md)
- [测试与验收记录](test-report.md)
- [后续优化建议](optimization-roadmap.md)
- [GitHub 团队协作指南](github-team-collaboration.md)
