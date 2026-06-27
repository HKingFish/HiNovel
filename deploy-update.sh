#!/bin/bash
# HiNovel AI 小说创作平台 - 一键更新部署脚本
# 功能：拉取最新代码并重新构建部署前后端服务

set -e

echo "=========================================="
echo "  HiNovel AI 小说创作平台 - 一键更新部署"
echo "=========================================="

# 检查 git
if ! command -v git &> /dev/null; then
    echo "错误：未找到 git，请先安装 git"
    exit 1
fi

# 检查 Docker
if ! command -v docker &> /dev/null; then
    echo "错误：未找到 Docker，请先安装 Docker"
    exit 1
fi

# 检查 docker-compose
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "错误：未找到 docker-compose，请先安装"
    exit 1
fi

# 获取当前目录
PROJECT_DIR=$(cd "$(dirname "$0")" && pwd)
echo ">>> 项目目录: $PROJECT_DIR"

# 进入项目目录
cd "$PROJECT_DIR"

# 拉取最新代码
echo ""
echo ">>> 拉取最新代码..."
git pull

# 获取当前分支和提交信息
BRANCH=$(git rev-parse --abbrev-ref HEAD)
COMMIT=$(git rev-parse --short HEAD)
COMMIT_MSG=$(git log -1 --format="%s")

echo ">>> 当前分支: $BRANCH"
echo ">>> 最新提交: $COMMIT - $COMMIT_MSG"

# 重新构建并启动前后端服务
echo ""
echo ">>> 重新构建后端服务..."
docker compose up -d --build hinovel-server

echo ""
echo ">>> 重新构建前端服务..."
docker compose up -d --build hinovel-web

# 等待服务就绪
echo ""
echo ">>> 等待服务就绪（30秒）..."
sleep 30

# 显示服务状态
echo ""
echo "=========================================="
echo "  服务更新完成！"
echo "=========================================="
docker compose ps

echo ""
echo ">>> 查看日志命令:"
echo "  docker compose logs -f hinovel-server"
echo "  docker compose logs -f hinovel-web"