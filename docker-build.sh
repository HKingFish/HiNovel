#!/bin/bash
# HiNovel AI 小说创作平台 Docker 构建与启动脚本

set -e

echo "=========================================="
echo "  HiNovel AI 小说创作平台 - Docker 启动脚本"
echo "=========================================="

# 检查 Docker
if ! command -v docker &> /dev/null; then
    echo "错误：未找到 Docker，请先安装 Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "错误：未找到 docker-compose，请先安装"
    exit 1
fi

# 创建宿主机数据挂载目录
init_data_dirs() {
    echo ">>> 初始化数据目录..."
    mkdir -p data/mysql/data data/mysql/conf data/mysql/logs
    mkdir -p data/redis/data data/redis/conf
    mkdir -p data/qdrant/storage data/qdrant/snapshots
    mkdir -p data/hinovel/uploads data/hinovel/logs
    mkdir -p data/nginx/logs
}

ACTION=${1:-"up"}

case $ACTION in
    "up")
        init_data_dirs
        echo ">>> 构建并启动所有服务..."
        docker-compose up --build -d
        echo ""
        echo "等待服务就绪（60秒）..."
        sleep 60
        echo ""
        echo "=========================================="
        echo "  所有服务已启动！"
        echo "  前端地址：http://localhost:${WEB_PORT:-80}"
        echo "  后端地址：http://localhost:${SERVER_PORT:-8080}"
        echo "  Qdrant：  http://localhost:${QDRANT_HTTP_PORT:-6333}/dashboard"
        echo "=========================================="
        docker-compose ps
        ;;
    "infra")
        init_data_dirs
        echo ">>> 仅启动基础设施（MySQL + Redis + Qdrant）..."
        docker-compose up -d mysql redis qdrant
        echo "等待基础设施就绪（20秒）..."
        sleep 20
        docker-compose ps
        ;;
    "down")
        echo ">>> 停止并移除所有容器（数据保留在 ./data 目录）..."
        docker-compose down
        ;;
    "restart")
        echo ">>> 重启所有服务..."
        docker-compose restart
        ;;
    "logs")
        SERVICE=${2:-"hinovel-server"}
        echo ">>> 查看 $SERVICE 日志..."
        docker-compose logs -f "$SERVICE"
        ;;
    "build")
        echo ">>> 仅构建镜像（不启动）..."
        docker-compose build
        ;;
    "status")
        echo ">>> 服务状态..."
        docker-compose ps
        ;;
    *)
        echo "用法: $0 [up|infra|down|restart|logs|build|status]"
        echo "  up      - 构建并启动所有服务（默认）"
        echo "  infra   - 仅启动基础设施（MySQL + Redis + Qdrant）"
        echo "  down    - 停止并移除所有容器（数据保留）"
        echo "  restart - 重启所有服务"
        echo "  logs    - 查看服务日志（默认 hinovel-server）"
        echo "  build   - 仅构建镜像"
        echo "  status  - 查看服务状态"
        exit 1
        ;;
esac
