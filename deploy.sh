#!/bin/bash

# 部署脚本 - 用于生产环境部署
set -e

echo "🚀 开始部署学习室管理系统..."

# 配置变量
REGISTRY="ghcr.io"
IMAGE_NAME="$GITHUB_REPOSITORY"
ADMIN_TAG="latest"
HOME_TAG="latest"

# 检查Docker是否可用
if ! command -v docker &> /dev/null; then
    echo "❌ Docker未安装"
    exit 1
fi

# 检查docker-compose是否可用
if ! command -v docker-compose &> /dev/null; then
    echo "❌ docker-compose未安装"
    exit 1
fi

# 拉取最新镜像
echo "📦 拉取最新Docker镜像..."
docker pull $REGISTRY/$IMAGE_NAME-client_admin:$ADMIN_TAG
docker pull $REGISTRY/$IMAGE_NAME-client_home:$HOME_TAG

# 停止现有服务
echo "🛑 停止现有服务..."
docker-compose down

# 启动新服务
echo "▶️ 启动新服务..."
docker-compose up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
echo "🔍 检查服务状态..."
if docker-compose ps | grep -q "Up"; then
    echo "✅ 部署成功！"
    echo "🌐 管理端访问地址: http://localhost:8080"
    echo "🌐 用户端访问地址: http://localhost:8081"
else
    echo "❌ 部署失败，请检查日志"
    docker-compose logs
    exit 1
fi

echo "🎉 部署完成！"