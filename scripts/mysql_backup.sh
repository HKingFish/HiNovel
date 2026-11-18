#!/usr/bin/env bash
set -euo pipefail
umask 077

source "$(dirname -- "${BASH_SOURCE[0]}")/compose-common.sh"

require_compose
require_command gzip gzip
database="$(mysql_database)"
backup_dir="${MYSQL_BACKUP_DIR:-$PROJECT_DIR/../data/mysql/backup}"
keep_days="${MYSQL_BACKUP_KEEP_DAYS:-7}"
[[ "$keep_days" =~ ^[1-9][0-9]*$ ]] || die "MYSQL_BACKUP_KEEP_DAYS 必须为正整数"

mkdir -p -- "$backup_dir"
backup_file="$backup_dir/${database}_$(date -u +%Y%m%dT%H%M%SZ).sql.gz"
temp_file="$(mktemp "$backup_dir/.hinovel-dump.XXXXXXXX")"
trap 'rm -f -- "$temp_file"' EXIT

dump_runner='MYSQL_PWD=$MYSQL_ROOT_PASSWORD exec mysqldump --protocol=TCP -h 127.0.0.1 -u root'
if ! compose exec -T mysql sh -ec "$dump_runner --single-transaction --quick --routines --triggers --events --set-gtid-purged=OFF $database" |
    gzip > "$temp_file"; then
    die "备份失败，未写入备份文件"
fi
ln -- "$temp_file" "$backup_file" || die "无法发布备份文件：$backup_file"
printf '备份完成：%s\n' "$backup_file"

find "$backup_dir" -maxdepth 1 -type f -name "${database}_*.sql.gz" -mtime "+$((keep_days - 1))" -delete
