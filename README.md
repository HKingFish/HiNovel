# HiNovel

[![CI](https://github.com/HKingFish/HiNovel/actions/workflows/ci.yml/badge.svg)](https://github.com/HKingFish/HiNovel/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](./LICENSE)

HiNovel 是一个面向小说创作者的 AI 辅助写作平台，提供章节编辑、人物图谱、RAG 知识库检索、多 Agent 协作等能力，帮助作者更高效地完成长篇创作。

**重要提示**：本项目是兴趣使然，最初是作为 LangChain4j 框架的学习实践项目，全程通过 Vibe Coding 完成开发。目前仍处于**开发测试阶段（各种 BUG 层出不穷）**，主要用于个人技术研究和学习。
 > ⚠️ **警告**：本项目未经系统性测试，存在已知和未知的 Bug，**不推荐用于任何生产环境或实际业务场景**。
 >
> 

 - **项目体验地址 ：[http://hinovel.haowl.cn](http://hinovel.haowl.cn)**

## 功能特性

- **小说创作**：基于 Tiptap 的富文本章节编辑器，支持自动保存、版本管理与章节大纲
- **人物图谱**：可视化人物关系网络，辅助保持人设一致性
- **AI 助手**：流式对话、续写、改写，支持 SSE 实时输出
- **Agent 系统**：可配置系统提示词、工具装配与 Token 限制的多 Agent 工作流
- **RAG 知识库**：基于 Qdrant 向量数据库的语义检索，支持章节内容同步与召回
- **LLM 管理**：多 Provider 配置、Embedding 模型配置、调用日志与 Token 统计
- **用户体系**：注册登录、Sa-Token 认证、API Key 加密存储

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 21、Spring Boot 3.5、MyBatis-Plus、LangChain4j |
| 前端 | Vue 3、TypeScript、Vite、Element Plus、Tiptap、ECharts |
| 基础设施 | MySQL 8、Redis 7、Qdrant、Docker Compose |
| 认证 | Sa-Token |
| 向量检索 | Qdrant（gRPC） |

## 项目结构

```
HiNovel/
├── hinovel-server/     # Spring Boot 后端（DDD 分层）
├── hinovel-web/        # Vue 3 前端
├── docker-compose.yml  # 容器编排
├── docker-build.sh     # Docker 构建与启动脚本
├── start.sh            # 本地开发一键启动脚本
├── scripts/            # 运维辅助脚本（如 MySQL 备份）
├── .env.example        # 环境变量模板
├── DEPLOY.md           # 部署文档
├── CHANGELOG.md        # 版本更新日志
└── README.md
```

后端主要模块：

- `user` — 用户认证与账号管理
- `novel` — 小说、章节、人物、大纲、批注
- `agent` — Agent 配置与执行
- `ai` — LLM 调用、Embedding、向量存储、工具链
- `common` / `infra` — 公共组件与基础设施

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.9+
- Node.js 20+
- Docker 24+ 与 Docker Compose 2.20+

### 1. 克隆并配置

```bash
git clone https://github.com/HKingFish/HiNovel.git
cd HiNovel
cp .env.example .env
```

编辑 `.env`，至少填写以下必填项：

| 变量 | 说明 |
|------|------|
| `MYSQL_PASSWORD` | MySQL 密码 |
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码（Docker 部署时使用） |
| `API_KEY_SECRET` | LLM API Key 加密密钥（至少 16 字符） |
| `OPENAI_EMBEDDING_API_KEY` | Embedding 接口密钥（使用 OpenAI 兼容 API 时） |

### 2. Docker 一键部署（推荐）

```bash
bash docker-build.sh up
```

启动完成后：

- 前端：http://localhost:80
- 后端 API：http://localhost:8080
- Qdrant 面板：http://localhost:6333/dashboard

### 3. 本地开发模式

```bash
# 方式一：一键启动（基础设施 + 后端 + 前端）
bash start.sh

# 方式二：分步启动
bash docker-build.sh infra          # 仅启动 MySQL / Redis / Qdrant
cd hinovel-server && mvn spring-boot:run
cd hinovel-web && npm install && npm run dev
```

本地开发时前端默认运行在 http://localhost:3000，API 请求通过 Vite 代理转发至后端。

### 4. 默认管理员账号

首次部署后，数据库初始化脚本会创建默认管理员账号：

| 字段 | 值 |
|------|-----|
| 用户名 | `admin` |
| 邮箱 | `admin@hinovel.com` |
| 密码 | `Admin@123456` |

**生产环境部署后请立即修改默认密码**，并确保 `.env` 中已设置强密码的 `API_KEY_SECRET` 与 `MYSQL_ROOT_PASSWORD`。

若数据库未自动初始化（例如 `data/mysql/data` 已有旧数据），可手动执行：

```powershell
.\scripts\init-db.ps1
```

更多部署细节、端口说明与更新流程请参阅 [DEPLOY.md](./DEPLOY.md)。

## 配置说明

所有敏感配置均通过 `.env` 注入，**请勿将 `.env` 提交到版本库**。

主要配置项：

- **数据库**：MySQL 连接信息
- **缓存**：Redis 连接与密码
- **向量库**：Qdrant 主机与端口
- **文件存储**：本地存储或阿里云 OSS
- **Embedding**：OpenAI 兼容 API 或本地 ONNX 模型（BGE-M3）
- **安全**：`API_KEY_SECRET` 用于加密用户配置的 LLM API Key

完整变量说明见 [.env.example](./.env.example)。

## 开发

```bash
# 后端测试
cd hinovel-server && mvn test

# 前端测试与 Lint
cd hinovel-web && npm test && npm run lint
```

## 开源说明

本项目采用 [Apache License 2.0](./LICENSE) 开源。

如果你在历史版本中发现过敏感信息，请在公开仓库前**轮换所有已暴露的密钥**（数据库密码、SSH 密码、LLM API Key、OSS 密钥等）。

## 贡献

欢迎提交 Issue 与 Pull Request，详见 [CONTRIBUTING.md](./CONTRIBUTING.md)。

参与社区互动时请遵守 [行为准则](./CODE_OF_CONDUCT.md)。版本变更记录见 [CHANGELOG.md](./CHANGELOG.md)。

提交前请确保：

1. 不包含任何密钥、密码或个人服务器信息
2. 本地可正常构建与启动（`mvn test`、`npm test`、`npm run lint`）
3. 遵循现有代码风格

安全问题请私下报告，参见 [SECURITY.md](./SECURITY.md)。

## License

Apache License 2.0 — 详见 [LICENSE](./LICENSE)
