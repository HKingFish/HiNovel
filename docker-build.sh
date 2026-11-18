#!/usr/bin/env bash
set -euo pipefail

source "$(dirname -- "${BASH_SOURCE[0]}")/scripts/compose-common.sh"

usage() {
    cat <<'EOF'
用法：bash docker-build.sh [up|infra|build|web|server|status|logs|restart|down]
  up       构建并启动全部服务（默认）
  infra    仅启动 MySQL、Redis、Qdrant
  build    构建前后端镜像
  web      重建前端镜像并更新前端
  server   重建后端镜像并更新后端
  status   显示服务状态
  logs     跟踪指定服务日志（默认 hinovel-server）
  restart  重启已创建的服务
  down     移除容器，保留数据目录
EOF
}

build_frontend() (
    prepare_frontend
    cd "$PROJECT_DIR/hinovel-web"
    npm run build
    [[ -s dist/index.html ]] || die "前端缺少 dist/index.html"
)

start_services() {
    if ! compose up --wait --wait-timeout "${COMPOSE_WAIT_TIMEOUT:-180}" "$@"; then
        compose ps -a >&2 || true
        die "服务未就绪，请用 bash docker-build.sh logs <服务名> 查看日志"
    fi
}

action="${1:-up}"
case "$action" in
    -h|--help|help) usage; exit 0 ;;
    up|infra|build|web|server|status|logs|restart|down) ;;
    *) usage >&2; die "未知操作：$action" ;;
esac
[[ $# -le 1 || ( "$action" == logs && $# -eq 2 ) ]] || die "参数不正确，请查看 --help"
require_compose
require_docker_engine

case "$action" in
    up)
        build_frontend
        start_services --build
        compose ps
        ;;
    infra)
        start_services mysql redis qdrant
        compose ps mysql redis qdrant
        ;;
    build)
        build_frontend
        compose build
        ;;
    web)
        build_frontend
        start_services --no-deps --build hinovel-web
        compose ps hinovel-web
        ;;
    server)
        start_services --no-deps --build hinovel-server
        compose ps hinovel-server
        ;;
    status) compose ps -a ;;
    logs) compose logs --tail 100 -f "${2:-hinovel-server}" ;;
    restart) compose restart ;;
    down) compose down ;;
esac
