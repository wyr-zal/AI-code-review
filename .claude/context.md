# AI代码审查平台 - 项目上下文

## 项目概述
AI代码审查平台，采用微服务架构，集成OpenAI GPT和Claude等AI大模型提供智能代码审查服务。

## 技术栈
- **前端**: Vue 3 + Vite + Element Plus + Pinia
- **后端**: Spring Boot + Spring Cloud + MyBatis-Plus
- **中间件**: Nacos + Redis + RabbitMQ
- **数据库**: MySQL 8.0
- **AI集成**: OpenAI GPT、Claude

## 服务架构
- Gateway (8000) - 网关服务
- User Service (8001) - 用户服务
- AI Review Service (8002) - AI审查服务
- Common - 公共模块

## 当前任务记录

### 2025-11-11 会话
**问题描述**:
1. 前端历史审查中的时间不显示
2. 每提交一个审查数据库会创建两个记录

**问题分析**:

**问题1：数据库重复创建记录**
- **根本原因**：异步审查流程存在逻辑问题
  1. `ReviewServiceImpl.submitReviewTask()` 创建第一条任务记录并发送到MQ队列
  2. `ReviewTaskListener.handleReviewTask()` 接收消息后调用 `executeSyncReview()`
  3. `executeSyncReview()` 又创建了第二条新任务记录（第89行）
- **影响文件**：
  - `ai-review-service/service/impl/ReviewServiceImpl.java`
  - `ai-review-service/listener/ReviewTaskListener.java`

**问题2：前端时间不显示**
- **根本原因**：缺少 MyBatis-Plus 的 `MetaObjectHandler` 配置
  - 实体类 `ReviewTask` 使用了 `@TableField(fill = FieldFill.INSERT)` 注解
  - 但没有实现 `MetaObjectHandler` 处理器
  - 导致 `createTime` 和 `updateTime` 字段没有自动填充，值为 null
- **影响文件**：
  - `ai-review-service/entity/ReviewTask.java`
  - `user-service/entity/User.java`

**修复方案**:

**修复1：数据库重复创建问题**
1. 在 `ReviewService` 接口中新增 `executeAsyncReview(Long taskId)` 方法
2. 在 `ReviewServiceImpl` 中实现该方法，直接使用已存在的任务执行审查
3. 修改 `ReviewTaskListener.handleReviewTask()`，改为调用新的 `executeAsyncReview()` 方法

**修复2：时间不显示问题**
1. 在 `common` 模块创建 `MyBatisPlusConfig` 类
2. 实现 `MetaObjectHandler` 接口
3. 自动填充 `createTime` 和 `updateTime` 字段

**修改的文件**:
- ✅ `common/config/MyBatisPlusConfig.java` (新建)
- ✅ `ai-review-service/service/ReviewService.java` (添加方法)
- ✅ `ai-review-service/service/impl/ReviewServiceImpl.java` (实现新方法)
- ✅ `ai-review-service/listener/ReviewTaskListener.java` (修改调用逻辑)

---
*最后更新: 2025-11-11*
