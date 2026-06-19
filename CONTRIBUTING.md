# 贡献指南

感谢你对 HiNovel 的关注！欢迎通过 Issue 或 Pull Request 参与项目。

## 开始之前

1. 阅读 [README.md](./README.md) 了解项目概况
2. 阅读 [DEPLOY.md](./DEPLOY.md) 完成本地环境搭建
3. 确认可以在本地正常启动前后端

## 开发环境

| 工具 | 版本 |
|------|------|
| JDK | 21+ |
| Maven | 3.9+ |
| Node.js | 20+ |
| Docker | 24+（用于 MySQL / Redis / Qdrant） |

```bash
cp .env.example .env
bash docker-build.sh infra
cd hinovel-server && mvn spring-boot:run
cd hinovel-web && npm install && npm run dev
```

## 提交规范

推荐使用 [Conventional Commits](https://www.conventionalcommits.org/) 风格：

```
feat: 新增章节导出功能
fix: 修复 Token 刷新失败问题
docs: 更新部署文档
refactor: 重构向量存储工厂
test: 补充 ApiResponse 单元测试
chore: 升级依赖版本
```

## Pull Request 流程

1. Fork 本仓库并在本地创建功能分支

```bash
git checkout -b feat/your-feature-name
```

2. 完成代码修改，确保通过本地检查：

```bash
# 后端
cd hinovel-server && mvn test

# 前端
cd hinovel-web && npm test && npm run lint && npm run build
```

3. 提交 PR 到 `master` 分支，描述中说明：
   - 变更目的与背景
   - 主要改动点
   - 如何验证（测试步骤）

4. 等待 CI 通过后再合并

## 代码规范

### 后端（Java）

- 遵循现有 DDD 分层：`domain` / `application` / `infrastructure` / `interfaces`
- 新增类、方法、字段需添加中文注释
- 枚举类用于固定取值集合，通过 `@see` 引用相关枚举
- 不使用 `record` 关键字创建类
- 变更尽量最小化，避免影响已有逻辑

### 前端（Vue / TypeScript）

- 使用 ESLint + Prettier（提交前 Husky 会自动执行 lint-staged）
- 组件与 API 层保持现有目录结构
- 新增页面需在 `router/index.ts` 注册路由

## 安全要求

**禁止**在 PR 中提交以下内容：

- `.env` 或任何含真实密钥的文件
- 个人服务器 IP、SSH 密码、数据库密码
- LLM API Key、OSS AccessKey 等凭证

如发现安全问题，请参阅 [SECURITY.md](./SECURITY.md) 私下报告，勿公开 Issue。

## Issue 指南

提交 Issue 时请尽量包含：

- 问题描述与期望行为
- 复现步骤
- 环境信息（OS、JDK/Node 版本、部署方式）
- 相关日志或截图

## 许可证

提交代码即表示你同意你的贡献在 [Apache License 2.0](./LICENSE) 下发布。
