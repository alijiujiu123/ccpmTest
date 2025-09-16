#!/bin/bash

# API测试脚本 - CV Agent认证相关接口
# 使用前请确保应用已启动在默认端口8080

# 设置基础URL
BASE_URL="http://localhost:8080/api"

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印函数
print_info() {
    echo -e "${YELLOW}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 1. 用户注册
print_info "开始用户注册测试..."
REGISTER_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/register" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "testuser",
        "email": "testuser@example.com",
        "fullName": "测试用户",
        "password": "password123"
    }')

echo "注册响应:"
echo "$REGISTER_RESPONSE"
echo ""

# 从注册响应中提取token（如果注册成功）
TOKEN=$(echo "$REGISTER_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    print_info "注册可能失败，尝试使用登录获取token..."

    # 2. 用户登录
    print_info "开始用户登录测试..."
    LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/login" \
        -H "Content-Type: application/json" \
        -d '{
            "username": "testuser",
            "password": "password123"
        }')

    echo "登录响应:"
    echo "$LOGIN_RESPONSE"
    echo ""

    # 从登录响应中提取token
    TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
fi

# 3. 获取当前用户信息
if [ ! -z "$TOKEN" ]; then
    print_success "成功获取token: ${TOKEN:0:50}..."

    print_info "开始获取当前用户信息..."
    USER_RESPONSE=$(curl -s -X GET "${BASE_URL}/auth/me" \
        -H "Authorization: Bearer ${TOKEN}")

    echo "当前用户信息:"
    echo "$USER_RESPONSE"
    echo ""

else
    print_error "无法获取token，请检查用户名和密码是否正确"
fi

# 额外的测试用例
print_info "额外的测试用例:"

# 测试重复用户名注册
print_info "测试重复用户名注册..."
DUPLICATE_REGISTER_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/register" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "testuser",
        "email": "another@example.com",
        "fullName": "另一个用户",
        "password": "password123"
    }')

echo "重复注册响应:"
echo "$DUPLICATE_REGISTER_RESPONSE"
echo ""

# 测试无效登录
print_info "测试无效登录..."
INVALID_LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "nonexistent",
        "password": "wrongpassword"
    }')

echo "无效登录响应:"
echo "$INVALID_LOGIN_RESPONSE"
echo ""

# 测试无token访问用户信息
print_info "测试无token访问用户信息..."
NO_TOKEN_RESPONSE=$(curl -s -X GET "${BASE_URL}/auth/me")

echo "无token访问响应:"
echo "$NO_TOKEN_RESPONSE"
echo ""

print_success "所有测试完成！"