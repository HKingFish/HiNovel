# ========================================================
# HiNovel 数据库手动初始化脚本（Windows PowerShell）
# 适用：MySQL 容器已运行，但数据库未初始化或需重新灌入结构/种子数据
#
# 用法：
#   .\scripts\init-db.ps1
#   .\scripts\init-db.ps1 -Force   # 清空 hinovel_platform 后重建（危险）
# ========================================================

param(
    [switch]$Force
)

$ErrorActionPreference = "Stop"
$RootDir = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$SchemaFile = Join-Path $RootDir "hinovel-server\sql\schema.sql"
$SeedFile = Join-Path $RootDir "hinovel-server\sql\seed.sql"
$EnvFile = Join-Path $RootDir ".env"

if (-not (Test-Path $EnvFile)) {
    Write-Error "未找到 .env，请先复制 .env.example 为 .env"
}

Get-Content $EnvFile | ForEach-Object {
    if ($_ -match '^\s*([^#=]+?)=(.*)$') {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()
        Set-Item -Path "env:$name" -Value $value
    }
}

$MysqlContainer = if ($env:MYSQL_CONTAINER) { $env:MYSQL_CONTAINER } else { "hinovel-mysql" }
$MysqlUser = if ($env:MYSQL_USER) { $env:MYSQL_USER } else { "root" }
$MysqlPassword = if ($env:MYSQL_PASSWORD) { $env:MYSQL_PASSWORD } elseif ($env:MYSQL_ROOT_PASSWORD) { $env:MYSQL_ROOT_PASSWORD } else { "root123" }
$MysqlDb = if ($env:MYSQL_DB) { $env:MYSQL_DB } else { "hinovel_platform" }

$running = docker ps --format "{{.Names}}" | Select-String -Pattern "^$([regex]::Escape($MysqlContainer))$"
if (-not $running) {
    Write-Error "MySQL 容器 $MysqlContainer 未运行，请先执行：docker compose up -d mysql"
}

Write-Host ">>> 目标容器：$MysqlContainer"
Write-Host ">>> 目标数据库：$MysqlDb"

if ($Force) {
    Write-Host ">>> 警告：即将删除并重建数据库 $MysqlDb"
    $dropSql = "DROP DATABASE IF EXISTS ``$MysqlDb``; CREATE DATABASE ``$MysqlDb`` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    $dropSql | docker exec -i $MysqlContainer mysql -u"$MysqlUser" -p"$MysqlPassword"
}

Write-Host ">>> 执行 schema.sql ..."
Get-Content $SchemaFile -Raw | docker exec -i $MysqlContainer mysql -u"$MysqlUser" -p"$MysqlPassword" $MysqlDb

Write-Host ">>> 执行 seed.sql ..."
Get-Content $SeedFile -Raw | docker exec -i $MysqlContainer mysql -u"$MysqlUser" -p"$MysqlPassword" $MysqlDb

Write-Host ">>> 数据库初始化完成"
Write-Host "    管理员账号：admin / Admin@123456"
Write-Host "    邮箱：admin@hinovel.com"
