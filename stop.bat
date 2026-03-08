@echo off
chcp 65001 >nul
title 景区停车引导系统 - 停止服务

:: ============================================================
::  景区停车引导系统 - 一键停止脚本 (Windows)
:: ============================================================

set "BACKEND_PORT=8087"
set "FRONTEND_PORT=3000"
set "ADMIN_PORT=9527"

echo.
echo ╔══════════════════════════════════════════╗
echo ║     景区停车引导系统 - 停止服务          ║
echo ╚══════════════════════════════════════════╝
echo.

:: 停止后端
echo   停止后端服务 (端口 %BACKEND_PORT%)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%BACKEND_PORT% " ^| findstr "LISTENING"') do (
    taskkill /f /pid %%a >nul 2>&1
    echo   √ 后端已停止 (PID: %%a)
)

:: 停止前端
echo   停止前端服务 (端口 %FRONTEND_PORT%)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%FRONTEND_PORT% " ^| findstr "LISTENING"') do (
    taskkill /f /pid %%a >nul 2>&1
    echo   √ 前端已停止 (PID: %%a)
)

:: 停止管理端
echo   停止管理端服务 (端口 %ADMIN_PORT%)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%ADMIN_PORT% " ^| findstr "LISTENING"') do (
    taskkill /f /pid %%a >nul 2>&1
    echo   √ 管理端已停止 (PID: %%a)
)

:: 关闭服务窗口
taskkill /fi "WINDOWTITLE eq 后端服务*" /f >nul 2>&1
taskkill /fi "WINDOWTITLE eq 前端服务*" /f >nul 2>&1
taskkill /fi "WINDOWTITLE eq 管理端服务*" /f >nul 2>&1

echo.
echo   所有服务已停止
echo.
pause
