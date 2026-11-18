#!/usr/bin/env bash

PROJECT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"

die() {
    printf '错误：%s\n' "$*" >&2
    exit 1
}

require_command() {
    command -v "$1" >/dev/null 2>&1 || die "未找到 $1，请安装 $2"
}

compose() (
    cd "$PROJECT_DIR"
    docker compose -f docker-compose.yml "$@"
)

require_compose() {
    require_command docker Docker
    docker compose version >/dev/null 2>&1 || die "需要 Docker Compose 2.20 或以上版本"
}

require_docker_engine() {
    docker info --format '{{.ServerVersion}}' >/dev/null || die "Docker 引擎未就绪"
}

mysql_database() {
    local database
    database="$(compose exec -T mysql printenv MYSQL_DATABASE)" || die "MySQL 容器未运行或缺少 MYSQL_DATABASE"
    database="${database%$'\r'}"
    [[ "$database" =~ ^[A-Za-z0-9_]{1,64}$ ]] || die "MYSQL_DB 只支持 1 到 64 位字母、数字和下划线"
    printf '%s\n' "$database"
}

prepare_frontend() (
    require_command node 'Node.js 20+'
    require_command npm 'npm'
    [[ "$(node -p 'Number(process.versions.node.split(".")[0]) >= 20')" == true ]] || die "需要 Node.js 20+"
    cd "$PROJECT_DIR/hinovel-web"
    if [[ ! -f node_modules/vite/bin/vite.js ]] ||
        ! cmp -s package-lock.json node_modules/.hinovel-lock.json; then
        npm ci
        cp package-lock.json node_modules/.hinovel-lock.json
    fi
)
