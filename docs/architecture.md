# 项目架构设计文档

## 1. 系统架构概述

本项目采用**微服务架构**设计，主要包含以下几个服务模块：

- **Gateway服务**：API网关，统一入口
- **User Service**：用户服务，负责用户认证授权
- **AI Review Service**：AI审查服务，核心业务逻辑

## 2. 技术选型理由

### 2.1 为什么选择微服务架构？

1. **服务解耦**：用户服务和审查服务独立部署，互不影响
2. **独立扩展**：AI审查服务可以独立扩容应对高并发
3. **技术异构**：不同服务可以使用不同技术栈
4. **容错隔离**：单个服务故障不影响整体系统

### 2.2 为什么选择Spring Cloud Alibaba？

1. **国内生态**：更适合国内开发者，文档丰富
2. **Nacos**：同时支持服务注册和配置管理
3. **Sentinel**：强大的限流熔断能力
4. **RocketMQ**：高性能消息队列（本项目使用RabbitMQ）

### 2.3 为什么使用Redis？

1. **缓存**：缓存AI审查结果，减少重复调用
2. **分布式锁**：保证任务执行幂等性
3. **限流**：基于Redis实现接口限流
4. **会话管理**：存储用户token

### 2.4 为什么使用RabbitMQ？

1. **异步处理**：AI审查耗时长，异步提升用户体验
2. **削峰填谷**：高峰期任务排队，保护后端服务
3. **解耦**：生产者和消费者解耦
4. **可靠性**：消息持久化，保证不丢失

## 3. 核心功能流程

### 3.1 用户登录流程

```
用户输入账号密码
    ↓
Gateway路由到User Service
    ↓
验证用户名密码（MD5）
    ↓
生成JWT Token
    ↓
Token存储到Redis
    ↓
返回Token给客户端
```

### 3.2 代码审查流程（异步）

```
用户提交代码
    ↓
Gateway验证Token
    ↓
AI Review Service创建任务
    ↓
任务入库（状态：待审查）
    ↓
发送消息到RabbitMQ
    ↓
返回任务ID给用户
    ↓
[异步] MQ消费者接收消息
    ↓
[异步] 获取分布式锁
    ↓
[异步] 调用AI API审查代码
    ↓
[异步] 解析结果并更新数据库
    ↓
[异步] 缓存结果到Redis
```

### 3.3 代码审查流程（同步）

```
用户提交代码
    ↓
Gateway验证Token
    ↓
AI Review Service创建任务
    ↓
任务入库（状态：待审查）
    ↓
同步调用AI API
    ↓
解析结果并更新数据库
    ↓
缓存结果到Redis
    ↓
返回审查结果给用户
```

## 4. 设计模式应用

### 4.1 策略模式（Strategy Pattern）

**应用场景**：支持多种AI模型（GPT、Claude等）

```java
// 策略接口
public interface AIClientStrategy {
    String reviewCode(String code, String language);
    String getModelName();
}

// 具体策略
@Component
public class GPTClientStrategy implements AIClientStrategy {
    // GPT实现
}

@Component
public class ClaudeClientStrategy implements AIClientStrategy {
    // Claude实现
}
```

**优点**：
- 易于扩展新的AI模型
- 符合开闭原则
- 运行时动态切换策略

### 4.2 工厂模式（Factory Pattern）

**应用场景**：根据模型名称创建对应的AI客户端

```java
@Component
public class AIClientFactory {
    private Map<String, AIClientStrategy> strategyMap;

    public AIClientStrategy getStrategy(String model) {
        // 根据model返回对应的策略
    }
}
```

**优点**：
- 统一管理策略创建
- 隐藏创建细节
- 便于维护

### 4.3 模板方法模式（Template Method Pattern）

**应用场景**：统一的代码审查流程

```java
public abstract class AbstractReviewService {
    public final void reviewCode() {
        preCheck();        // 前置检查
        doReview();        // 执行审查（子类实现）
        parseResult();     // 解析结果
        saveResult();      // 保存结果
        postProcess();     // 后置处理
    }

    protected abstract void doReview();
}
```

## 5. 数据库设计

### 5.1 用户表（user）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名，唯一索引 |
| password | VARCHAR(100) | 密码（MD5加密） |
| email | VARCHAR(100) | 邮箱，唯一索引 |
| nickname | VARCHAR(50) | 昵称 |
| avatar | VARCHAR(255) | 头像URL |
| role | TINYINT | 角色（0-普通用户，1-管理员） |
| status | TINYINT | 状态（0-禁用，1-正常） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

**索引设计**：
- PRIMARY KEY: id
- UNIQUE KEY: username, email
- KEY: status, create_time

### 5.2 审查任务表（review_task）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| title | VARCHAR(200) | 任务标题 |
| code_content | TEXT | 代码内容 |
| language | VARCHAR(50) | 编程语言 |
| ai_model | VARCHAR(50) | AI模型 |
| status | TINYINT | 状态（0-待审查，1-审查中，2-已完成，3-失败） |
| review_result | TEXT | 审查结果（JSON） |
| quality_score | INT | 质量评分 |
| security_score | INT | 安全评分 |
| performance_score | INT | 性能评分 |
| issue_count | INT | 问题数量 |
| error_msg | VARCHAR(500) | 错误信息 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

**索引设计**：
- PRIMARY KEY: id
- KEY: user_id, status, create_time

## 6. Redis数据结构设计

### 6.1 用户Token

```
Key: user:token:{userId}
Type: String
Value: JWT Token
TTL: 24小时
```

### 6.2 审查结果缓存

```
Key: ai:review:cache:{taskId}
Type: String
Value: JSON格式的审查结果
TTL: 72小时
```

### 6.3 分布式锁

```
Key: lock:review:{taskId}
Type: String
Value: UUID（锁的持有者标识）
TTL: 5分钟
```

### 6.4 接口限流

```
Key: rate:limit:{userId}:{api}
Type: String
Value: 请求次数
TTL: 1分钟
```

## 7. 消息队列设计

### 7.1 队列定义

```
队列名称：code.review.queue
持久化：是
消息格式：JSON
```

### 7.2 消息结构

```json
{
  "taskId": 123,
  "userId": 456
}
```

### 7.3 消费者配置

```yaml
spring:
  rabbitmq:
    listener:
      simple:
        acknowledge-mode: auto      # 自动确认
        concurrency: 3              # 并发消费者数量
        max-concurrency: 10         # 最大并发数
```

## 8. 性能优化策略

### 8.1 缓存策略

1. **审查结果缓存**：相同代码的审查结果缓存72小时
2. **用户信息缓存**：用户信息缓存，减少数据库查询
3. **热点数据预热**：系统启动时加载热点数据

### 8.2 异步处理

1. **消息队列**：耗时的AI调用放入队列异步处理
2. **线程池**：使用线程池处理并发任务
3. **回调机制**：审查完成后通过WebSocket推送结果

### 8.3 数据库优化

1. **索引优化**：为常用查询字段建立索引
2. **连接池**：Druid连接池配置（min:5, max:20）
3. **慢查询监控**：记录并优化慢SQL

### 8.4 限流熔断

1. **网关层限流**：Sentinel限流，防止服务过载
2. **用户级限流**：每个用户每分钟最多10次请求
3. **熔断降级**：AI服务异常时返回默认结果

## 9. 安全设计

### 9.1 认证授权

1. **JWT Token**：无状态认证，token存储到Redis
2. **Token刷新**：24小时过期，支持刷新
3. **权限控制**：基于角色的权限管理（RBAC）

### 9.2 数据安全

1. **密码加密**：MD5加密存储（生产环境建议BCrypt）
2. **SQL注入防护**：MyBatis预编译SQL
3. **XSS防护**：输入参数校验和转义
4. **敏感信息脱敏**：日志中脱敏处理

### 9.3 接口安全

1. **请求签名**：重要接口增加签名验证
2. **重放攻击防护**：时间戳 + nonce
3. **HTTPS**：生产环境强制HTTPS

## 10. 监控与运维

### 10.1 日志管理

1. **日志级别**：开发环境DEBUG，生产环境INFO
2. **日志格式**：统一的日志格式，便于分析
3. **日志收集**：ELK收集和分析日志

### 10.2 性能监控

1. **JVM监控**：内存、GC、线程等
2. **接口监控**：响应时间、成功率、QPS
3. **中间件监控**：Redis、MySQL、RabbitMQ状态

### 10.3 告警机制

1. **服务宕机告警**
2. **接口异常告警**
3. **资源使用率告警**

## 11. 扩展性设计

### 11.1 水平扩展

1. **无状态服务**：所有服务都是无状态的，可以随意扩容
2. **负载均衡**：Nginx + Nacos实现负载均衡
3. **数据库分库分表**：支持按用户ID分库

### 11.2 功能扩展

1. **插件机制**：支持自定义审查规则插件
2. **多语言支持**：支持更多编程语言
3. **团队协作**：团队审查报告、代码评审协作

## 12. 面试常见问题

### Q1: 为什么使用分布式锁？

**问题场景**：同一个任务可能被多次消费（消息重复投递），导致重复审查

**解决方案**：
- 使用Redis分布式锁，key为`lock:review:{taskId}`
- 执行前尝试获取锁，获取成功才执行
- 使用UUID作为锁的value，释放时验证
- 设置锁超时时间，防止死锁

### Q2: AI API调用失败如何处理？

**处理策略**：
1. **重试机制**：指数退避重试（1s, 2s, 4s...）
2. **降级策略**：切换到其他AI模型
3. **熔断保护**：连续失败后熔断，避免雪崩
4. **错误记录**：记录错误信息，更新任务状态

### Q3: 如何保证消息不丢失？

**保证措施**：
1. **消息持久化**：队列和消息都设置为持久化
2. **手动ACK**：消费成功后手动确认
3. **死信队列**：失败消息进入死信队列
4. **数据库记录**：任务先入库再发送消息

### Q4: 高并发场景如何优化？

**优化方案**：
1. **异步处理**：耗时操作异步化
2. **缓存**：Redis缓存热点数据
3. **限流**：Sentinel限流保护
4. **数据库优化**：索引优化、读写分离
5. **服务扩容**：水平扩展服务实例

---

**本文档作为技术面试的参考资料，帮助深入理解项目设计思路**
