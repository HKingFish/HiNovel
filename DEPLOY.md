# HiNovel 部署指南

## 准备

Docker 部署需要 Docker 24+、Compose 2.20+ 和 Node.js 20+。本地运行后端还需要 JDK 21+ 与 Maven 3.9+。Bash 脚本可在 Linux、macOS 或 Windows 的 Git Bash 中运行。

```bash
cp .env.example .env
```

填写 `MYSQL_ROOT_PASSWORD`、`MYSQL_PASSWORD` 及至少 16 字符的 `API_KEY_SECRET`。默认以 root 登录数据库，两个 MySQL 密码填相同值。使用已经建好的非 root 数据库账号时，通过 `MYSQL_USER` 和 `MYSQL_PASSWORD` 提供应用凭据。`API_KEY_SECRET` 用于解密原有 LLM 密钥，迁移或重建应用时须沿用。

Compose 默认把端口发布到 `127.0.0.1`。远程部署可由宿主机 HTTPS 反向代理转发到 Web 端口，或通过 `WEB_BIND_IP` 明确指定监听地址。对外开放之前，请由数据库管理员替换或禁用种子脚本创建的默认管理员凭据。

| 服务 | 宿主机默认端口 | 设置 |
|---|---|---|
| Web / Vite | 3000 | `WEB_PORT` |
| Spring Boot | 8080 | `SERVER_PORT` |
| MySQL | 3306 | `MYSQL_PORT` |
| Redis | 6379 | `REDIS_PORT` |
| Qdrant HTTP / gRPC | 6333 / 6334 | `QDRANT_HTTP_PORT` / `QDRANT_GRPC_PORT` |

容器间使用服务名和容器内端口。修改宿主机端口无需修改容器间连接。开发模式默认从同一份 `.env` 读取端口。

## Docker 部署和更新

```bash
bash docker-build.sh up
bash docker-build.sh status
bash docker-build.sh logs hinovel-server
```

`up` 在宿主机安装前端锁定依赖并构建 `dist/`，随后构建容器镜像并等待各服务通过健康检查。初次启动较慢时，可用 `COMPOSE_WAIT_TIMEOUT=300 bash docker-build.sh up` 延长等待。也可以手动构建前端，再运行 Compose。

```bash
(cd hinovel-web && npm ci && npm run build)
docker compose up --build --wait --wait-timeout 180
```

更新单个应用容器时使用下列命令。已运行的依赖容器不会被重建。

```bash
bash docker-build.sh web
bash docker-build.sh server
```

`bash docker-build.sh restart` 重启现有容器。`bash docker-build.sh down` 移除容器，保留项目上一级 `../data/` 中的 MySQL、Redis、Qdrant、上传文件和日志。更新前数据库与备份目录均沿用原路径。

默认 Web 入口为 `http://localhost:3000`。Nginx 转发 `/api/` 与 `/files/`，后端重建后会重新解析上游地址。本地上传默认使用同源 `/files`。已有文件 URL 使用旧前缀的部署，应保留原 `OSS_LOCAL_URL_PREFIX`，直至单独迁移历史 URL。

## 本地开发

```bash
bash start.sh
```

脚本通过 Compose 读取 `.env`，需 Compose 2.27.2+。它会启动基础设施、打包后端并启动 Vite。使用 `--no-infra` 可以复用已运行的 MySQL、Redis 和 Qdrant。前后端的控制台日志在 `logs/`。按 Ctrl+C 停止本地前后端，基础设施继续运行。[Compose 版本说明](https://github.com/docker/compose/releases/tag/v2.27.2)

也可以先执行 `bash docker-build.sh infra`，再从仓库根目录分别运行下列两条命令：

```bash
mvn -f hinovel-server/pom.xml spring-boot:run
cd hinovel-web && npm ci && npm run dev
```

Docker 与本地开发的默认应用端口相同，请先停止同端口的前后端容器，或在 `.env` 改端口。`QDRANT_PORT` 可覆盖本地 Java 使用的 gRPC 端口。

## 数据库初始化和迁移

MySQL 数据目录为空时，容器先创建 `MYSQL_DB` 指定的库，再按顺序执行 `schema.sql` 和 `seed.sql`。种子账号为 `admin`，邮箱 `admin@hinovel.com`，初始密码 `Admin@123456`。

容器已运行但缺表或种子记录时，可以手动补建。重复执行会保留管理员密码与已有模板。

```bash
bash scripts/init-db.sh
```

```powershell
.\scripts\init-db.ps1
```

`--force`（PowerShell 的 `-Force`）会删除当前容器配置的整个应用库，再重新建表和写入种子。仅在明确要清空目标数据时使用。

已有数据库的表结构升级需单独执行。以小说配置升级为例，先备份，再执行一次迁移：

```bash
bash scripts/mysql_backup.sh
bash scripts/init-db.sh --sql hinovel-server/sql/upgrade-20261118-novel-settings.sql
```

```powershell
.\scripts\init-db.ps1 -SqlFile .\hinovel-server\sql\upgrade-20261118-novel-settings.sql
```

手动执行 SQL 时，客户端必须选定目标库。MySQL 容器的初始化脚本只在新数据目录上运行。调整 `MYSQL_DB` 或容器环境里的密码不会迁移旧库或修改库内密码。

## 备份

`bash scripts/mysql_backup.sh` 使用当前 Compose MySQL 容器的应用库名，生成压缩备份。目录默认是 `../data/mysql/backup`，默认保留 7 天。可通过 shell 环境变量 `MYSQL_BACKUP_DIR` 与 `MYSQL_BACKUP_KEEP_DAYS` 覆盖。

```cron
0 3 * * * /bin/bash /path/to/HiNovel/scripts/mysql_backup.sh >> /var/log/hinovel-backup.log 2>&1
```

备份可解压为 SQL 后用上面的 `--sql` 或 `-SqlFile` 导入。在恢复前核对当前容器的目标库。

## 排查

- MySQL 长时间未就绪时，查看 `bash docker-build.sh logs mysql`。已有数据目录使用原有 root 密码，修改 `.env` 不会修改该密码。
- 本地开发失败时，检查 `logs/hinovel-server.log` 与 `logs/hinovel-web.log`。容器模式使用 `bash docker-build.sh logs <服务名>`。
- RAG 使用 Qdrant gRPC 端口，并要求在应用页面启用有效的 OpenAI 兼容 Embedding 配置。
