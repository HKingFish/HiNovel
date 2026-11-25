#!/usr/bin/env bash
set -euo pipefail

source "$(dirname -- "${BASH_SOURCE[0]}")/compose-common.sh"

case "${1:-}" in
    ''|--force) [[ $# -le 1 ]] || die "参数不正确" ;;
    --sql) [[ $# -eq 2 && -f "$2" ]] || die "请提供要执行的 SQL 文件" ;;
    -h|--help)
        printf '用法：bash scripts/init-db.sh [--force | --sql <文件>]\n'
        exit 0
        ;;
    *) die "未知参数，请查看 --help" ;;
esac

require_compose
database="$(mysql_database)"
mysql_runner='MYSQL_PWD=$MYSQL_ROOT_PASSWORD exec mysql --protocol=TCP -h 127.0.0.1 -u root --default-character-set=utf8mb4'

if [[ "${1:-}" == --sql ]]; then
    compose exec -T mysql sh -ec "$mysql_runner -D $database" < "$2"
    printf 'SQL 执行完成，目标库：%s\n' "$database"
    exit 0
fi

{
    if [[ "${1:-}" == --force ]]; then
        printf '正在清空并重建数据库 %s\n' "$database" >&2
        printf 'DROP DATABASE IF EXISTS `%s`;\n' "$database"
    fi
    printf 'CREATE DATABASE IF NOT EXISTS `%s` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\n' "$database"
} | compose exec -T mysql sh -ec "$mysql_runner"

for file in schema.sql seed.sql; do
    compose exec -T mysql sh -ec "$mysql_runner -D $database" < "$PROJECT_DIR/hinovel-server/sql/$file"
done
printf '数据库初始化完成：%s\n' "$database"
