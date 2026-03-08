@echo off
chcp 65001 >nul
title 景区停车引导系统 - 一键启动
setlocal enabledelayedexpansion

:: ============================================================
::  景区停车引导系统 - 一键启动脚本 (Windows)
:: ============================================================

set "PROJECT_DIR=%~dp0"
set "BACKEND_DIR=%PROJECT_DIR%backend"
set "FRONTEND_DIR=%PROJECT_DIR%frontend"
set "ADMIN_DIR=%PROJECT_DIR%admin"
set "SQL_DIR=%PROJECT_DIR%sql"
set "LOG_DIR=%PROJECT_DIR%.logs"

set "DB_HOST=localhost"
set "DB_PORT=3306"
set "DB_NAME=parking_system"
set "DB_USER=root"
set "DB_PASS=ab123168"

set "BACKEND_PORT=8087"
set "FRONTEND_PORT=3000"
set "ADMIN_PORT=9527"

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

echo.
echo ╔══════════════════════════════════════════╗
echo ║     景区停车引导系统                     ║
echo ║          一键启动脚本                    ║
echo ╚══════════════════════════════════════════╝
echo.

:: ─── 1. 检查基础环境 ───
echo [1/7] 检查基础环境...
where java >nul 2>&1 || (echo   X java 未安装 & goto :err)
echo   √ java 已安装
where mvn >nul 2>&1 || (echo   X maven 未安装 & goto :err)
echo   √ maven 已安装
where node >nul 2>&1 || (echo   X node 未安装 & goto :err)
echo   √ node 已安装
where pnpm >nul 2>&1 || (
    echo   ! pnpm 未安装，正在安装...
    npm install -g pnpm
)
echo   √ pnpm 已安装

:: ─── 2. 检查 MySQL ───
echo.
echo [2/7] 检查 MySQL...
mysql -h %DB_HOST% -u %DB_USER% -p%DB_PASS% -e "SELECT 1;" >nul 2>&1
if errorlevel 1 (
    echo   尝试启动 MySQL...
    net start mysql >nul 2>&1
    timeout /t 3 /nobreak >nul
)

:: 检查数据库是否存在
mysql -h %DB_HOST% -u %DB_USER% -p%DB_PASS% -e "USE %DB_NAME%;" >nul 2>&1
if errorlevel 1 (
    echo   初始化数据库...
    mysql -h %DB_HOST% -u %DB_USER% -p%DB_PASS% < "%SQL_DIR%\init.sql"
    mysql -h %DB_HOST% -u %DB_USER% -p%DB_PASS% < "%SQL_DIR%\data.sql"
    echo   √ 数据库初始化完成
) else (
    echo   √ 数据库已存在
)

:: ─── 3. 检查依赖 ───
echo.
echo [3/7] 检查项目依赖...
if not exist "%BACKEND_DIR%\target\classes" (
    echo   编译后端...
    cd /d "%BACKEND_DIR%"
    call mvn compile -q
    if errorlevel 1 (echo   X 后端编译失败 & goto :err)
    echo   √ 后端编译完成
) else (
    echo   √ 后端已编译
)

if not exist "%FRONTEND_DIR%\node_modules" (
    echo   安装前端依赖...
    cd /d "%FRONTEND_DIR%"
    call pnpm install
    echo   √ 前端依赖安装完成
) else (
    echo   √ 前端依赖已安装
)

if not exist "%ADMIN_DIR%\node_modules" (
    echo   安装管理端依赖...
    cd /d "%ADMIN_DIR%"
    call pnpm install
    echo   √ 管理端依赖安装完成
) else (
    echo   √ 管理端依赖已安装
)

:: ─── 4. 检查端口 ───
echo.
echo [4/7] 检查端口...
netstat -an | findstr ":%BACKEND_PORT% " | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo   ! 端口 %BACKEND_PORT% 被占用
    set /p "KILL=  是否终止占用进程? [y/N] "
    if /i "!KILL!"=="y" (
        for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%BACKEND_PORT% " ^| findstr "LISTENING"') do taskkill /f /pid %%a >nul 2>&1
    )
)

:: ─── 5. 启动服务 ───
echo.
echo [5/7] 启动服务...

:: 启动后端 (红色窗口)
echo   启动后端服务...
start "后端服务" /min cmd /c "title 后端服务 & color 4F & cd /d %BACKEND_DIR% & mvn spring-boot:run 2>&1 | tee %LOG_DIR%\backend.log & pause"

:: 启动前端 (绿色窗口)
echo   启动前端服务...
start "前端服务" /min cmd /c "title 前端服务 & color 2F & cd /d %FRONTEND_DIR% & pnpm dev 2>&1 | tee %LOG_DIR%\frontend.log & pause"

:: 启动管理端 (蓝色窗口)
echo   启动管理端服务...
start "管理端服务" /min cmd /c "title 管理端服务 & color 1F & cd /d %ADMIN_DIR% & pnpm dev 2>&1 | tee %LOG_DIR%\admin.log & pause"

:: ─── 6. 等待就绪 ───
echo.
echo [6/7] 等待服务就绪...
set "READY=0"
:wait_loop
timeout /t 3 /nobreak >nul
set "READY=0"
netstat -an | findstr ":%BACKEND_PORT% " | findstr "LISTENING" >nul 2>&1 && set /a READY+=1
netstat -an | findstr ":%FRONTEND_PORT% " | findstr "LISTENING" >nul 2>&1 && set /a READY+=1
netstat -an | findstr ":%ADMIN_PORT% " | findstr "LISTENING" >nul 2>&1 && set /a READY+=1
if !READY! lss 3 goto :wait_loop

:: ─── 7. 输出信息 ───
echo.
echo ╔══════════════════════════════════════════╗
echo ║         所有服务已成功启动！             ║
echo ╚══════════════════════════════════════════╝
echo.
echo   用户端前端:  http://localhost:%FRONTEND_PORT%
echo   管理端后台:  http://localhost:%ADMIN_PORT%
echo   后端 API:    http://localhost:%BACKEND_PORT%
echo   API 文档:    http://localhost:%BACKEND_PORT%/doc.html
echo.
echo   测试账号:
echo     管理员: admin / admin123
echo     游客:   user1 / 123456
echo.
echo   按任意键打开浏览器...
pause >nul
start http://localhost:%FRONTEND_PORT%
start http://localhost:%ADMIN_PORT%
goto :eof

:err
echo.
echo 启动失败，请检查错误信息
pause
exit /b 1
