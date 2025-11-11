# AI Code Review Platform

## 项目简介

AI代码审查平台是一个基于AI大模型的智能代码审查系统，采用微服务架构，集成OpenAI GPT、Claude等AI模型，为开发者提供专业的代码质量分析、安全漏洞检测和性能优化建议。

## 项目亮点

### 技术亮点

1. **微服务架构**
   - 网关服务（Gateway）：统一入口、路由转发、跨域处理
   - 用户服务（User Service）：用户认证、权限管理
   - AI审查服务（AI Review Service）：核心业务逻辑、AI集成

2. **AI集成**
   - 支持多种AI模型（GPT-4、GPT-3.5、Claude-3等）
   - 策略模式实现AI客户端切换
   - 智能化代码分析和问题发现

3. **分布式特性**
   - Redis分布式锁保证任务幂等性
   - RabbitMQ异步消息队列处理耗时任务
   - Nacos服务注册与发现

4. **高性能优化**
   - Redis缓存审查结果，提升响应速度
   - 数据库连接池（Druid）优化
   - 异步审查模式，提升用户体验

5. **设计模式应用**
   - 策略模式：支持多种AI模型
   - 工厂模式：AI客户端工厂
   - 模板方法模式：统一审查流程

### 业务亮点

- 智能代码质量评分（0-100分）
- 安全漏洞自动检测
- 性能瓶颈分析
- 最佳实践建议
- 历史审查记录管理

## 技术栈

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.3.4 | 渐进式JavaScript框架 |
| Vite | 4.4.9 | 下一代前端构建工具 |
| Vue Router | 4.2.4 | 路由管理器 |
| Pinia | 2.1.6 | 状态管理 |
| Element Plus | 2.3.14 | UI组件库 |
| Axios | 1.5.0 | HTTP客户端 |
| Nginx | - | Web服务器 |

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.14 | 基础框架 |
| Spring Cloud | 2021.0.8 | 微服务框架 |
| Spring Cloud Alibaba | 2021.0.5.0 | 阿里微服务组件 |
| MyBatis-Plus | 3.5.3.1 | 持久层框架 |
| MySQL | 8.0.33 | 关系型数据库 |
| Redis | - | 缓存 & 分布式锁 |
| RabbitMQ | - | 消息队列 |
| Nacos | - | 服务注册发现 |
| Sentinel | - | 限流熔断 |
| Druid | 1.2.18 | 数据库连接池 |
| JWT | 0.11.5 | 认证授权 |
| Lombok | 1.18.28 | 简化开发 |
| Hutool | 5.8.20 | 工具类库 |
| FastJson2 | 2.0.40 | JSON处理 |
| OkHttp | 4.11.0 | HTTP客户端 |

### AI模型

- OpenAI GPT-4 / GPT-3.5-Turbo
- Anthropic Claude-3
- 支持扩展其他AI模型

## 项目结构

```
ai-code-review-platform/
├── frontend/                    # 前端项目（Vue 3）
│   ├── src/
│   │   ├── api/                # API接口
│   │   ├── assets/             # 静态资源
│   │   ├── components/         # 公共组件
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # 状态管理
│   │   ├── utils/              # 工具函数
│   │   └── views/              # 页面组件
│   ├── nginx.conf              # Nginx配置
│   ├── Dockerfile              # Docker配置
│   ├── README.md               # 前端使用说明
│   ├── DEPLOYMENT.md           # 前端部署指南
│   └── package.json            # 依赖配置
│
├── common/                      # 公共模块
│   └── src/main/java/com/codereview/common/
│       ├── constant/           # 常量定义
│       ├── enums/              # 枚举类
│       ├── exception/          # 异常处理
│       ├── result/             # 统一响应
│       └── utils/              # 工具类（JWT、Redis等）
│
├── gateway/                     # 网关服务（端口：8000）
│   └── src/main/
│       ├── java/com/codereview/gateway/
│       └── resources/
│           └── application.yml
│
├── user-service/               # 用户服务（端口：8001）
│   └── src/main/java/com/codereview/user/
│       ├── controller/         # 控制器
│       ├── service/            # 服务层
│       ├── mapper/             # 数据访问层
│       ├── entity/             # 实体类
│       └── dto/                # 数据传输对象
│
├── ai-review-service/          # AI审查服务（端口：8002）
│   └── src/main/java/com/codereview/review/
│       ├── controller/         # 控制器
│       ├── service/            # 服务层
│       ├── mapper/             # 数据访问层
│       ├── entity/             # 实体类
│       ├── dto/                # 数据传输对象
│       ├── strategy/           # AI策略模式
│       └── listener/           # MQ监听器
│
├── sql/                        # SQL脚本
│   └── schema.sql             # 数据库初始化脚本
│
└── pom.xml                     # 父级Maven配置
```

## 快速开始

### 环境要求

**后端环境：**
- JDK 8+
- Maven 3.6+
- MySQL 8.0+
- Redis 5.0+
- RabbitMQ 3.8+
- Nacos 2.0+

**前端环境：**
- Node.js 16+
- npm 7+ 或 yarn 1.22+
- Nginx（生产环境）

### 安装步骤

#### 1. 克隆项目

```bash
git clone https://github.com/yourusername/ai-code-review-platform.git
cd ai-code-review-platform
```

#### 2. 数据库初始化

```bash
# 创建数据库并执行SQL脚本
mysql -u root -p < sql/schema.sql
```

默认创建了两个测试用户：
- 管理员：用户名 `admin`，密码 `123456`
- 普通用户：用户名 `testuser`，密码 `123456`

#### 3. 安装中间件

**启动 Nacos**
```bash
# 下载 Nacos（https://nacos.io）
sh startup.sh -m standalone
# 访问：http://localhost:8848/nacos
# 默认账号密码：nacos/nacos
```

**启动 Redis**
```bash
redis-server
```

**启动 RabbitMQ**
```bash
rabbitmq-server
# 访问管理界面：http://localhost:15672
# 默认账号密码：guest/guest
```

#### 4. 配置AI API密钥

修改 `ai-review-service/src/main/resources/application.yml`：

```yaml
ai:
  openai:
    api-key: your-openai-api-key-here  # 替换为你的OpenAI API Key
    api-url: https://api.openai.com/v1/chat/completions
    model: gpt-3.5-turbo
```

#### 5. 编译打包

```bash
mvn clean package -DskipTests
```

#### 6. 启动后端服务

```bash
# 启动网关服务
cd gateway && mvn spring-boot:run

# 启动用户服务
cd user-service && mvn spring-boot:run

# 启动AI审查服务
cd ai-review-service && mvn spring-boot:run
```

#### 7. 启动前端服务

**Windows系统：**
```bash
cd frontend
# 双击运行 start.bat
# 或手动执行
npm install
npm run dev
```

**Linux/Mac系统：**
```bash
cd frontend
chmod +x start.sh
./start.sh
# 或手动执行
npm install
npm run dev
```

前端访问地址：http://localhost:3000

**测试账号：**
- 管理员：`admin` / `123456`
- 普通用户：`testuser` / `123456`

## API接口文档

### 用户服务（User Service）

#### 1. 用户注册

```http
POST /api/user/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456",
  "email": "test@example.com",
  "nickname": "测试用户"
}
```

#### 2. 用户登录

```http
POST /api/user/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456"
}

Response:
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "email": "test@example.com"
  }
}
```

#### 3. 获取用户信息

```http
GET /api/user/info
Authorization: {token}
```

### AI审查服务（AI Review Service）

#### 1. 提交代码审查（异步）

```http
POST /api/review/submit
Authorization: {token}
Content-Type: application/json

{
  "title": "登录功能代码审查",
  "codeContent": "public class UserService { ... }",
  "language": "Java",
  "aiModel": "gpt-3.5-turbo",
  "async": true
}

Response:
{
  "code": 200,
  "message": "任务提交成功",
  "data": 1  // 任务ID
}
```

#### 2. 同步代码审查

```http
POST /api/review/sync
Authorization: {token}
Content-Type: application/json

{
  "title": "算法优化建议",
  "codeContent": "public int fibonacci(int n) { ... }",
  "language": "Java",
  "aiModel": "gpt-4"
}
```

#### 3. 查询审查结果

```http
GET /api/review/task/{taskId}
Authorization: {token}
```

#### 4. 获取审查任务列表

```http
GET /api/review/tasks?page=1&size=10
Authorization: {token}
```

## 核心功能演示

### 代码审查示例

**输入代码：**
```java
public class UserService {
    public User login(String username, String password) {
        String sql = "SELECT * FROM user WHERE username='" + username + "'";
        // ... 执行SQL查询
        if (user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
}
```

**AI审查结果：**
```json
{
  "summary": "发现多个严重安全问题和代码质量问题",
  "qualityScore": 45,
  "securityScore": 20,
  "performanceScore": 60,
  "issues": [
    {
      "type": "安全漏洞",
      "severity": "high",
      "description": "存在SQL注入漏洞，直接拼接用户输入到SQL语句中",
      "suggestion": "使用PreparedStatement预编译SQL，避免SQL注入"
    },
    {
      "type": "安全问题",
      "severity": "high",
      "description": "密码使用明文比较，未进行加密处理",
      "suggestion": "使用BCrypt或其他加密算法对密码进行加密比较"
    },
    {
      "type": "代码质量",
      "severity": "medium",
      "description": "缺少异常处理和日志记录",
      "suggestion": "添加try-catch异常处理，记录关键操作日志"
    }
  ],
  "suggestions": [
    "使用MyBatis或JPA等ORM框架",
    "实现统一的密码加密策略",
    "添加登录失败次数限制，防止暴力破解"
  ]
}
```

## 架构设计

### 系统架构图

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────┐
│          Gateway (8000)             │
│  - 路由转发                          │
│  - 跨域处理                          │
│  - 限流熔断 (Sentinel)               │
└──────┬──────────────────────────────┘
       │
       ├───────────────┬──────────────────┐
       ▼               ▼                  ▼
┌─────────────┐ ┌─────────────┐  ┌──────────────┐
│User Service │ │AI Review    │  │   Nacos      │
│   (8001)    │ │Service      │  │   (8848)     │
│             │ │  (8002)     │  │              │
│- 用户认证    │ │- 代码审查    │  │- 服务注册     │
│- JWT授权    │ │- AI集成     │  │- 配置中心     │
└──────┬──────┘ └──────┬──────┘  └──────────────┘
       │               │
       └───────┬───────┘
               │
    ┌──────────┼──────────┬──────────┐
    ▼          ▼          ▼          ▼
┌───────┐ ┌───────┐ ┌─────────┐ ┌──────────┐
│ MySQL │ │ Redis │ │RabbitMQ │ │ AI APIs  │
└───────┘ └───────┘ └─────────┘ └──────────┘
```

### 技术特色

1. **策略模式**：支持多种AI模型动态切换
2. **工厂模式**：AI客户端工厂统一管理
3. **分布式锁**：Redis保证任务执行幂等性
4. **消息队列**：RabbitMQ异步处理耗时任务
5. **缓存优化**：Redis缓存审查结果
6. **服务治理**：Nacos注册中心 + Sentinel限流

## 面试要点

### 1. 项目背景

"在实际开发中，代码审查是保证代码质量的重要环节，但传统人工审查效率低、标准不统一。本项目利用AI大模型的代码理解能力，实现智能化代码审查，提高开发效率和代码质量。"

### 2. 技术难点

**Q: 如何保证同一任务不被重复执行？**
A: 使用Redis分布式锁，在执行审查前尝试获取锁，锁的key为`lock:review:{taskId}`，使用UUID作为锁的value，审查完成后释放锁。

**Q: AI API调用失败如何处理？**
A:
- 使用策略模式，支持多种AI模型切换
- 设置合理的超时时间（60秒）
- 捕获异常并记录错误信息，更新任务状态为失败
- 后续可以增加重试机制（指数退避）

**Q: 如何优化大量代码审查的性能？**
A:
- 异步审查：提交任务到RabbitMQ，后台处理
- Redis缓存：相同代码的审查结果缓存72小时
- 数据库连接池优化：Druid连接池配置
- 限流：Sentinel防止服务过载

### 3. 亮点总结

- ✅ 微服务架构实战经验
- ✅ AI大模型集成（OpenAI、Claude）
- ✅ 分布式技术应用（Redis锁、RabbitMQ）
- ✅ 设计模式应用（策略、工厂）
- ✅ 高并发场景优化（缓存、异步）
- ✅ 完整的项目工程化实践

## 后续优化方向

1. **功能扩展**
   - 支持批量代码文件审查
   - 代码对比与变更审查
   - 团队审查报告统计
   - 审查规则自定义

2. **性能优化**
   - AI调用结果缓存优化
   - 代码分片并行审查
   - 审查任务优先级队列

3. **安全增强**
   - 代码加密存储
   - 审查结果权限控制
   - API访问频率限制

4. **可观测性**
   - 接入Skywalking链路追踪
   - Prometheus + Grafana监控
   - ELK日志分析

## 许可证

MIT License

## 联系方式

- 作者：CodeReview Team
- 邮箱：contact@codereview.com
- 项目地址：https://github.com/yourusername/ai-code-review-platform

---

**适用场景：Java后端开发、AI应用、微服务架构相关岗位的简历项目**
