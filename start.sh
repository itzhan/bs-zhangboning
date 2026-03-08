#!/bin/bash
# ============================================================
#  景区停车引导系统 - 一键启动脚本 (Mac/Linux)
# ============================================================

set -e

# ─── 颜色定义 ───
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# ─── 项目配置 ───
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$PROJECT_DIR/backend"
FRONTEND_DIR="$PROJECT_DIR/frontend"
ADMIN_DIR="$PROJECT_DIR/admin"
SQL_DIR="$PROJECT_DIR/sql"
LOG_DIR="$PROJECT_DIR/.logs"
PID_DIR="$PROJECT_DIR/.pids"

DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="parking_system"
DB_USER="root"
DB_PASS="ab123168"

BACKEND_PORT=8087
FRONTEND_PORT=3000
ADMIN_PORT=9527

BANNER="景区停车引导系统"

# ─── 创建目录 ───
mkdir -p "$LOG_DIR" "$PID_DIR"

# ─── Banner ───
echo ""
echo -e "${CYAN}╔══════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║     $BANNER     ║${NC}"
echo -e "${CYAN}║          一键启动脚本                    ║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════╝${NC}"
echo ""

# ─── 1. 检查基础环境 ───
echo -e "${YELLOW}[1/7] 检查基础环境...${NC}"

check_command() {
    if ! command -v "$1" &>/dev/null; then
        echo -e "${RED}  ✗ $1 未安装${NC}"
        echo -e "    安装指引: $2"
        return 1
    fi
    echo -e "${GREEN}  ✓ $1 已安装${NC}"
    return 0
}

ALL_OK=true
check_command java "brew install openjdk / https://adoptium.net/" || ALL_OK=false
check_command mvn "brew install maven / https://maven.apache.org/" || ALL_OK=false
check_command node "brew install node / https://nodejs.org/" || ALL_OK=false
check_command mysql "brew install mysql / https://dev.mysql.com/" || ALL_OK=false

if ! command -v pnpm &>/dev/null; then
    echo -e "${YELLOW}  ! pnpm 未安装，正在自动安装...${NC}"
    npm install -g pnpm 2>&1 || { echo -e "${RED}  ✗ pnpm 安装失败${NC}"; ALL_OK=false; }
    echo -e "${GREEN}  ✓ pnpm 已安装${NC}"
else
    echo -e "${GREEN}  ✓ pnpm 已安装${NC}"
fi

if [ "$ALL_OK" = false ]; then
    echo -e "${RED}环境检查未通过，请安装缺失的工具后重试${NC}"
    exit 1
fi

# ─── 2. 检查 MySQL ───
echo ""
echo -e "${YELLOW}[2/7] 检查 MySQL 数据库...${NC}"

# 检测 MySQL 是否运行
if ! mysqladmin ping -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" --silent 2>/dev/null; then
    echo -e "${YELLOW}  MySQL 未运行，尝试启动...${NC}"
    if [[ "$OSTYPE" == "darwin"* ]]; then
        brew services start mysql 2>/dev/null || true
    else
        sudo systemctl start mysql 2>/dev/null || sudo systemctl start mysqld 2>/dev/null || true
    fi
    sleep 3
    if ! mysqladmin ping -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" --silent 2>/dev/null; then
        echo -e "${RED}  ✗ MySQL 启动失败，请手动启动${NC}"
        exit 1
    fi
fi
echo -e "${GREEN}  ✓ MySQL 运行中${NC}"

# 检查数据库
DB_EXISTS=$(mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" -e "SHOW DATABASES LIKE '$DB_NAME';" 2>/dev/null | grep "$DB_NAME" || true)
TABLE_COUNT=$(mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "SHOW TABLES;" 2>/dev/null | wc -l || echo "0")

if [ -z "$DB_EXISTS" ] || [ "$TABLE_COUNT" -le 1 ]; then
    echo -e "${YELLOW}  数据库不存在或为空，正在初始化...${NC}"
    mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" < "$SQL_DIR/init.sql" 2>/dev/null
    echo -e "${GREEN}  ✓ 表结构创建完成${NC}"
    mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" < "$SQL_DIR/data.sql" 2>/dev/null
    echo -e "${GREEN}  ✓ 测试数据导入完成${NC}"
else
    echo -e "${GREEN}  ✓ 数据库 $DB_NAME 已存在 ($((TABLE_COUNT-1)) 张表)${NC}"
fi

# ─── 3. 检查并安装依赖 ───
echo ""
echo -e "${YELLOW}[3/7] 检查项目依赖...${NC}"

if [ ! -d "$BACKEND_DIR/target/classes" ]; then
    echo -e "${YELLOW}  编译后端...${NC}"
    cd "$BACKEND_DIR"
    mvn compile -q 2>&1 || { echo -e "${RED}  ✗ 后端编译失败${NC}"; exit 1; }
    echo -e "${GREEN}  ✓ 后端编译完成${NC}"
else
    echo -e "${GREEN}  ✓ 后端已编译${NC}"
fi

if [ ! -d "$FRONTEND_DIR/node_modules" ]; then
    echo -e "${YELLOW}  安装前端依赖...${NC}"
    cd "$FRONTEND_DIR"
    pnpm install 2>&1 || { echo -e "${RED}  ✗ 前端依赖安装失败${NC}"; exit 1; }
    echo -e "${GREEN}  ✓ 前端依赖安装完成${NC}"
else
    echo -e "${GREEN}  ✓ 前端依赖已安装${NC}"
fi

if [ ! -d "$ADMIN_DIR/node_modules" ]; then
    echo -e "${YELLOW}  安装管理端依赖...${NC}"
    cd "$ADMIN_DIR"
    pnpm install 2>&1 || { echo -e "${RED}  ✗ 管理端依赖安装失败${NC}"; exit 1; }
    echo -e "${GREEN}  ✓ 管理端依赖安装完成${NC}"
else
    echo -e "${GREEN}  ✓ 管理端依赖已安装${NC}"
fi

# ─── 4. 检查端口 ───
echo ""
echo -e "${YELLOW}[4/7] 检查端口...${NC}"

check_port() {
    local port=$1
    local name=$2
    if lsof -i ":$port" -sTCP:LISTEN &>/dev/null; then
        local pid=$(lsof -i ":$port" -sTCP:LISTEN -t 2>/dev/null | head -1)
        echo -e "${YELLOW}  ! 端口 $port ($name) 被进程 $pid 占用${NC}"
        read -p "  是否终止该进程? [y/N] " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            kill "$pid" 2>/dev/null
            sleep 1
            echo -e "${GREEN}  ✓ 进程已终止${NC}"
        else
            echo -e "${RED}  请手动释放端口 $port 后重试${NC}"
            exit 1
        fi
    else
        echo -e "${GREEN}  ✓ 端口 $port ($name) 可用${NC}"
    fi
}

check_port $BACKEND_PORT "后端"
check_port $FRONTEND_PORT "前端"
check_port $ADMIN_PORT "管理端"

# ─── 5. 启动服务 ───
echo ""
echo -e "${YELLOW}[5/7] 启动服务...${NC}"

# 启动后端
echo -e "${CYAN}  启动后端服务...${NC}"
cd "$BACKEND_DIR"
mvn spring-boot:run > "$LOG_DIR/backend.log" 2>&1 &
echo $! > "$PID_DIR/backend.pid"
echo -e "${GREEN}  ✓ 后端服务已启动 (PID: $(cat $PID_DIR/backend.pid))${NC}"

# 启动前端
echo -e "${CYAN}  启动前端服务...${NC}"
cd "$FRONTEND_DIR"
pnpm dev > "$LOG_DIR/frontend.log" 2>&1 &
echo $! > "$PID_DIR/frontend.pid"
echo -e "${GREEN}  ✓ 前端服务已启动 (PID: $(cat $PID_DIR/frontend.pid))${NC}"

# 启动管理端
echo -e "${CYAN}  启动管理端服务...${NC}"
cd "$ADMIN_DIR"
pnpm dev > "$LOG_DIR/admin.log" 2>&1 &
echo $! > "$PID_DIR/admin.pid"
echo -e "${GREEN}  ✓ 管理端服务已启动 (PID: $(cat $PID_DIR/admin.pid))${NC}"

# ─── 6. 等待就绪 ───
echo ""
echo -e "${YELLOW}[6/7] 等待服务就绪...${NC}"

wait_for_port() {
    local port=$1
    local name=$2
    local max_wait=60
    local waited=0
    while ! lsof -i ":$port" -sTCP:LISTEN &>/dev/null; do
        sleep 2
        waited=$((waited + 2))
        if [ $waited -ge $max_wait ]; then
            echo -e "${RED}  ✗ $name (端口 $port) 启动超时${NC}"
            echo -e "    查看日志: cat $LOG_DIR/$(echo $name | tr '[:upper:]' '[:lower:]').log"
            return 1
        fi
    done
    echo -e "${GREEN}  ✓ $name 已就绪 (端口 $port, ${waited}s)${NC}"
}

wait_for_port $BACKEND_PORT "后端"
wait_for_port $FRONTEND_PORT "前端"
wait_for_port $ADMIN_PORT "管理端"

# ─── 7. 输出访问信息 ───
echo ""
echo -e "${CYAN}╔══════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║         所有服务已成功启动！             ║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════╝${NC}"
echo ""
echo -e "${GREEN}  用户端前端:  ${NC}http://localhost:$FRONTEND_PORT"
echo -e "${GREEN}  管理端后台:  ${NC}http://localhost:$ADMIN_PORT"
echo -e "${GREEN}  后端 API:    ${NC}http://localhost:$BACKEND_PORT"
echo -e "${GREEN}  API 文档:    ${NC}http://localhost:$BACKEND_PORT/doc.html"
echo ""
echo -e "${YELLOW}  测试账号:${NC}"
echo -e "    管理员: admin / admin123"
echo -e "    游客:   user1 / 123456"
echo ""
echo -e "${YELLOW}  日志目录: $LOG_DIR/${NC}"
echo -e "    后端: tail -f $LOG_DIR/backend.log"
echo -e "    前端: tail -f $LOG_DIR/frontend.log"
echo -e "    管理端: tail -f $LOG_DIR/admin.log"
echo ""
echo -e "${YELLOW}  停止所有服务: bash $PROJECT_DIR/stop.sh${NC}"
echo ""

# ─── 实时日志输出 ───
echo -e "${CYAN}按 Ctrl+C 停止所有服务${NC}"
echo ""

# 监控进程
trap "bash $PROJECT_DIR/stop.sh; exit 0" INT TERM

# 实时输出合并日志
tail -f "$LOG_DIR/backend.log" 2>/dev/null | while read line; do echo -e "${RED}[后端]${NC} $line"; done &
TAIL_BACKEND=$!
tail -f "$LOG_DIR/frontend.log" 2>/dev/null | while read line; do echo -e "${GREEN}[前端]${NC} $line"; done &
TAIL_FRONTEND=$!
tail -f "$LOG_DIR/admin.log" 2>/dev/null | while read line; do echo -e "${BLUE}[管理端]${NC} $line"; done &
TAIL_ADMIN=$!

echo $TAIL_BACKEND > "$PID_DIR/tail_backend.pid"
echo $TAIL_FRONTEND > "$PID_DIR/tail_frontend.pid"
echo $TAIL_ADMIN > "$PID_DIR/tail_admin.pid"

wait
