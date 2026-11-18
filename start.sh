#!/usr/bin/env bash
set -euo pipefail

source "$(dirname -- "${BASH_SOURCE[0]}")/scripts/compose-common.sh"

if [[ "${1:-}" == --help || "${1:-}" == -h ]]; then
    printf '用法：bash start.sh [--no-infra]\n启动本地前后端。--no-infra 使用已运行的基础设施。\n'
    exit 0
fi
[[ $# -eq 0 || ( $# -eq 1 && "$1" == --no-infra ) ]] || die "参数不正确，请查看 --help"

require_compose
require_command mvn 'Maven 3.9+'
require_command curl curl
java_cmd="${JAVA_HOME:+$JAVA_HOME/bin/}java"
require_command "$java_cmd" 'JDK 21+'
java_version="$("$java_cmd" -version 2>&1)"
[[ $java_version =~ version\ \"([0-9]+) && ${BASH_REMATCH[1]} -ge 21 ]] || die "需要 JDK 21 或以上版本"

# 由 Compose 读取 dotenv，避免把 .env 当作 shell 脚本执行。
if ! settings="$(compose config --environment)"; then
    die "无法读取 Compose 环境变量，本地一键启动需要 Docker Compose 2.27.2 或以上版本"
fi
while IFS= read -r line; do
    key="${line%%=*}"
    value="${line#*=}"
    case "$key" in
        SERVER_PORT|WEB_PORT|MYSQL_HOST|MYSQL_PORT|MYSQL_DB|MYSQL_USER|MYSQL_PASSWORD|MYSQL_ROOT_PASSWORD|\
        REDIS_HOST|REDIS_PORT|REDIS_PASSWORD|QDRANT_HOST|QDRANT_PORT|QDRANT_GRPC_PORT|API_KEY_SECRET|\
        OSS_TYPE|OSS_LOCAL_PATH|OSS_LOCAL_URL_PREFIX|OSS_ALIYUN_ENDPOINT|OSS_ALIYUN_ACCESS_KEY_ID|\
        OSS_ALIYUN_ACCESS_KEY_SECRET|OSS_ALIYUN_BUCKET_NAME|HINOVEL_LOG_FILE|HINOVEL_MACHINE_ID)
            export "$key=${value%$'\r'}"
            ;;
    esac
done <<< "$settings"
unset settings line key value

export MYSQL_PASSWORD="${MYSQL_PASSWORD:-${MYSQL_ROOT_PASSWORD:-}}"
export QDRANT_PORT="${QDRANT_PORT:-${QDRANT_GRPC_PORT:-6334}}"
export SERVER_PORT="${SERVER_PORT:-8080}" WEB_PORT="${WEB_PORT:-3000}"
export OSS_LOCAL_URL_PREFIX="${OSS_LOCAL_URL_PREFIX:-/files}"
export API_KEY_SECRET="${API_KEY_SECRET:-}"
[[ -n "$MYSQL_PASSWORD" ]] || die "请配置 MYSQL_PASSWORD（或 MYSQL_ROOT_PASSWORD）"
[[ ${#API_KEY_SECRET} -ge 16 ]] || die "请配置至少 16 个字符的 API_KEY_SECRET"

for port in "$SERVER_PORT" "$WEB_PORT"; do
    [[ "$port" =~ ^[1-9][0-9]{0,4}$ && "$port" -le 65535 ]] || die "端口不合法：$port"
    if curl -s -o /dev/null --noproxy '*' --max-time 2 "http://localhost:$port/"; then
        die "端口 $port 已有 HTTP 服务，请停止该服务或调整 SERVER_PORT / WEB_PORT"
    fi
done
[[ "$SERVER_PORT" != "$WEB_PORT" ]] || die "SERVER_PORT 与 WEB_PORT 必须不同"

cd "$PROJECT_DIR"
if [[ "${1:-}" != --no-infra ]]; then
    bash "$PROJECT_DIR/docker-build.sh" infra
fi
prepare_frontend
mvn -f hinovel-server/pom.xml -B clean package -DskipTests
jar_files=(hinovel-server/target/*.jar)
[[ ${#jar_files[@]} -eq 1 && -f "${jar_files[0]}" ]] || die "预期仅生成一个可运行的后端 JAR"

mkdir -p logs
backend_pid=
frontend_pid=
cleanup() {
    if [[ -n "$frontend_pid" ]]; then
        kill "$frontend_pid" 2>/dev/null || true
        wait "$frontend_pid" 2>/dev/null || true
    fi
    if [[ -n "$backend_pid" ]]; then
        kill "$backend_pid" 2>/dev/null || true
        wait "$backend_pid" 2>/dev/null || true
    fi
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

wait_for_http() {
    local url="$1" expected="$2" pid="$3" log_file="$4" status
    local deadline=$((SECONDS + 90))
    while (( SECONDS < deadline )) && kill -0 "$pid" 2>/dev/null; do
        status="$(curl -s -o /dev/null -w '%{http_code}' --noproxy '*' --max-time 2 "$url" || true)"
        [[ "$status" != "$expected" ]] || return 0
        sleep 1
    done
    die "服务未就绪：$url，日志见 $log_file"
}

"$java_cmd" -Duser.timezone=Asia/Shanghai -jar "${jar_files[0]}" > logs/hinovel-server.log 2>&1 &
backend_pid=$!
wait_for_http "http://localhost:$SERVER_PORT/api/user/auth/me" 401 "$backend_pid" logs/hinovel-server.log

(
    cd hinovel-web
    exec node node_modules/vite/bin/vite.js
) > logs/hinovel-web.log 2>&1 &
frontend_pid=$!
wait_for_http "http://localhost:$WEB_PORT/" 200 "$frontend_pid" logs/hinovel-web.log

printf '前端：http://localhost:%s\n后端：http://localhost:%s\n日志：%s/logs/\n' "$WEB_PORT" "$SERVER_PORT" "$PROJECT_DIR"
printf '按 Ctrl+C 停止本地前后端，基础设施继续运行。\n'
while kill -0 "$backend_pid" 2>/dev/null && kill -0 "$frontend_pid" 2>/dev/null; do
    sleep 1
done
die "应用进程已退出，请查看 logs/ 下的日志"
