#!/usr/bin/env bash
# ========================================================
# HiNovel 数据库手动初始化脚本
# 适用：MySQL 容器已运行，但数据库未初始化或需重新灌入结构/种子数据
#
# 用法：
#   bash scripts/init-db.sh
#   bash scripts/init-db.sh --force   # 清空 hinovel_platform 后重建（危险）
# ========================================================

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
SCHEMA_FILE="$ROOT_DIR/hinovel-server/sql/schema.sql"
SEED_FILE="$ROOT_DIR/hinovel-server/sql/seed.sql"
ENV_FILE="$ROOT_DIR/.env"

FORCE=false
if [[ "${1:-}" == "--force" ]]; then
  FORCE=true
fi

if [[ ! -f "$ENV_FILE" ]]; then
  echo "错误：未找到 .env，请先复制 .env.example 为 .env"
  exit 1
fi

# shellcheck disable=SC1090
source "$ENV_FILE"

MYSQL_CONTAINER="${MYSQL_CONTAINER:-hinovel-mysql}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-${MYSQL_ROOT_PASSWORD:-root123}}"
MYSQL_DB="${MYSQL_DB:-hinovel_platform}"

if ! docker ps --format '{{.Names}}' | grep -qx "$MYSQL_CONTAINER"; then
  echo "错误：MySQL 容器 $MYSQL_CONTAINER 未运行，请先 docker compose up -d mysql"
  exit 1
fi

mysql_exec() {
  docker exec -i "$MYSQL_CONTAINER" mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$@"
}

echo ">>> 目标容器：$MYSQL_CONTAINER"
echo ">>> 目标数据库：$MYSQL_DB"

if $FORCE; then
  echo ">>> 警告：即将删除并重建数据库 $MYSQL_DB"
  mysql_exec -e "DROP DATABASE IF EXISTS \`$MYSQL_DB\`; CREATE DATABASE \`$MYSQL_DB\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
fi

echo ">>> 执行 schema.sql ..."
mysql_exec "$MYSQL_DB" < "$SCHEMA_FILE"

echo ">>> 执行 seed.sql ..."
mysql_exec "$MYSQL_DB" < "$SEED_FILE"

echo ">>> 数据库初始化完成"
echo "    管理员账号：admin / Admin@123456"
echo "    邮箱：admin@hinovel.com"
