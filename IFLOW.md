# AI Code Review Platform - iFlow Context

## 项目概述

AI代码审查平台是一个基于AI大模型的智能代码审查系统，采用微服务架构。该平台集成了OpenAI GPT、Claude等AI模型，为开发者提供专业的代码质量分析、安全漏洞检测和性能优化建议。

### 核心特性

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

## 技术栈

### 后端技术

- **基础框架**: Spring Boot 2.7.14, Spring Cloud 2021.0.8
- **微服务组件**: Spring Cloud Alibaba 2021.0.5.0
- **持久层**: MyBatis-Plus 3.5.3.1, MySQL 8.0.33
- **中间件**: Redis, RabbitMQ, Nacos
- **其他**: JWT 0.11.5, Lombok 1.18.28, Hutool 5.8.20

### 前端技术

- **框架**: Vue 3.3.4, Vite 4.4.9
- **路由**: Vue Router 4.2.4
- **状态管理**: Pinia 2.1.6
- **UI组件库**: Element Plus 2.3.14
- **HTTP客户端**: Axios 1.5.0
- **代码编辑器**: CodeMirror 6

### AI模型

- OpenAI GPT-4 / GPT-3.5-Turbo
- Anthropic Claude-3
- 支持扩展其他AI模型

## 项目结构

```
ai-code-review-platform/
├── frontend/                    # 前端项目（Vue 3）
├── common/                      # 公共模块
├── gateway/                     # 网关服务（端口：8000）
├── user-service/               # 用户服务（端口：8001）
├── ai-review-service/          # AI审查服务（端口：8002）
├── sql/                        # SQL脚本
└── pom.xml                     # 父级Maven配置
```

## 构建和运行

### 后端服务

1. **环境要求**:
   - JDK 8+
   - Maven 3.6+
   - MySQL 8.0+
   - Redis 5.0+
   - RabbitMQ 3.8+
   - Nacos 2.0+

2. **编译打包**:
   ```bash
   mvn clean package -DskipTests
   ```

3. **启动服务**:
   ```bash
   # 启动网关服务
   cd gateway && mvn spring-boot:run

   # 启动用户服务
   cd user-service && mvn spring-boot:run

   # 启动AI审查服务
   cd ai-review-service && mvn spring-boot:run
   ```

### 前端服务

1. **环境要求**:
   - Node.js 16+
   - npm 7+ 或 yarn 1.22+

2. **安装依赖**:
   ```bash
   cd frontend
   npm install
   ```

3. **启动开发服务器**:
   ```bash
   npm run dev
   ```

4. **构建生产版本**:
   ```bash
   npm run build
   ```

## 开发约定

### 后端开发

1. **代码结构**:
   - 使用标准的Maven项目结构
   - 控制器层（controller）、服务层（service）、数据访问层（mapper）、实体类（entity）、数据传输对象（dto）
   - 公共代码放在common模块中

2. **统一响应格式**:
   - 使用`Result<T>`类作为统一的API响应格式
   - 状态码200表示成功，其他表示失败

3. **异常处理**:
   - 使用`BusinessException`处理业务异常
   - 全局异常处理器`GlobalExceptionHandler`

4. **AI集成**:
   - 使用策略模式（Strategy Pattern）实现不同的AI客户端
   - 使用工厂模式（Factory Pattern）管理AI客户端实例

### 前端开发

1. **代码结构**:
   - `src/api/`: API接口调用
   - `src/components/`: 可复用组件
   - `src/views/`: 页面组件
   - `src/router/`: 路由配置
   - `src/stores/`: 状态管理
   - `src/utils/`: 工具函数

2. **API调用**:
   - 使用Axios进行HTTP请求
   - 统一的请求拦截器和响应拦截器

3. **组件开发**:
   - 使用Vue 3 Composition API
   - 遵循Element Plus组件库的使用规范

## 数据库设计

### 核心表结构

1. **用户表 (user)**:
   - 存储用户基本信息、角色和状态
   - 支持逻辑删除

2. **代码审查任务表 (review_task)**:
   - 存储代码审查任务信息
   - 包含代码内容、AI模型、审查状态和结果
   - 支持逻辑删除

### 初始化数据

- 默认创建管理员用户（用户名：admin，密码：123456）
- 默认创建测试用户（用户名：testuser，密码：123456）

## 核心功能模块

### 用户认证模块

- 用户注册、登录
- JWT Token生成和验证
- 用户信息获取

### 代码审查模块

- 提交代码审查任务（异步）
- 同步代码审查
- 查询审查结果
- 获取审查任务列表

### AI集成模块

- 支持多种AI模型
- 策略模式实现模型切换
- 统一的AI客户端接口