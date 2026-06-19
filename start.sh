#!/bin/bash
# ========================================================
# HiNovel AI 小说创作平台 - 本地开发启动脚本
# 用法：./start.sh
# ========================================================

set -e

# 应用 jar 包路径（与 pom.xml 中 artifactId + version 对应）
JAR_PATH="hinovel-server/target/hinovel-server-1.0.0-SNAPSHOT.jar"

echo "=========================================="
echo "  HiNovel AI 小说创作平台 - 本地开发启动脚本"
echo "=========================================="

# --------------------------------------------------------
# 环境检查
# --------------------------------------------------------
check_command() {
    if ! command -v "$1" &> /dev/null; then
        echo "错误：未找到 $1，请先安装 $2"
        exit 1
    fi
}

check_command java "JDK 21+"
check_command mvn "Maven 3.9+"
check_command node "Node.js 20+"
check_command docker "Docker"

JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "错误：需要 JDK 21 或以上版本，当前版本为 $JAVA_VERSION"
    exit 1
fi

# 检查 .env 文件是否存在
if [ ! -f ".env" ]; then
    echo "错误：未找到 .env 配置文件，请复制 .env.example 为 .env 并填写配置"
    exit 1
fi

# --------------------------------------------------------
# 创建数据目录
# --------------------------------------------------------
mkdir -p data/mysql/data data/mysql/conf data/mysql/logs
mkdir -p data/redis/data data/redis/conf
mkdir -p data/qdrant/storage data/qdrant/snapshots
mkdir -p data/hinovel/uploads data/hinovel/logs
mkdir -p data/nginx/logs
mkdir -p logs

# --------------------------------------------------------
# 步骤 1：启动基础设施
# --------------------------------------------------------
echo ""
echo ">>> 步骤 1：启动基础设施（MySQL、Redis、Qdrant）..."
docker-compose up -d mysql redis qdrant
echo "等待基础设施就绪（20秒）..."
sleep 20

# --------------------------------------------------------
# 步骤 2：构建后端
# --------------------------------------------------------
echo ""
echo ">>> 步骤 2：构建后端..."
cd hinovel-server
mvn clean package -DskipTests -q
cd ..

if [ ! -f "$JAR_PATH" ]; then
    echo "错误：构建产物不存在，路径：$JAR_PATH"
    exit 1
fi

# --------------------------------------------------------
# 步骤 3：启动后端
# --------------------------------------------------------
echo ""
echo ">>> 步骤 3：启动后端（后台运行）..."
nohup java -jar "$JAR_PATH" > logs/hinovel-server.log 2>&1 &
BACKEND_PID=$!
echo "后端 PID: $BACKEND_PID，日志：logs/hinovel-server.log"
echo "等待后端启动（15秒）..."
sleep 15

# --------------------------------------------------------
# 步骤 4：启动前端
# --------------------------------------------------------
echo ""
echo ">>> 步骤 4：安装前端依赖并启动..."
cd hinovel-web
if [ ! -d "node_modules" ]; then
    npm install
fi
echo "前端开发服务器启动中..."
npm run dev &
FRONTEND_PID=$!
cd ..

# --------------------------------------------------------
# 启动完成
# --------------------------------------------------------
echo ""
echo "=========================================="
echo "  启动完成"
echo "  前端地址：http://localhost:3000"
echo "  后端地址：http://localhost:8080"
echo "  按 Ctrl+C 停止所有服务"
echo "=========================================="

# 捕获退出信号，优雅停止所有服务
trap "echo '正在停止服务...'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; docker-compose stop mysql redis qdrant; exit 0" INT TERM
wait
