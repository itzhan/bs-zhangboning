#!/bin/bash
# ============================================================
#  景区停车引导系统 - 一键停止脚本 (Mac/Linux)
# ============================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
PID_DIR="$PROJECT_DIR/.pids"

BACKEND_PORT=8087
FRONTEND_PORT=3000
ADMIN_PORT=9527

echo ""
echo -e "${CYAN}╔══════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║     景区停车引导系统 - 停止服务          ║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════╝${NC}"
echo ""

# 通过 PID 文件停止
stop_by_pid() {
    local name=$1
    local pidfile="$PID_DIR/$2.pid"
    if [ -f "$pidfile" ]; then
        local pid=$(cat "$pidfile")
        if kill -0 "$pid" 2>/dev/null; then
            kill "$pid" 2>/dev/null
            echo -e "${GREEN}  ✓ $name 已停止 (PID: $pid)${NC}"
        fi
        rm -f "$pidfile"
    fi
}

# 通过端口停止
stop_by_port() {
    local port=$1
    local name=$2
    local pids=$(lsof -i ":$port" -sTCP:LISTEN -t 2>/dev/null)
    if [ -n "$pids" ]; then
        echo "$pids" | xargs kill 2>/dev/null
        echo -e "${GREEN}  ✓ $name (端口 $port) 已停止${NC}"
    fi
}

# 停止 tail 监控进程
stop_by_pid "后端日志监控" "tail_backend"
stop_by_pid "前端日志监控" "tail_frontend"
stop_by_pid "管理端日志监控" "tail_admin"

# 停止服务（PID 文件方式）
stop_by_pid "后端服务" "backend"
stop_by_pid "前端服务" "frontend"
stop_by_pid "管理端服务" "admin"

# 双重清理（端口方式）
sleep 1
stop_by_port $BACKEND_PORT "后端"
stop_by_port $FRONTEND_PORT "前端"
stop_by_port $ADMIN_PORT "管理端"

# 清理 PID 目录
rm -rf "$PID_DIR"

echo ""
echo -e "${GREEN}所有服务已停止${NC}"
echo ""
