param(
    [switch]$Force,
    [string]$SqlFile
)

$ErrorActionPreference = 'Stop'
$RootDir = Split-Path -Parent $PSScriptRoot
$ComposeArgs = @('compose', '--project-directory', $RootDir, '-f', (Join-Path $RootDir 'docker-compose.yml'))
$EnvFile = Join-Path $RootDir '.env'
if (Test-Path -LiteralPath $EnvFile -PathType Leaf) {
    $ComposeArgs += @('--env-file', $EnvFile)
}

if ($Force -and $SqlFile) {
    throw '-Force 与 -SqlFile 不能一起使用。'
}
if ($SqlFile -and -not (Test-Path -LiteralPath $SqlFile -PathType Leaf)) {
    throw '找不到指定的 SQL 文件。'
}

$Database = & docker @ComposeArgs exec -T mysql printenv MYSQL_DATABASE
if ($LASTEXITCODE -ne 0) {
    throw 'MySQL 容器未运行或缺少 MYSQL_DATABASE。'
}
if ($Database -cnotmatch '^[A-Za-z0-9_]{1,64}$') {
    throw 'MYSQL_DB 只支持 1 到 64 位字母、数字和下划线。'
}

$MysqlRunner = 'MYSQL_PWD=$MYSQL_ROOT_PASSWORD exec mysql --protocol=TCP -h 127.0.0.1 -u root --default-character-set=utf8mb4'

function Invoke-MySqlScript {
    param([string]$Sql, [string]$Command)
    $OutputEncoding = [System.Text.UTF8Encoding]::new($false)
    $Sql | & docker @ComposeArgs exec -T mysql sh -ec $Command
    if ($LASTEXITCODE -ne 0) {
        throw "MySQL 执行失败（退出码 $LASTEXITCODE）。"
    }
}

if ($SqlFile) {
    Invoke-MySqlScript (Get-Content -LiteralPath $SqlFile -Raw -Encoding UTF8) "$MysqlRunner -D $Database"
    Write-Host "SQL 执行完成，目标库：$Database"
    exit 0
}

$CreateDatabase = "CREATE DATABASE IF NOT EXISTS ``$Database`` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if ($Force) {
    Write-Host "正在清空并重建数据库 $Database"
    $CreateDatabase = "DROP DATABASE IF EXISTS ``$Database``; $CreateDatabase"
}
Invoke-MySqlScript $CreateDatabase $MysqlRunner

foreach ($File in @('schema.sql', 'seed.sql')) {
    $Path = Join-Path $RootDir "hinovel-server/sql/$File"
    Invoke-MySqlScript (Get-Content -LiteralPath $Path -Raw -Encoding UTF8) "$MysqlRunner -D $Database"
}
Write-Host "数据库初始化完成：$Database"
