# HiNovel 部署指南

本文档介绍 HiNovel 的 Docker 部署与本地开发部署方式。

## 环境要求

| 组件 | 版本要求 |
|------|----------|
| Docker | 24.0+ |
| Docker Compose | 2.20+ |
| 内存 | 建议 4 GB 以上 |
| 磁盘 | 建议 20 GB 以上 |

本地开发额外需要：JDK 21+、Maven 3.9+、Node.js 20+。

---

## 服务与端口

| 服务 | 默认端口 | 环境变量 | 说明 |
|------|----------|----------|------|
| 前端（Nginx） | 80 | `WEB_PORT` | Web 访问入口 |
| 后端（Spring Boot） | 8080 | `SERVER_PORT` | REST API |
| MySQL | 3306 | `MYSQL_PORT` | 关系型数据库 |
| Redis | 6379 | `REDIS_PORT` | 缓存 / Session |
| Qdrant HTTP | 6333 | `QDRANT_HTTP_PORT` | 向量库 REST / 管理面板 |
| Qdrant gRPC | 6334 | `QDRANT_GRPC_PORT` | Java 客户端连接端口 |

---

## 部署前准备

### 1. 获取代码

```bash
git clone https://github.com/HKingFish/HiNovel.git
cd HiNovel
```

### 2. 配置环境变量

```bash
cp .env.example .env
```

编辑 `.env`，**生产环境务必修改所有默认密码与密钥**：

```bash
# 必填
MYSQL_PASSWORD=your_mysql_password
MYSQL_ROOT_PASSWORD=your_root_password
API_KEY_SECRET=your_random_secret_16chars
```

> **安全提示**：`API_KEY_SECRET` 用于加密数据库中存储的 LLM API Key，部署后请勿随意更改，否则已加密的 Key 将无法解密。

### 3. 数据目录

容器数据通过 Volume 挂载到宿主机 `./data` 目录，删除容器不会丢失数据：

```
data/
├── mysql/
│   ├── data/          # MySQL 数据文件
│   ├── conf/          # 自定义配置
│   ├── logs/          # 日志
│   └── backup/        # 备份（由 scripts/mysql_backup.sh 生成）
├── redis/
│   ├── data/
│   └── conf/
├── qdrant/
│   ├── storage/       # 向量数据
│   └── snapshots/
├── hinovel/
│   ├── uploads/       # 上传文件
│   └── logs/          # 应用日志
└── nginx/
    └── logs/
```

首次启动时 `docker-build.sh` 会自动创建上述目录。

---

## Docker Compose 完整部署

### 构建并启动全部服务

```bash
bash docker-build.sh up
```

脚本会构建前后端镜像，启动 MySQL、Redis、Qdrant、后端与前端，并等待约 60 秒后输出服务状态。

### 常用命令

```bash
bash docker-build.sh status    # 查看服务状态
bash docker-build.sh logs        # 查看后端日志（默认 hinovel-server）
bash docker-build.sh logs hinovel-web
bash docker-build.sh restart     # 重启所有服务
bash docker-build.sh down        # 停止并移除容器（数据保留在 ./data）
bash docker-build.sh build       # 仅构建镜像，不启动
```

也可直接使用 Docker Compose：

```bash
docker compose up -d --build
docker compose ps
docker compose logs -f hinovel-server
```

### 验证部署

1. 访问 http://localhost（或你配置的 `WEB_PORT`）
2. 使用默认管理员登录：`admin` / `Admin@123456`（生产环境请立即修改密码）
3. 在「LLM 配置」中添加你的大模型 Provider
4. 创建小说并测试 AI 续写 / 对话功能

---

## 数据库初始化

### 自动初始化（推荐）

MySQL 容器**首次启动**且 `data/mysql/data` 为空时，会自动执行：

1. `hinovel-server/sql/schema.sql` — 建表
2. `hinovel-server/sql/seed.sql` — 初始数据（含 admin 账号、内置 Agent 模板）

> **注意**：初始化在 **MySQL 容器首次启动**时执行，**不在** Docker 镜像构建阶段执行。若 `data/mysql/data` 已有数据，脚本不会再次运行。

### 手动初始化

容器已运行但库表未创建，或需要重新灌库时：

```powershell
# Windows
.\scripts\init-db.ps1

# 清空并重建（会删除 hinovel_platform 全部数据）
.\scripts\init-db.ps1 -Force
```

```bash
# Linux / macOS / Git Bash
bash scripts/init-db.sh
bash scripts/init-db.sh --force
```

若需完全重新自动初始化，停止容器后删除 MySQL 数据目录再启动：

```powershell
docker compose down
Remove-Item -Recurse -Force .\data\mysql\data\*
docker compose up -d mysql
```

默认管理员：

| 字段 | 值 |
|------|-----|
| 用户名 | `admin` |
| 邮箱 | `admin@hinovel.com` |
| 密码 | `Admin@123456` |

---

## 本地开发部署

适合需要调试前后端代码的场景。

### 1. 启动基础设施

```bash
bash docker-build.sh infra
```

仅启动 MySQL、Redis、Qdrant，不启动应用容器。

### 2. 配置 `.env`

本地开发时 `.env` 中数据库与中间件地址保持 `localhost` 即可（与 `docker-compose.yml` 中基础设施端口映射一致）。

### 3. 启动后端

```bash
cd hinovel-server
mvn spring-boot:run
```

或使用一键脚本（含构建、启动前后端）：

```bash
bash start.sh
```

### 4. 启动前端

```bash
cd hinovel-web
npm install
npm run dev
```

- 前端：http://localhost:3000
- 后端：http://localhost:8080
- API 文档（如已启用 Knife4j）：http://localhost:8080/doc.html

---

## 更新已运行的服务

更新代码后，只需重建对应服务镜像，**MySQL / Redis / Qdrant 不会被重启**。

```bash
# 更新后端
docker compose up -d --build hinovel-server

# 更新前端
docker compose up -d --build hinovel-web

# 同时更新
docker compose up -d --build hinovel-server hinovel-web
```

查看更新日志：

```bash
docker compose logs -f hinovel-server
docker compose logs -f hinovel-web
```

> **注意**：必须加 `--build` 才会重新编译代码；仅 `docker compose up -d` 会复用旧镜像。

### 数据库迁移

若后端包含 Flyway 迁移脚本，容器重启后会自动执行。若有手动 SQL 脚本，请在 MySQL 中自行执行：

```bash
docker exec -i hinovel-mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" hinovel_platform < your_script.sql
```

---

## 生产环境建议

1. **修改所有默认密码**：`MYSQL_ROOT_PASSWORD`、`REDIS_PASSWORD`、`API_KEY_SECRET`
2. **使用 HTTPS**：在 Nginx 或云负载均衡层配置 TLS 证书
3. **限制端口暴露**：生产环境可仅暴露 80/443，MySQL、Redis、Qdrant 不映射到公网
4. **定期备份**：使用 `scripts/mysql_backup.sh` 配合 crontab 定时备份 MySQL

```bash
# 示例：每天凌晨 3 点备份
0 3 * * * /path/to/HiNovel/scripts/mysql_backup.sh >> /var/log/hinovel-backup.log 2>&1
```

5. **日志与监控**：应用日志位于 `data/hinovel/logs/`，建议接入日志采集与告警
6. **Embedding 选型**：
   - `openai`（推荐入门）：调用 OpenAI 兼容 API，配置简单
   - `onnx`：本地 BGE-M3 模型，无 API 费用，需自行准备模型文件

---

## 常见问题

### 后端启动失败：无法连接 MySQL

- 确认 `docker compose ps` 中 `hinovel-mysql` 状态为 healthy
- 检查 `.env` 中 `MYSQL_PASSWORD` 与 `MYSQL_ROOT_PASSWORD` 是否一致
- Docker 部署时后端通过服务名 `mysql` 连接，无需改 `MYSQL_HOST`

### Qdrant 连接失败

- 确认 Qdrant 容器 healthy
- Java 客户端使用 gRPC 端口 `6334`，不是 HTTP 端口 `6333`

### Embedding / RAG 不可用

- 确认 `OPENAI_EMBEDDING_API_KEY` 已配置且有效
- 或切换为 `EMBEDDING_TYPE=onnx` 并配置本地模型路径

### 前端无法访问 API

- Docker 部署：Nginx 已配置反向代理，直接访问前端端口即可
- 本地开发：确认后端在 8080 端口运行，Vite 代理配置见 `hinovel-web/vite.config.ts`
