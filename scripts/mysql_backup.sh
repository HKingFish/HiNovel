#!/bin/bash

# ============================================================
# MySQL 定时备份脚本
# 功能：每天自动 dump MySQL 数据库，保留最近 7 天的备份
# 使用方式：通过 crontab 配置每天定时执行
# ============================================================

# 项目根目录（脚本所在目录的上一级）
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

# 加载 .env 环境变量（获取数据库密码）
if [ -f "$PROJECT_DIR/.env" ]; then
    export $(grep -v '^#' "$PROJECT_DIR/.env" | grep -v '^\s*$' | xargs)
fi

# 备份配置
BACKUP_DIR="$PROJECT_DIR/../data/mysql/backup"
MYSQL_CONTAINER="hinovel-mysql"
DATABASE_NAME="hinovel_platform"
MYSQL_USER="root"
MYSQL_PASSWORD="${MYSQL_ROOT_PASSWORD:-root123}"

# 保留天数
KEEP_DAYS=7

# 当前日期（用于备份文件命名）
CURRENT_DATE=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="$BACKUP_DIR/${DATABASE_NAME}_${CURRENT_DATE}.sql.gz"

# 日志函数
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# 创建备份目录（不存在则创建）
if [ ! -d "$BACKUP_DIR" ]; then
    mkdir -p "$BACKUP_DIR"
    log "备份目录已创建：$BACKUP_DIR"
fi

# 检查 MySQL 容器是否运行中
if ! docker ps --format '{{.Names}}' | grep -q "^${MYSQL_CONTAINER}$"; then
    log "错误：MySQL 容器 ${MYSQL_CONTAINER} 未运行，备份终止"
    exit 1
fi

# 执行 mysqldump 并通过管道压缩为 .sql.gz（恢复时：gunzip < backup.sql.gz | mysql -u root -p hinovel_platform）
log "开始备份数据库：${DATABASE_NAME}"
docker exec "$MYSQL_CONTAINER" mysqldump \
    -u"$MYSQL_USER" \
    -p"$MYSQL_PASSWORD" \
    --single-transaction \
    --routines \
    --triggers \
    --events \
    --set-gtid-purged=OFF \
    "$DATABASE_NAME" 2>/dev/null | gzip > "$BACKUP_FILE"

# 检查备份结果
if [ $? -eq 0 ] && [ -s "$BACKUP_FILE" ]; then
    BACKUP_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
    log "备份成功：$BACKUP_FILE（大小：$BACKUP_SIZE）"
else
    log "错误：备份失败，请检查数据库连接和容器状态"
    rm -f "$BACKUP_FILE"
    exit 1
fi

# 清理过期备份（保留最近 KEEP_DAYS 天）
log "清理 ${KEEP_DAYS} 天前的过期备份..."
DELETED_COUNT=0
find "$BACKUP_DIR" -name "${DATABASE_NAME}_*.sql.gz" -type f -mtime +${KEEP_DAYS} | while read old_file; do
    rm -f "$old_file"
    log "已删除过期备份：$old_file"
    DELETED_COUNT=$((DELETED_COUNT + 1))
done

log "备份任务完成"
