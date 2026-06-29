#!/bin/bash
# HiNovel AI 小说创作平台 Docker 构建与启动脚本
# 说明：前端产物 dist/ 由宿主机构建（避免容器内 Vite/Rollup 构建卡死），
#       镜像仅托管静态文件。up/build/web 动作会自动触发前端构建。

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

# 统一使用 docker compose（v2）或 docker-compose（v1）
DC=$(command -v docker-compose 2>/dev/null || echo "docker compose")

# 创建宿主机数据挂载目录
init_data_dirs() {
    echo ">>> 初始化数据目录..."
    mkdir -p ../data/mysql/data ../data/mysql/conf ../data/mysql/logs
    mkdir -p ../data/redis/data ../data/redis/conf
    mkdir -p ../data/qdrant/storage ../data/qdrant/snapshots
    mkdir -p ../data/hinovel/uploads ../data/hinovel/logs
    mkdir -p ../data/nginx/logs
}

# 在宿主机构建前端产物 dist/
# 前端在容器内构建会因 overlay2 IO 瓶颈与内存峰值导致卡死，改由宿主机构建
build_frontend() {
    echo ">>> 构建前端产物（宿主机 npm run build）..."
    if ! command -v npm &> /dev/null; then
        echo "错误：未找到 npm，请先安装 Node.js 20+"
        exit 1
    fi
    pushd hinovel-web > /dev/null
    if [ ! -d "node_modules" ]; then
        echo ">>> 安装前端依赖（npm ci）..."
        npm ci
    fi
    echo ">>> 执行 vite build..."
    npm run build
    popd > /dev/null
    echo ">>> 前端产物构建完成：hinovel-web/dist/"
}

ACTION=${1:-"up"}

case $ACTION in
    "up")
        init_data_dirs
        build_frontend
        echo ">>> 构建并启动所有服务..."
        $DC up --build -d
        echo ""
        echo "等待服务就绪（60秒）..."
        sleep 60
        echo ""
        echo "=========================================="
        echo "  所有服务已启动！"
        echo "  前端地址：http://localhost:${WEB_PORT:-3000}"
        echo "  后端地址：http://localhost:${SERVER_PORT:-8080}"
        echo "  Qdrant：  http://localhost:${QDRANT_HTTP_PORT:-6333}/dashboard"
        echo "=========================================="
        $DC ps
        ;;
    "infra")
        init_data_dirs
        echo ">>> 仅启动基础设施（MySQL + Redis + Qdrant）..."
        $DC up -d mysql redis qdrant
        echo "等待基础设施就绪（20秒）..."
        sleep 20
        $DC ps
        ;;
    "down")
        echo ">>> 停止并移除所有容器（数据保留在 ../data 目录）..."
        $DC down
        ;;
    "restart")
        echo ">>> 重启所有服务..."
        $DC restart
        ;;
    "logs")
        SERVICE=${2:-"hinovel-server"}
        echo ">>> 查看 $SERVICE 日志..."
        $DC logs -f "$SERVICE"
        ;;
    "build")
        build_frontend
        echo ">>> 仅构建镜像（不启动）..."
        $DC build
        ;;
    "status")
        echo ">>> 服务状态..."
        $DC ps
        ;;
    # 单独重新部署前端：宿主机构建 dist 后重建并重启 web 容器
    "web")
        build_frontend
        echo ">>> 重新构建并启动前端服务..."
        $DC up -d --build hinovel-web
        echo ""
        echo "前端已更新：http://localhost:${WEB_PORT:-3000}"
        $DC ps hinovel-web
        ;;
    # 单独重新部署后端：重建并重启 server 容器（后端仍为容器内 Maven 构建）
    "server")
        echo ">>> 重新构建并启动后端服务..."
        $DC up -d --build hinovel-server
        echo ""
        echo "后端已更新：http://localhost:${SERVER_PORT:-8080}"
        $DC ps hinovel-server
        ;;
    *)
        echo "用法: $0 [up|infra|down|restart|logs|build|status|web|server]"
        echo "  up      - 构建前端产物并构建/启动所有服务（默认）"
        echo "  infra   - 仅启动基础设施（MySQL + Redis + Qdrant）"
        echo "  down    - 停止并移除容器（数据保留）"
        echo "  restart - 重启所有服务（不重新构建）"
        echo "  logs    - 查看服务日志（默认 hinovel-server），如: $0 logs hinovel-web"
        echo "  build   - 构建前端产物并构建镜像（不启动）"
        echo "  status  - 查看服务状态"
        echo "  web     - 单独重新构建并部署前端（宿主机 build + 重启 web 容器）"
        echo "  server  - 单独重新构建并部署后端（重启 server 容器）"
        exit 1
        ;;
esac
