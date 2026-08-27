# Aurora Blog Platform v1.0.0 发布说明

这是 Aurora Blog Platform 的首次完整课程交付版本，由仓库所有者 `lzl051120` 创建并维护。

## 主要能力

- 现代编辑出版风格的公开博客、响应式阅读、搜索和分类。
- 注册登录、个人中心、评论与留言的待审工作流。
- 集文章创作、审核治理、用户、分类和媒体于一体的管理后台。
- Spring Security、BCrypt、HttpOnly Cookie、CSRF、角色授权、XSS 清洗、安全上传与 Redis 限流。
- MySQL 事实源、Flyway 迁移、Redis 会话和热门排行、上传持久卷。

## 交付附件

- `aurora-blog.war`：Java 17 可执行 WAR。
- `aurora-blog-defense.pptx`：10 页中文答辩 PPT。
- `aurora-blog-demo.mp4`：本地 Docker 全栈真实浏览器演示视频。

## 验证范围

- 前后端自动测试、静态检查与生产构建。
- Docker 五服务健康与真实 API 联通。
- 1440px、768px、390px 三视口浏览器闭环与 WCAG 2.2 AA 扫描。
- MySQL 重启持久化、上传文件重启持久化、备份至临时库恢复演练。

## 使用边界

本版本验收目标为本地 Docker 一键部署。公网部署需要另行提供服务器或托管平台凭据；正式实验报告 DOCX 不包含在本次发布中。
