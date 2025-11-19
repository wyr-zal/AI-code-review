# AI代码审查平台 - 项目上下文

> 快速恢复项目上下文，执行 `/load` 命令即可

**最后更新**: 2025-11-19

## 用户偏好
- ⚠️ **每次只展示当前对话内容，不用展示之前的历史对话**
- ⚠️ **不需要执行git操作（add、commit、push等），用户自己处理**

---

## 项目信息

**项目名称**: AI 代码审查平台
**项目路径**: E:\project\AI-project\ai-code-review-platform
**技术栈**: Spring Boot 2.7.14 + Spring Cloud + Nacos + Vue 3
**Java 版本**: 1.8

### 服务端口
- gateway: 8000
- user-service: 8001
- ai-review-service: 8002

### 技术栈详细
- **前端**: Vue 3 + Vite + Element Plus + Pinia + Vue Router
- **后端**: Spring Boot 2.7.14 + Spring Cloud 2021.0.8 + MyBatis-Plus 3.5.3.1
- **中间件**: Nacos + Redis + RabbitMQ
- **数据库**: MySQL 8.0
- **AI集成**: OpenAI GPT、Claude (通过 iFlow 平台)

---

## 关键配置

### 基础服务
- **MySQL**: 192.168.100.128:3306/code_review
- **Redis**: 192.168.100.128:6379 (database: 2)
- **RabbitMQ**: 192.168.100.128:5672
- **Nacos**: 192.168.100.128:8848

### AI 配置
- **平台**: iFlow (心流) - https://platform.iflow.cn
- **API URL**: https://apis.iflow.cn/v1/chat/completions ⭐
- **模型**: Qwen3-Coder（代码专用，256k上下文）
- **兼容性**: OpenAI API 100% 兼容

### Swagger 文档
- user-service: http://localhost:8001/swagger-ui/index.html
- ai-review-service: http://localhost:8002/swagger-ui/index.html

---

## 历史问题修复记录

### 2025-11-19 (本次会话)

#### 监控系统集成

**背景**：为平台添加 Prometheus + Grafana 监控系统

**完成内容**：
1. ✅ **配置类创建**
   - `ActuatorConfig.java` - Actuator端点配置（用于user-service和ai-review-service）
   - `MeterRegistryConfig.java` - Micrometer指标注册配置
   - `MetricsUtils.java` - 监控指标工具类

2. ✅ **Maven依赖**
   - 所有服务添加 `micrometer-registry-prometheus` 依赖
   - 主pom.xml添加版本管理 (1.9.12)

3. ✅ **Actuator端点配置**
   - gateway、user-service、ai-review-service 的 `application.yml` 全部配置 management 端点
   - 暴露所有端点，启用Prometheus支持

4. ✅ **Docker Compose监控栈**
   - 创建 `docker-compose.monitoring.yml`
   - 服务包括：Prometheus、Grafana、AlertManager、Node Exporter
   - Exporter: MySQL、Redis、RabbitMQ
   - 修复基础设施连接地址（192.168.100.128）

5. ✅ **Prometheus配置**
   - `prometheus.yml` - 监控所有微服务和基础设施
   - `prometheus-rules.yml` - 告警规则（服务宕机、高QPS、高响应时间等）

6. ✅ **Grafana配置**
   - `ai-code-review-dashboard.json` - 监控仪表板
   - 修复Prometheus查询表达式（使用正���匹配 `job=~"..."`）
   - 自动加载数据源和仪表板

7. ✅ **环境变量配置**
   - 创建 `.env.monitoring.example` 示例文件

**修改文件**：
- ✅ `common/config/ActuatorConfig.java` (新建)
- ✅ `common/config/MeterRegistryConfig.java` (新建)
- ✅ `common/utils/MetricsUtils.java` (新建)
- ✅ `common/pom.xml` (添加actuator和micrometer依赖)
- ✅ `gateway/pom.xml` (添加micrometer依赖)
- ✅ `user-service/pom.xml` (添加micrometer依赖)
- ✅ `ai-review-service/pom.xml` (添加micrometer依赖)
- ✅ `gateway/application.yml` (添加management配置)
- ✅ `user-service/application.yml` (添加management配置)
- ✅ `ai-review-service/application.yml` (添加management配置)
- ✅ `pom.xml` (添加micrometer版本管理)
- ✅ `docker-compose.monitoring.yml` (新建)
- ✅ `monitoring/prometheus.yml` (新建)
- ✅ `monitoring/prometheus-rules.yml` (新建)
- ✅ `monitoring/alertmanager.yml` (新建)
- ✅ `monitoring/grafana-dashboards/ai-code-review-dashboard.json` (新建)
- ✅ `monitoring/grafana-dashboards/dashboards.yml` (新建)
- ✅ `monitoring/grafana-datasources/prometheus.yml` (新建)
- ✅ `monitoring/README.md` (新建)
- ✅ `.env.monitoring.example` (新建)

**启动监控系统**：
```bash
# 1. 配置环境变量
cp .env.monitoring.example .env.monitoring
# 编辑 .env.monitoring，设置MySQL密码等

# 2. 启动监控服务
docker-compose -f docker-compose.monitoring.yml up -d

# 3. 访问监控界面
# Prometheus: http://localhost:9090
# Grafana: http://localhost:3000 (admin/admin)
# AlertManager: http://localhost:9093
```

---

### 2025-11-11

#### 第一轮修复：数据库重复创建 & 时间不显示

**问题1：数据库重复创建记录**
- **现象**: 每提交一个审查任务，数据库会创建两条记录
- **根本原因**: 异步审查流程逻辑错误
  1. `submitReviewTask()` 创建第一条记录并发送到MQ
  2. 监听器收到消息后调用 `executeSyncReview()`
  3. `executeSyncReview()` 又创建了第二条记录
- **修复方案**:
  - 新增 `executeAsyncReview(Long taskId)` 方法
  - 监听器改为调用该方法，直接使用已存在的任务
- **修改文件**:
  - ✅ `ai-review-service/service/ReviewService.java`
  - ✅ `ai-review-service/service/impl/ReviewServiceImpl.java`
  - ✅ `ai-review-service/listener/ReviewTaskListener.java`

**问题2：前端时间不显示**
- **现象**: 历史记录页面的创建时间显示为空
- **根本原因**: 缺少 MyBatis-Plus 的 `MetaObjectHandler` 配置
  - 实体类使用了 `@TableField(fill = FieldFill.INSERT)` 注解
  - 但没有实现处理器，导致 `createTime` 和 `updateTime` 未自动填充
- **修复方案**:
  - 创建 `MyBatisPlusConfig` 实现 `MetaObjectHandler` 接口
  - 自动填充创建时间和更新时间
- **修改文件**:
  - ✅ `common/config/MyBatisPlusConfig.java` (新建)

#### 第二轮修复：前端查询功能 & JWT验证

**问题3：查询筛选功能无效**
- **现象**: 前端历史页面的状态和语言筛选不起作用
- **根本原因**: 后端接口只接收 `page` 和 `size` 参数，忽略了筛选参数
- **修复方案**:
  - 创建 `ReviewTaskQueryDTO` 封装查询参数
  - 修改 Controller、Service、ServiceImpl 支持筛选
- **修改文件**:
  - ✅ `ai-review-service/dto/ReviewTaskQueryDTO.java` (新建)
  - ✅ `ai-review-service/controller/ReviewController.java`
  - ✅ `ai-review-service/service/ReviewService.java`
  - ✅ `ai-review-service/service/impl/ReviewServiceImpl.java`

**问题4：JWT过期未验证**
- **现象**: Token 过期后仍可访问受保护页面
- **根本原因**: 路由守卫只检查 token 是否存在，未验证过期时间
- **修复方案**:
  - 添加 `isTokenExpired()` 函数解析 JWT payload
  - 检查 `exp` 字段判断是否过期
  - 过期则清除 localStorage 并跳转登录页
- **修改文件**:
  - ✅ `frontend/src/router/index.js`

---

### 2025-11-11 (之前的工作)

1. ✅ **前端 keep-alive 数据持久化** - 修复 Dashboard.vue，使用 `router-view v-slot` 语法，添加生命周期调试日志
2. ✅ **Nacos 服务注册 IP 问题修复** - 删除错误的 `discovery.ip: 192.168.100.128:8848`，添加 `ip: 127.0.0.1` 强制本地注册
3. ✅ **网关路由 500 错误修复** - 修复服务注册到错误 IP 导致的路由失败问题
4. ✅ **AI 审查结果解析失败修复** - 添加 `extractJsonContent()` 方法，支持从 markdown 代码块、文本中提取 JSON
5. ✅ **前端解析容错处理** - Review.vue 和 Detail.vue 添加详细日志和类型判断
6. ✅ **创建时间显示修复** - 添加 `formatDateTime()` 函数，支持数组、字符串、Date 对象等多种时间格式
7. ✅ **Redis 工具类重构** - 从 `RedisTemplate<String, Object>` 重构为 `StringRedisTemplate`，添加对象缓存方法

### 2025-11-10

1. ✅ **Nacos 配置问题修复** - 修复命名空间 ID 配置错误（使用 UUID 而非名称）

### 2025-11-09

1. ✅ **前端数据持久化** - Dashboard.vue 添加 keep-alive，防止页面切换数据丢失
2. ✅ **后端分页统一** - 创建 PageResponseDTO，统一返回 `{records, total}` 格式
3. ✅ **iFlow AI 集成** - 配置官方 API 和 Qwen3-Coder 模型
4. ✅ **Swagger 文档** - 为所有 Controller 添加 OpenAPI 注解
5. ✅ **Java 8 兼容** - List.of() 改为 Arrays.asList()
6. ✅ **环境变量配置** - 所有配置改为 `${变量名:默认值}` 格式
7. ✅ **Nacos Config** - 添加配置中心支持

---

## 关键技术点

### 1. 前端数据持久化（keep-alive）
```vue
<!-- frontend/src/views/Dashboard.vue -->
<router-view v-slot="{ Component }">
  <keep-alive :include="['Review', 'History']">
    <component :is="Component" />
  </keep-alive>
</router-view>
```
组件需添加 `defineOptions({ name: 'Review' })` 并使用 `onActivated/onDeactivated` 钩子

### 2. Nacos 服务注册配置
```yaml
# bootstrap.yml - 本地开发环境
spring:
  cloud:
    nacos:
      server-addr: 192.168.100.128:8848
      discovery:
        namespace: 8f3b736b-7758-431d-ac34-a6eca5bb7474
        ip: 127.0.0.1  # 强制使用 localhost，避免注册到虚拟网卡
```

### 3. AI 审查结果解析
```java
// ReviewServiceImpl.java
private String extractJsonContent(String content) {
    // 支持多种格式：
    // 1. Markdown 代码块: ```json\n{...}\n```
    // 2. 普通代码块: ```...```
    // 3. 文本 + JSON: 说明文字\n{...}
    // 4. 纯 JSON: {...}
}
```

### 4. 统一分页响应
```java
// ai-review-service/.../dto/PageResponseDTO.java
public class PageResponseDTO<T> {
    private List<T> records;
    private Long total;
    private Integer page;
    private Integer size;
}
```

### 5. 时间格式化（LocalDateTime 数组问题）
```javascript
// 后端返回: [2025, 11, 11, 10, 30, 0]
// 前端格式化: formatDateTime(dateTime)
// 输出: "2025-11-11 10:30:00"
```

### 6. 环境变量配置
```yaml
# application.yml
datasource:
  url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:code_review}
ai:
  openai:
    api-key: ${OPENAI_API_KEY:}
    api-url: ${OPENAI_API_URL:https://apis.iflow.cn/v1/chat/completions}
```

### 7. Redis 工具类（StringRedisTemplate + JSON）
```java
// 字符串缓存
redisUtils.set("key", "value", 1, TimeUnit.HOURS);
String value = redisUtils.get("key");

// 对象缓存（自动 JSON 序列化）
User user = new User();
redisUtils.setObject("user:1", user, 2, TimeUnit.HOURS);
User cachedUser = redisUtils.getObject("user:1", User.class);

// 复杂类型缓存（List、Map 等）
List<User> users = Arrays.asList(user1, user2);
redisUtils.setObject("users", users);
List<User> cachedUsers = redisUtils.getObject("users", new TypeReference<List<User>>(){});

// 分布式锁
String lockValue = UUID.randomUUID().toString();
if (redisUtils.tryLock("lock:order:123", lockValue, 5, TimeUnit.MINUTES)) {
    try {
        // 业务逻辑
    } finally {
        redisUtils.unlock("lock:order:123", lockValue);
    }
}
```

### 8. MyBatis-Plus 自动填充
```java
// common/config/MyBatisPlusConfig.java
@Component
public class MyBatisPlusConfig implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

### 9. JWT 过期验证
```javascript
// frontend/src/router/index.js
function isTokenExpired(token) {
    try {
        const parts = token.split('.')
        const payload = JSON.parse(atob(parts[1]))
        if (!payload.exp) return false
        return Date.now() >= payload.exp * 1000
    } catch (error) {
        return true
    }
}
```

### 10. 查询 DTO 封装
```java
// ai-review-service/dto/ReviewTaskQueryDTO.java
@Data
public class ReviewTaskQueryDTO {
    private Integer page = 1;
    private Integer size = 10;
    private Integer status;      // 0-待审查 1-审查中 2-已完成 3-失败
    private String language;     // Java/Python/JavaScript
}
```

---

## 常见问题快速处理

### Nacos 服务注册问题
- **错误配置**: `discovery.ip: 192.168.100.128:8848` ❌ (把 Nacos 服务器地址当成服务地址)
- **自动选择虚拟网卡**: 删除 `ip` 配置后可能注册到 VirtualBox 虚拟网卡 (192.168.56.1)
- **正确配置**: `discovery.ip: 127.0.0.1` ✅ (本地开发强制使用 localhost)
- **命名空间配置**: 必须使用命名空间 ID（UUID），不能使用名称
  ```yaml
  namespace: 8f3b736b-7758-431d-ac34-a6eca5bb7474  # ✅ 正确
  namespace: ai-code-review                         # ❌ 错误
  ```

### 网关路由失败 (500 错误)
**现象**: 前端请求网关返回 500，后端服务收不到请求
**原因**: 服务注册到错误的 IP 地址，网关无法路由
**排查步骤**:
1. 打开 Nacos 控制台: http://192.168.100.128:8848/nacos
2. 查看服务列表，检查服务注册的 IP 是否正确
3. 应该是 `127.0.0.1:8001/8002`，而不是其他 IP
4. 修改 bootstrap.yml 添加 `discovery.ip: 127.0.0.1`
5. 重启服务

### AI 审查结果解析失败
**现象**: 后端日志显示解析失败，前端无法显示审查结果
**原因**: AI 返回的内容包含 markdown 代码块或额外说明文字
**解决**: 使用 `extractJsonContent()` 方法自动提取 JSON

### 前端时间显示问题
**现象**: 创建时间列显示为空或显示数组
**原因**: 后端 LocalDateTime 被序列化为数组 `[2025, 11, 11, 10, 30, 0]`
**解决**: 使用 `formatDateTime()` 函数格式化显示

### 前端数据丢失问题
**现象**: 切换页面后，表单数据或筛选条件丢失
**原因**: 组件被销毁，数据未缓存
**解决**:
1. Dashboard.vue 使用 keep-alive 缓存组件
2. 组件添加 `defineOptions({ name: 'xxx' })`
3. 使用 `onActivated` 替代 `onMounted`

### 数据库重复创建记录
**现象**: 提交一个审查任务，数据库创建两条记录
**原因**: 异步审查流程中，监听器调用了会重新创建任务的方法
**解决**: 监听器改为调用 `executeAsyncReview(taskId)` 方法

### 查询筛选不起作用
**现象**: 前端选择状态或语言筛选后，列表数据不变
**原因**: 后端接口未接收筛选参数
**解决**: 创建 DTO 封装查询参数，修改 Service 实现筛选逻辑

### JWT 过期后仍可访问
**现象**: Token 过期后未自动跳转登录页
**原因**: 路由守卫只检查 token 是否存在
**解决**: 添加 JWT 过期时间验证逻辑

### 端口占用
```cmd
netstat -ano | findstr :8000
taskkill /F /PID <PID号>
```

### Maven 依赖刷新
- IDEA: 右侧 Maven → 刷新按钮
- 命令行: `mvn clean install -DskipTests`

### API 调用失败
1. 检查 API Key 是否配置
2. 确认 API URL: https://apis.iflow.cn
3. 查看后端日志详细错误

---

## 服务启动顺序

1. 确保基础服务运行（MySQL、Redis、RabbitMQ、Nacos）
2. 启动 user-service (8001)
3. 启动 ai-review-service (8002)
4. 启动 gateway (8000)
5. 启动前端 (npm run dev)

---

## 重要提醒

- ⭐ iFlow API 官方地址: `https://apis.iflow.cn/v1/chat/completions`
- ⚠️ 项目使用 Java 1.8，避免使用 Java 9+ 特性
- 📝 修改 pom.xml 后必须刷新 Maven
- 🔄 环境变量通过 `-D参数` 或环境变量传入
- 🔒 JWT 过期验证已添加到路由守卫
- 📊 查询筛选功能已支持状态和语言过滤
- ⏰ 时间字段自动填充已配置
- 🚫 数据库重复创建问题已修复

---

*文档最后更新: 2025-11-11*
