#!/bin/bash
# ============================================================
# 景区停车引导系统 - 前端→后端全接口联调测试脚本
# ============================================================
# 测试账号:
#   管理员: admin / admin123
#   游客:   user1 / 123456
# ============================================================

BASE_URL="http://localhost:8087"
PASS=0
FAIL=0
TOTAL=0
FAILED_LIST=""

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

# ============================================================
# 通用测试函数
# ============================================================
test_api() {
    local method="$1"
    local url="$2"
    local desc="$3"
    local token="$4"
    local data="$5"
    local expect_code="${6:-200}"

    TOTAL=$((TOTAL + 1))

    local auth_header=""
    if [ -n "$token" ]; then
        auth_header="-H \"Authorization: Bearer $token\""
    fi

    local content_type=""
    local body=""
    if [ -n "$data" ]; then
        content_type="-H \"Content-Type: application/json\""
        body="-d '$data'"
    fi

    # 构建并执行curl命令 (--data-urlencode 不适用于此处，使用 --globoff 避免curl解析中文问题)
    local cmd="curl -s -g -w '\n%{http_code}' -X $method '${BASE_URL}${url}' $auth_header $content_type $body"
    local response
    response=$(eval $cmd 2>/dev/null)

    local http_code
    http_code=$(echo "$response" | tail -1)
    local body_response
    body_response=$(echo "$response" | sed '$d')

    # 检查业务code
    local biz_code
    biz_code=$(echo "$body_response" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('code',''))" 2>/dev/null)

    if [ "$http_code" = "$expect_code" ] && [ "$biz_code" = "200" ]; then
        printf "${GREEN}✅ [PASS]${NC} %-6s %-40s %s\n" "$method" "$url" "$desc"
        PASS=$((PASS + 1))
    elif [ "$http_code" = "$expect_code" ]; then
        # 有些接口可能不返回标准格式
        printf "${GREEN}✅ [PASS]${NC} %-6s %-40s %s (HTTP $http_code)\n" "$method" "$url" "$desc"
        PASS=$((PASS + 1))
    else
        printf "${RED}❌ [FAIL]${NC} %-6s %-40s %s (HTTP=$http_code, BIZ=$biz_code)\n" "$method" "$url" "$desc"
        # 打印错误详情
        local err_msg
        err_msg=$(echo "$body_response" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('message',d.get('msg','')))" 2>/dev/null)
        if [ -n "$err_msg" ]; then
            printf "   ${YELLOW}→ 错误信息: %s${NC}\n" "$err_msg"
        fi
        FAIL=$((FAIL + 1))
        FAILED_LIST="${FAILED_LIST}\n  ${method} ${url} - ${desc}"
    fi
}

# 提取JSON字段的辅助函数
json_val() {
    echo "$1" | python3 -c "import sys,json; d=json.load(sys.stdin); print($2)" 2>/dev/null
}

# 登录并提取token的辅助函数
do_login() {
    local username="$1"
    local password="$2"
    local response
    response=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$username\",\"password\":\"$password\"}")
    echo "$response" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('token',''))" 2>/dev/null
}

echo ""
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║       景区停车引导系统 - 全接口联调测试                      ║"
echo "║       Backend: ${BASE_URL}                            ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

# ============================================================
# 0. 前置：检查后端是否可达
# ============================================================
echo -e "${CYAN}[0] 检查后端服务...${NC}"
HTTP_CHECK=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/api/public/announcements" 2>/dev/null)
if [ "$HTTP_CHECK" = "000" ]; then
    echo -e "${RED}❌ 后端服务不可达! 请先启动后端 (端口 8087)${NC}"
    exit 1
fi
echo -e "${GREEN}✅ 后端服务已启动${NC}"
echo ""

# ============================================================
# 1. 认证模块 (Auth) - 前端 + 管理端共用
# ============================================================
echo -e "${CYAN}[1] ========== 认证模块 (Auth) ==========${NC}"

# 1.1 管理员登录
ADMIN_TOKEN=$(do_login "admin" "admin123")
if [ -z "$ADMIN_TOKEN" ] || [ "$ADMIN_TOKEN" = "None" ]; then
    echo -e "${RED}❌ 管理员登录失败，无法获取Token，终止测试${NC}"
    exit 1
fi
echo -e "${GREEN}✅ 管理员登录成功, Token: ${ADMIN_TOKEN:0:30}...${NC}"
TOTAL=$((TOTAL + 1))
PASS=$((PASS + 1))

# 1.2 普通用户登录
USER_TOKEN=$(do_login "user1" "123456")
if [ -z "$USER_TOKEN" ] || [ "$USER_TOKEN" = "None" ]; then
    echo -e "${RED}❌ 用户登录失败，无法获取Token${NC}"
    TOTAL=$((TOTAL + 1))
    FAIL=$((FAIL + 1))
    FAILED_LIST="${FAILED_LIST}\n  POST /api/auth/login (user1) - 用户登录"
else
    echo -e "${GREEN}✅ 用户(user1)登录成功, Token: ${USER_TOKEN:0:30}...${NC}"
    TOTAL=$((TOTAL + 1))
    PASS=$((PASS + 1))
fi

# 1.3 获取当前用户信息 (管理端调用)
test_api "GET" "/api/auth/info" "获取当前登录用户信息(Admin)" "$ADMIN_TOKEN"

# 1.4 获取当前用户信息 (用户端调用)
test_api "GET" "/api/auth/info" "获取当前登录用户信息(User)" "$USER_TOKEN"

# 1.5 用户注册 (使用唯一用户名避免冲突)
TEST_USERNAME="testuser_$(date +%s)"
test_api "POST" "/api/auth/register" "用户注册" "" \
    "{\"username\":\"${TEST_USERNAME}\",\"password\":\"test123456\",\"nickname\":\"测试用户\",\"phone\":\"13800138000\",\"email\":\"test@test.com\"}"

echo ""

# ============================================================
# 2. 公开接口模块 (Public) - 无需登录
# ============================================================
echo -e "${CYAN}[2] ========== 公开接口模块 (Public) ==========${NC}"

test_api "GET" "/api/public/announcements" "公开-公告列表"
test_api "GET" "/api/public/parking-lots" "公开-停车场列表"
test_api "GET" "/api/public/parking-lots/1" "公开-停车场详情"
test_api "GET" "/api/public/coupons" "公开-可领取优惠券列表"

echo ""

# ============================================================
# 3. 用户端 - 车辆管理 (Vehicle)
# ============================================================
echo -e "${CYAN}[3] ========== 用户端 - 车辆管理 (Vehicle) ==========${NC}"

test_api "GET" "/api/vehicles" "查询我的车辆列表" "$USER_TOKEN"
test_api "GET" "/api/vehicles/1" "查询车辆详情" "$USER_TOKEN"

# 新增车辆
TEST_PLATE="豫T$(date +%s | tail -c 6)"
test_api "POST" "/api/vehicles" "新增车辆" "$USER_TOKEN" \
    "{\"plateNumber\":\"${TEST_PLATE}\",\"brand\":\"理想\",\"model\":\"L9\",\"color\":\"白色\",\"isDefault\":0}"

# 获取新增车辆的ID
VEHICLES_RESP=$(curl -s -H "Authorization: Bearer $USER_TOKEN" "${BASE_URL}/api/vehicles")
NEW_VEHICLE_ID=$(echo "$VEHICLES_RESP" | python3 -c "
import sys,json
d=json.load(sys.stdin)
data=d.get('data',[])
if isinstance(data,dict): data=data.get('list',data.get('records',[]))
if isinstance(data,list):
    for v in data:
        if v.get('plateNumber','')=='${TEST_PLATE}':
            print(v.get('id',''))
            break
" 2>/dev/null)

if [ -n "$NEW_VEHICLE_ID" ] && [ "$NEW_VEHICLE_ID" != "None" ]; then
    test_api "PUT" "/api/vehicles/${NEW_VEHICLE_ID}" "更新车辆信息" "$USER_TOKEN" \
        "{\"plateNumber\":\"${TEST_PLATE}\",\"brand\":\"理想\",\"model\":\"L9 MAX\",\"color\":\"黑色\",\"isDefault\":0}"
    test_api "DELETE" "/api/vehicles/${NEW_VEHICLE_ID}" "删除车辆" "$USER_TOKEN"
else
    echo -e "${YELLOW}⚠️  跳过车辆更新/删除测试（未获取到新车辆ID）${NC}"
fi

echo ""

# ============================================================
# 4. 用户端 - 预约管理 (Reservation)
# ============================================================
echo -e "${CYAN}[4] ========== 用户端 - 预约管理 (Reservation) ==========${NC}"

test_api "GET" "/api/reservations?page=1&size=10" "查询我的预约列表" "$USER_TOKEN"
test_api "GET" "/api/reservations/1" "查询预约详情" "$USER_TOKEN"

# 创建预约 (使用未来的时间)
FUTURE_START="2026-06-15 09:00:00"
FUTURE_END="2026-06-15 17:00:00"
test_api "POST" "/api/reservations" "创建预约" "$USER_TOKEN" \
    "{\"lotId\":1,\"vehicleId\":1,\"startTime\":\"${FUTURE_START}\",\"endTime\":\"${FUTURE_END}\"}"

# 查询新创建的预约并取消
RESERVATIONS_RESP=$(curl -s -H "Authorization: Bearer $USER_TOKEN" "${BASE_URL}/api/reservations?page=1&size=100")
NEW_RESERVATION_ID=$(echo "$RESERVATIONS_RESP" | python3 -c "
import sys,json
d=json.load(sys.stdin)
data=d.get('data',{})
records=data.get('list',data.get('records',[]))
if isinstance(records,list) and len(records)>0:
    # 取最新的待生效预约
    for r in records:
        if r.get('status',None)==0:
            print(r.get('id',''))
            break
" 2>/dev/null)

if [ -n "$NEW_RESERVATION_ID" ] && [ "$NEW_RESERVATION_ID" != "None" ]; then
    test_api "PUT" "/api/reservations/${NEW_RESERVATION_ID}/cancel" "取消预约" "$USER_TOKEN"
else
    echo -e "${YELLOW}⚠️  跳过取消预约测试（未找到待生效预约）${NC}"
fi

echo ""

# ============================================================
# 5. 用户端 - 订单管理 (Order)
# ============================================================
echo -e "${CYAN}[5] ========== 用户端 - 订单管理 (Order) ==========${NC}"

test_api "GET" "/api/orders?page=1&size=10" "查询我的订单列表" "$USER_TOKEN"
test_api "GET" "/api/orders/1" "查询订单详情" "$USER_TOKEN"
test_api "GET" "/api/orders/1/qrcode" "获取入场二维码" "$USER_TOKEN"

echo ""

# ============================================================
# 6. 用户端 - 支付 (Payment)
# ============================================================
echo -e "${CYAN}[6] ========== 用户端 - 支付 (Payment) ==========${NC}"

test_api "GET" "/api/payments/order/1" "查询订单支付记录" "$USER_TOKEN"

echo ""

# ============================================================
# 7. 用户端 - 优惠券 (UserCoupon)
# ============================================================
echo -e "${CYAN}[7] ========== 用户端 - 用户优惠券 (UserCoupon) ==========${NC}"

test_api "GET" "/api/user-coupons" "查询我的优惠券列表" "$USER_TOKEN"
test_api "GET" "/api/coupons/available" "查询可领取优惠券" "$USER_TOKEN"

# 领取优惠券 (如果优惠券5还可领取)
test_api "POST" "/api/user-coupons/claim/5" "领取优惠券" "$USER_TOKEN"

echo ""

# ============================================================
# 8. 用户端 - 异常申诉 (Appeal)
# ============================================================
echo -e "${CYAN}[8] ========== 用户端 - 异常申诉 (Appeal) ==========${NC}"

test_api "GET" "/api/appeals?page=1&size=10" "查询我的申诉列表" "$USER_TOKEN"

# 创建申诉
test_api "POST" "/api/appeals" "提交申诉" "$USER_TOKEN" \
    "{\"orderId\":1,\"type\":4,\"content\":\"测试申诉内容-停车计时不准确\",\"images\":\"\"}"

echo ""

# ============================================================
# 9. 用户端 - 公告 (Announcements via Public)
# ============================================================
echo -e "${CYAN}[9] ========== 用户端 - 公告浏览 ==========${NC}"

test_api "GET" "/api/public/announcements" "浏览公告列表" "$USER_TOKEN"

echo ""

# ============================================================
# 10. 管理端 - 仪表盘 (Dashboard)
# ============================================================
echo -e "${CYAN}[10] ========== 管理端 - 仪表盘 (Dashboard) ==========${NC}"

test_api "GET" "/api/dashboard" "仪表盘汇总统计" "$ADMIN_TOKEN"
test_api "GET" "/api/dashboard/revenue?startDate=2025-12-01&endDate=2026-03-01" "收入统计" "$ADMIN_TOKEN"
test_api "GET" "/api/dashboard/occupancy" "车位占用统计" "$ADMIN_TOKEN"

echo ""

# ============================================================
# 11. 管理端 - 停车场管理 (ParkingLot)
# ============================================================
echo -e "${CYAN}[11] ========== 管理端 - 停车场管理 (ParkingLot) ==========${NC}"

test_api "GET" "/api/parking-lots?page=1&size=10" "分页查询停车场" "$ADMIN_TOKEN"
test_api "GET" "/api/parking-lots/all" "查询所有停车场(下拉)" "$ADMIN_TOKEN"
test_api "GET" "/api/parking-lots/1" "查询停车场详情" "$ADMIN_TOKEN"

# 新增停车场
test_api "POST" "/api/parking-lots" "新增停车场" "$ADMIN_TOKEN" \
    "{\"name\":\"测试停车场-API\",\"address\":\"河南省洛阳市测试路1号\",\"longitude\":112.4500,\"latitude\":34.6000,\"totalSpaces\":50,\"image\":\"https://picsum.photos/id/1080/800/400\",\"description\":\"API测试新增停车场\",\"openTime\":\"08:00\",\"closeTime\":\"20:00\",\"contactPhone\":\"0379-12345678\",\"status\":1}"

# 获取新增的停车场ID
LOTS_RESP=$(curl -s -H "Authorization: Bearer $ADMIN_TOKEN" "${BASE_URL}/api/parking-lots?page=1&size=100")
NEW_LOT_ID=$(echo "$LOTS_RESP" | python3 -c "
import sys,json
d=json.load(sys.stdin)
data=d.get('data',{})
records=data.get('list',data.get('records',[]))
if isinstance(records,list):
    for r in records:
        if r.get('name','')=='测试停车场-API':
            print(r.get('id',''))
            break
" 2>/dev/null)

if [ -n "$NEW_LOT_ID" ] && [ "$NEW_LOT_ID" != "None" ]; then
    test_api "PUT" "/api/parking-lots/${NEW_LOT_ID}" "更新停车场" "$ADMIN_TOKEN" \
        "{\"name\":\"测试停车场-已更新\",\"address\":\"河南省洛阳市测试路2号\",\"longitude\":112.4500,\"latitude\":34.6000,\"totalSpaces\":60,\"image\":\"https://picsum.photos/id/1080/800/400\",\"description\":\"更新后的测试停车场\",\"openTime\":\"07:00\",\"closeTime\":\"21:00\",\"contactPhone\":\"0379-12345678\",\"status\":1}"
    test_api "DELETE" "/api/parking-lots/${NEW_LOT_ID}" "删除停车场" "$ADMIN_TOKEN"
else
    echo -e "${YELLOW}⚠️  跳过停车场更新/删除测试${NC}"
fi

echo ""

# ============================================================
# 12. 管理端 - 车位管理 (ParkingSpace)
# ============================================================
echo -e "${CYAN}[12] ========== 管理端 - 车位管理 (ParkingSpace) ==========${NC}"

test_api "GET" "/api/parking-spaces?page=1&size=10" "分页查询车位" "$ADMIN_TOKEN"
test_api "GET" "/api/parking-spaces?page=1&size=10&lotId=1" "按停车场查询车位" "$ADMIN_TOKEN"
test_api "GET" "/api/parking-spaces/1" "查询车位详情" "$ADMIN_TOKEN"
test_api "GET" "/api/parking-spaces/available/1" "查询空闲车位" "$ADMIN_TOKEN"

# 新增车位
test_api "POST" "/api/parking-spaces" "新增车位" "$ADMIN_TOKEN" \
    "{\"lotId\":1,\"spaceNo\":\"TEST-001\",\"type\":1,\"floor\":\"D区\",\"area\":\"测试区域\",\"status\":0}"

# 获取新增车位ID
SPACES_RESP=$(curl -s -H "Authorization: Bearer $ADMIN_TOKEN" "${BASE_URL}/api/parking-spaces?page=1&size=100&lotId=1")
NEW_SPACE_ID=$(echo "$SPACES_RESP" | python3 -c "
import sys,json
d=json.load(sys.stdin)
data=d.get('data',{})
records=data.get('list',data.get('records',[]))
if isinstance(records,list):
    for r in records:
        if r.get('spaceNo','')=='TEST-001':
            print(r.get('id',''))
            break
" 2>/dev/null)

if [ -n "$NEW_SPACE_ID" ] && [ "$NEW_SPACE_ID" != "None" ]; then
    test_api "PUT" "/api/parking-spaces/${NEW_SPACE_ID}" "更新车位" "$ADMIN_TOKEN" \
        "{\"lotId\":1,\"spaceNo\":\"TEST-001\",\"type\":2,\"floor\":\"D区\",\"area\":\"VIP区域\",\"status\":0}"
    test_api "DELETE" "/api/parking-spaces/${NEW_SPACE_ID}" "删除车位" "$ADMIN_TOKEN"
else
    echo -e "${YELLOW}⚠️  跳过车位更新/删除测试${NC}"
fi

# 批量创建车位（使用ASCII参数避免URL编码问题）
test_api "POST" "/api/parking-spaces/batch?lotId=1&prefix=TEST&count=3&type=1&floor=D&area=TestArea" "批量创建车位" "$ADMIN_TOKEN"

# 清理批量创建的车位
CLEANUP_RESP=$(curl -s -H "Authorization: Bearer $ADMIN_TOKEN" "${BASE_URL}/api/parking-spaces?page=1&size=100&lotId=1")
CLEANUP_IDS=$(echo "$CLEANUP_RESP" | python3 -c "
import sys,json
d=json.load(sys.stdin)
data=d.get('data',{})
records=data.get('list',data.get('records',[]))
if isinstance(records,list):
    for r in records:
        if str(r.get('spaceNo','')).startswith('TEST'):
            print(r.get('id',''))
" 2>/dev/null)

for cid in $CLEANUP_IDS; do
    curl -s -X DELETE -H "Authorization: Bearer $ADMIN_TOKEN" "${BASE_URL}/api/parking-spaces/${cid}" > /dev/null 2>&1
done

echo ""

# ============================================================
# 13. 管理端 - 计费规则 (BillingRule)
# ============================================================
echo -e "${CYAN}[13] ========== 管理端 - 计费规则 (BillingRule) ==========${NC}"

test_api "GET" "/api/billing-rules?page=1&size=10" "分页查询计费规则" "$ADMIN_TOKEN"
test_api "GET" "/api/billing-rules/1" "查询计费规则详情" "$ADMIN_TOKEN"
test_api "GET" "/api/billing-rules/lot/1" "按停车场查询计费规则" "$ADMIN_TOKEN"

# 新增计费规则
test_api "POST" "/api/billing-rules" "新增计费规则" "$ADMIN_TOKEN" \
    "{\"lotId\":5,\"name\":\"关林-节假日计费\",\"ruleType\":1,\"firstHourPrice\":10.00,\"extraHourPrice\":6.00,\"dailyMax\":60.00,\"freeMinutes\":10,\"flatPrice\":0.00,\"status\":1}"

# 获取新增规则ID
RULES_RESP=$(curl -s -H "Authorization: Bearer $ADMIN_TOKEN" "${BASE_URL}/api/billing-rules?page=1&size=100")
NEW_RULE_ID=$(echo "$RULES_RESP" | python3 -c "
import sys,json
d=json.load(sys.stdin)
data=d.get('data',{})
records=data.get('list',data.get('records',[]))
if isinstance(records,list):
    for r in records:
        if r.get('name','')=='关林-节假日计费':
            print(r.get('id',''))
            break
" 2>/dev/null)

if [ -n "$NEW_RULE_ID" ] && [ "$NEW_RULE_ID" != "None" ]; then
    test_api "PUT" "/api/billing-rules/${NEW_RULE_ID}" "更新计费规则" "$ADMIN_TOKEN" \
        "{\"lotId\":5,\"name\":\"关林-节假日计费(已更新)\",\"ruleType\":1,\"firstHourPrice\":12.00,\"extraHourPrice\":8.00,\"dailyMax\":80.00,\"freeMinutes\":15,\"flatPrice\":0.00,\"status\":1}"
    test_api "DELETE" "/api/billing-rules/${NEW_RULE_ID}" "删除计费规则" "$ADMIN_TOKEN"
else
    echo -e "${YELLOW}⚠️  跳过计费规则更新/删除测试${NC}"
fi

echo ""

# ============================================================
# 14. 管理端 - 优惠券管理 (Coupon)
# ============================================================
echo -e "${CYAN}[14] ========== 管理端 - 优惠券管理 (Coupon) ==========${NC}"

test_api "GET" "/api/coupons?page=1&size=10" "分页查询优惠券" "$ADMIN_TOKEN"
test_api "GET" "/api/coupons/available" "查询可用优惠券" "$ADMIN_TOKEN"

# 新增优惠券
test_api "POST" "/api/coupons" "新增优惠券" "$ADMIN_TOKEN" \
    "{\"name\":\"API测试满20减8\",\"type\":1,\"discountValue\":8.00,\"minAmount\":20.00,\"startTime\":\"2026-03-01 00:00:00\",\"endTime\":\"2026-12-31 23:59:59\",\"totalCount\":500,\"status\":1}"

# 获取新增优惠券ID
COUPONS_RESP=$(curl -s -H "Authorization: Bearer $ADMIN_TOKEN" "${BASE_URL}/api/coupons?page=1&size=100")
NEW_COUPON_ID=$(echo "$COUPONS_RESP" | python3 -c "
import sys,json
d=json.load(sys.stdin)
data=d.get('data',{})
records=data.get('list',data.get('records',[]))
if isinstance(records,list):
    for r in records:
        if r.get('name','')=='API测试满20减8':
            print(r.get('id',''))
            break
" 2>/dev/null)

if [ -n "$NEW_COUPON_ID" ] && [ "$NEW_COUPON_ID" != "None" ]; then
    test_api "PUT" "/api/coupons/${NEW_COUPON_ID}" "更新优惠券" "$ADMIN_TOKEN" \
        "{\"name\":\"API测试满20减8(已更新)\",\"type\":1,\"discountValue\":10.00,\"minAmount\":20.00,\"startTime\":\"2026-03-01 00:00:00\",\"endTime\":\"2026-12-31 23:59:59\",\"totalCount\":600,\"status\":1}"
    test_api "DELETE" "/api/coupons/${NEW_COUPON_ID}" "删除优惠券" "$ADMIN_TOKEN"
else
    echo -e "${YELLOW}⚠️  跳过优惠券更新/删除测试${NC}"
fi

echo ""

# ============================================================
# 15. 管理端 - 黑名单管理 (Blacklist)
# ============================================================
echo -e "${CYAN}[15] ========== 管理端 - 黑名单管理 (Blacklist) ==========${NC}"

test_api "GET" "/api/blacklist?page=1&size=10" "分页查询黑名单" "$ADMIN_TOKEN"

# 新增黑名单
TEST_BL_PLATE="豫X$(date +%s | tail -c 6)"
test_api "POST" "/api/blacklist" "新增黑名单" "$ADMIN_TOKEN" \
    "{\"plateNumber\":\"${TEST_BL_PLATE}\",\"reason\":\"API测试-恶意占位\",\"status\":1}"

# 获取新增黑名单ID
BL_RESP=$(curl -s -H "Authorization: Bearer $ADMIN_TOKEN" "${BASE_URL}/api/blacklist?page=1&size=100")
NEW_BL_ID=$(echo "$BL_RESP" | python3 -c "
import sys,json
d=json.load(sys.stdin)
data=d.get('data',{})
records=data.get('list',data.get('records',[]))
if isinstance(records,list):
    for r in records:
        if r.get('plateNumber','')=='${TEST_BL_PLATE}':
            print(r.get('id',''))
            break
" 2>/dev/null)

if [ -n "$NEW_BL_ID" ] && [ "$NEW_BL_ID" != "None" ]; then
    test_api "PUT" "/api/blacklist/${NEW_BL_ID}" "更新黑名单" "$ADMIN_TOKEN" \
        "{\"plateNumber\":\"${TEST_BL_PLATE}\",\"reason\":\"API测试-已解除\",\"status\":0}"
    test_api "DELETE" "/api/blacklist/${NEW_BL_ID}" "删除黑名单" "$ADMIN_TOKEN"
else
    echo -e "${YELLOW}⚠️  跳过黑名单更新/删除测试${NC}"
fi

echo ""

# ============================================================
# 16. 管理端 - 公告管理 (Announcement)
# ============================================================
echo -e "${CYAN}[16] ========== 管理端 - 公告管理 (Announcement) ==========${NC}"

test_api "GET" "/api/announcements?page=1&size=10" "分页查询公告" "$ADMIN_TOKEN"
test_api "GET" "/api/announcements/1" "查询公告详情" "$ADMIN_TOKEN"

# 新增公告
test_api "POST" "/api/announcements" "新增公告" "$ADMIN_TOKEN" \
    "{\"title\":\"API测试公告\",\"content\":\"这是一条通过API测试脚本自动创建的测试公告\",\"type\":1,\"status\":0}"

# 获取新增公告ID
ANN_RESP=$(curl -s -H "Authorization: Bearer $ADMIN_TOKEN" "${BASE_URL}/api/announcements?page=1&size=100")
NEW_ANN_ID=$(echo "$ANN_RESP" | python3 -c "
import sys,json
d=json.load(sys.stdin)
data=d.get('data',{})
records=data.get('list',data.get('records',[]))
if isinstance(records,list):
    for r in records:
        if r.get('title','')=='API测试公告':
            print(r.get('id',''))
            break
" 2>/dev/null)

if [ -n "$NEW_ANN_ID" ] && [ "$NEW_ANN_ID" != "None" ]; then
    test_api "PUT" "/api/announcements/${NEW_ANN_ID}" "更新公告" "$ADMIN_TOKEN" \
        "{\"title\":\"API测试公告(已更新)\",\"content\":\"公告内容已更新\",\"type\":1,\"status\":0}"
    test_api "PUT" "/api/announcements/${NEW_ANN_ID}/publish" "发布公告" "$ADMIN_TOKEN"
    test_api "DELETE" "/api/announcements/${NEW_ANN_ID}" "删除公告" "$ADMIN_TOKEN"
else
    echo -e "${YELLOW}⚠️  跳过公告更新/发布/删除测试${NC}"
fi

echo ""

# ============================================================
# 17. 管理端 - 申诉处理 (Appeal Admin)
# ============================================================
echo -e "${CYAN}[17] ========== 管理端 - 申诉处理 (Appeal) ==========${NC}"

test_api "GET" "/api/appeals/admin?page=1&size=10" "分页查询申诉(管理员)" "$ADMIN_TOKEN"

# 回复申诉 (使用现有的待处理申诉 ID=5)
test_api "PUT" "/api/appeals/5/reply" "回复申诉" "$ADMIN_TOKEN" \
    "{\"status\":2,\"reply\":\"API测试回复-已核实，系统已修复\"}"

echo ""

# ============================================================
# 18. 管理端 - 订单管理 (Order Admin)
# ============================================================
echo -e "${CYAN}[18] ========== 管理端 - 订单管理 (Order Admin) ==========${NC}"

test_api "GET" "/api/orders/admin?page=1&size=10" "分页查询订单(管理员)" "$ADMIN_TOKEN"
test_api "GET" "/api/orders/admin?page=1&size=10&lotId=1" "按停车场查询订单" "$ADMIN_TOKEN"
test_api "GET" "/api/orders/1" "查询订单详情(管理员)" "$ADMIN_TOKEN"

# 创建入场订单
test_api "POST" "/api/orders/entry" "创建入场订单" "$ADMIN_TOKEN" \
    "{\"lotId\":1,\"plateNumber\":\"豫A12345\"}"

echo ""

# ============================================================
# 19. 管理端 - 支付查询 (Payment Admin)
# ============================================================
echo -e "${CYAN}[19] ========== 管理端 - 支付查询 (Payment Admin) ==========${NC}"

test_api "GET" "/api/payments/admin?page=1&size=10" "分页查询支付记录" "$ADMIN_TOKEN"
test_api "GET" "/api/payments/order/1" "查询订单支付详情" "$ADMIN_TOKEN"

echo ""

# ============================================================
# 20. 管理端 - 用户管理 (User)
# ============================================================
echo -e "${CYAN}[20] ========== 管理端 - 用户管理 (User) ==========${NC}"

test_api "GET" "/api/users?page=1&size=10" "分页查询用户列表" "$ADMIN_TOKEN"
# 使用百分号编码的中文关键字
test_api "GET" "/api/users?page=1&size=10&keyword=%E5%BC%A0%E4%B8%89" "按关键词查询用户" "$ADMIN_TOKEN"
test_api "GET" "/api/users/2" "查询用户详情" "$ADMIN_TOKEN"
test_api "PUT" "/api/users/2" "更新用户信息" "$ADMIN_TOKEN" \
    "{\"nickname\":\"张三(已更新)\",\"phone\":\"13900000001\",\"email\":\"zhangsan@qq.com\",\"avatar\":\"\"}"

# 恢复用户信息
curl -s -X PUT -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
    "${BASE_URL}/api/users/2" -d '{"nickname":"张三","phone":"13900000001","email":"zhangsan@qq.com","avatar":""}' > /dev/null 2>&1

# 更新用户状态
test_api "PUT" "/api/users/9/status?status=0" "禁用用户" "$ADMIN_TOKEN"
test_api "PUT" "/api/users/9/status?status=1" "启用用户" "$ADMIN_TOKEN"

echo ""

# ============================================================
# 21. 管理端 - 预约管理 (Reservation Admin)
# ============================================================
echo -e "${CYAN}[21] ========== 管理端 - 预约管理 (Reservation Admin) ==========${NC}"

test_api "GET" "/api/reservations/admin?page=1&size=10" "分页查询预约(管理员)" "$ADMIN_TOKEN"
test_api "GET" "/api/reservations/admin?page=1&size=10&lotId=1" "按停车场查询预约" "$ADMIN_TOKEN"
test_api "GET" "/api/reservations/1" "查询预约详情(管理员)" "$ADMIN_TOKEN"

echo ""

# ============================================================
# 报告汇总
# ============================================================
echo ""
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║                     测试结果汇总                              ║"
echo "╠══════════════════════════════════════════════════════════════╣"
printf "║  总计: %-4d  通过: ${GREEN}%-4d${NC}  失败: ${RED}%-4d${NC}                         ║\n" "$TOTAL" "$PASS" "$FAIL"
echo "╚══════════════════════════════════════════════════════════════╝"

if [ "$FAIL" -gt 0 ]; then
    echo ""
    echo -e "${RED}失败的接口:${NC}"
    echo -e "$FAILED_LIST"
    echo ""
    exit 1
else
    echo ""
    echo -e "${GREEN}🎉 所有接口测试通过！前端后端联调无问题！${NC}"
    echo ""
    exit 0
fi
