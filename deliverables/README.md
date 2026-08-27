# 课程交付物

本目录用于保存可直接提交的课程交付物。正式实验报告 DOCX 按当前要求暂不生成。

| 文件 | 用途 | 生成/提交策略 |
|---|---|---|
| `aurora-blog-defense.pptx` | 10 页中文答辩演示 | 纳入源码仓库 |
| `aurora-blog-demo.mp4` | 真实 Docker 浏览器路径演示，约 6 秒 | 本地保留，并上传 GitHub Release |
| `aurora-blog.war` | Java 17 可执行 WAR | 本地保留，CI 也会生成并保存 |

视频和 WAR 为可再生产物，因此不直接写入 Git 历史，避免仓库体积无谓增长。发布版本会把它们作为 Release 附件提供。
