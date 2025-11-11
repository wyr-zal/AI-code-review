#!/bin/bash

# AI代码审查平台 - 前端快速启动脚本

echo "====================================="
echo "  AI代码审查平台 - 前端快速启动"
echo "====================================="
echo ""

# 检查Node.js是否安装
if ! command -v node &> /dev/null; then
    echo "[错误] 未检测到Node.js，请先安装Node.js"
    echo "下载地址: https://nodejs.org/"
    exit 1
fi

echo "[信息] Node.js版本:"
node -v
echo ""

# 检查npm是否安装
if ! command -v npm &> /dev/null; then
    echo "[错误] 未检测到npm"
    exit 1
fi

echo "[信息] npm版本:"
npm -v
echo ""

# 检查node_modules是否存在
if [ ! -d "node_modules" ]; then
    echo "[信息] 首次运行，正在安装依赖..."
    echo ""
    npm install
    if [ $? -ne 0 ]; then
        echo ""
        echo "[错误] 依赖安装失败"
        exit 1
    fi
    echo ""
    echo "[成功] 依赖安装完成"
    echo ""
fi

echo "[信息] 正在启动开发服务器..."
echo ""
echo "启动成功后，请访问: http://localhost:3000"
echo ""
echo "测试账号:"
echo "  管理员: admin / 123456"
echo "  普通用户: testuser / 123456"
echo ""
echo "按 Ctrl+C 可停止服务器"
echo ""
echo "====================================="
echo ""

npm run dev
