# 快速启动指南

## 前置条件检查

在启动项目前，请确保已安装以下软件：

- [x] JDK 8 或更高版本
- [x] Maven 3.6+
- [x] MySQL 8.0+
- [x] Redis 5.0+
- [x] RabbitMQ 3.8+
- [x] Nacos 2.0+

## 一键启动脚本

### Windows环境

创建 `start-all.bat` 文件：

```batch
@echo off
echo ====== 启动AI代码审查平台 ======

echo [1/3] 启动Gateway服务...
start cmd /k "cd gateway && mvn spring-boot:run"
timeout /t 5

echo [2/3] 启动User Service...
start cmd /k "cd user-service && mvn spring-boot:run"
timeout /t 5

echo [3/3] 启动AI Review Service...
start cmd /k "cd ai-review-service && mvn spring-boot:run"

echo ====== 所有服务启动完成 ======
echo Gateway: http://localhost:8000
echo User Service: http://localhost:8001
echo AI Review Service: http://localhost:8002
pause
```

### Linux/Mac环境

创建 `start-all.sh` 文件：

```bash
#!/bin/bash

echo "====== 启动AI代码审查平台 ======"

echo "[1/3] 启动Gateway服务..."
cd gateway && nohup mvn spring-boot:run > ../logs/gateway.log 2>&1 &
sleep 5

echo "[2/3] 启动User Service..."
cd ../user-service && nohup mvn spring-boot:run > ../logs/user-service.log 2>&1 &
sleep 5

echo "[3/3] 启动AI Review Service..."
cd ../ai-review-service && nohup mvn spring-boot:run > ../logs/ai-review-service.log 2>&1 &

echo "====== 所有服务启动完成 ======"
echo "Gateway: http://localhost:8000"
echo "User Service: http://localhost:8001"
echo "AI Review Service: http://localhost:8002"
```

## 详细启动步骤

### 第一步：启动中间件

#### 1. 启动MySQL

```bash
# Windows
net start mysql

# Linux/Mac
sudo service mysql start

# 或使用Docker
docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root --name mysql mysql:8.0
```

#### 2. 启动Redis

```bash
# Windows
redis-server

# Linux/Mac
redis-server /usr/local/etc/redis.conf

# 或使用Docker
docker run -d -p 6379:6379 --name redis redis:latest
```

#### 3. 启动RabbitMQ

```bash
# Windows
rabbitmq-server

# Linux/Mac
brew services start rabbitmq

# 或使用Docker
docker run -d -p 5672:5672 -p 15672:15672 --name rabbitmq rabbitmq:management
```

访问RabbitMQ管理界面：http://localhost:15672
- 用户名：guest
- 密码：guest

#### 4. 启动Nacos

```bash
# Windows
cd nacos/bin
startup.cmd -m standalone

# Linux/Mac
cd nacos/bin
sh startup.sh -m standalone
```

访问Nacos控制台：http://localhost:8848/nacos
- 用户名：nacos
- 密码：nacos

### 第二步：初始化数据库

```bash
# 登录MySQL
mysql -u root -p

# 执行SQL脚本
source sql/schema.sql

# 或者直接导入
mysql -u root -p < sql/schema.sql
```

验证数据库创建：
```sql
USE code_review;
SHOW TABLES;
SELECT * FROM user;
```

### 第三步：配置AI API密钥

编辑 `ai-review-service/src/main/resources/application.yml`：

```yaml
ai:
  openai:
    api-key: sk-xxxxxxxxxxxxxxxxxxxxxxxxx  # 替换为你的OpenAI API Key
```

**获取OpenAI API Key：**
1. 访问 https://platform.openai.com/
2. 登录账号
3. 进入 API Keys 页面
4. 创建新的 API Key

### 第四步：编译项目

```bash
# 编译所有模块
mvn clean install -DskipTests

# 或者分别编译
cd common && mvn clean install
cd ../gateway && mvn clean install
cd ../user-service && mvn clean install
cd ../ai-review-service && mvn clean install
```

### 第五步：启动服务

**方式一：IDE启动（推荐开发环境）**

使用IDEA或Eclipse：
1. 导入Maven项目
2. 分别运行各个模块的 Application 启动类
   - GatewayApplication
   - UserServiceApplication
   - AIReviewServiceApplication

**方式二：命令行启动**

```bash
# 启动Gateway
cd gateway
mvn spring-boot:run

# 启动User Service（新终端）
cd user-service
mvn spring-boot:run

# 启动AI Review Service（新终端）
cd ai-review-service
mvn spring-boot:run
```

**方式三：打包后启动**

```bash
# 打包
mvn clean package -DskipTests

# 启动Gateway
java -jar gateway/target/gateway-1.0.0.jar

# 启动User Service
java -jar user-service/target/user-service-1.0.0.jar

# 启动AI Review Service
java -jar ai-review-service/target/ai-review-service-1.0.0.jar
```

### 第六步：验证服务

#### 1. 检查Nacos服务注册

访问 http://localhost:8848/nacos，查看服务列表：
- gateway-service
- user-service
- ai-review-service

#### 2. 测试用户登录

```bash
curl -X POST http://localhost:8000/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

期望返回：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "username": "admin"
  }
}
```

#### 3. 测试代码审查

```bash
# 替换 {token} 为上一步返回的token
curl -X POST http://localhost:8000/api/review/submit \
  -H "Content-Type: application/json" \
  -H "Authorization: {token}" \
  -d '{
    "title": "测试审查",
    "codeContent": "public class Test { public static void main(String[] args) { System.out.println(\"Hello\"); } }",
    "language": "Java",
    "aiModel": "gpt-3.5-turbo",
    "async": true
  }'
```

## 常见问题排查

### 问题1: 端口被占用

```bash
# Windows查看端口占用
netstat -ano | findstr :8000
taskkill /F /PID {进程ID}

# Linux/Mac查看端口占用
lsof -i :8000
kill -9 {进程ID}
```

### 问题2: 连接MySQL失败

检查：
1. MySQL是否启动
2. 用户名密码是否正确
3. 数据库是否存在

```bash
mysql -u root -p -e "SHOW DATABASES;"
```

### 问题3: 连接Redis失败

```bash
# 测试Redis连接
redis-cli ping
# 应该返回 PONG
```

### 问题4: Nacos服务注册失败

检查：
1. Nacos是否启动
2. application.yml中Nacos地址是否正确
3. 网络是否通畅

### 问题5: AI API调用失败

检查：
1. API Key是否正确
2. 网络是否能访问OpenAI
3. API额度是否充足
4. 查看日志错误信息

## 日志查看

### 应用日志

```bash
# 查看Gateway日志
tail -f gateway/logs/spring.log

# 查看User Service日志
tail -f user-service/logs/spring.log

# 查看AI Review Service日志
tail -f ai-review-service/logs/spring.log
```

### 中间件日志

```bash
# MySQL日志
tail -f /var/log/mysql/error.log

# Redis日志
tail -f /var/log/redis/redis-server.log

# RabbitMQ日志
tail -f /var/log/rabbitmq/rabbit@hostname.log
```

## 停止服务

### Windows

```batch
# 找到Java进程并关闭
tasklist | findstr java
taskkill /F /PID {进程ID}
```

### Linux/Mac

```bash
# 查找并停止所有Spring Boot应用
ps aux | grep spring-boot
kill -9 {进程ID}

# 或者创建停止脚本 stop-all.sh
#!/bin/bash
pkill -f "spring-boot:run"
```

## 性能调优建议

### JVM参数优化

```bash
# 启动时添加JVM参数
java -jar \
  -Xms512m \
  -Xmx1024m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  app.jar
```

### 数据库连接池

编辑 application.yml：

```yaml
spring:
  datasource:
    druid:
      initial-size: 10
      min-idle: 10
      max-active: 30
      max-wait: 60000
```

## 下一步

- 阅读 [API文档](README.md#api接口文档)
- 了解 [架构设计](docs/architecture.md)
- 查看 [开发规范](docs/dev-guide.md)（待补充）

---

**如有问题，请查看项目文档或提交Issue**
