# Swagger API 文档完善说明

## 文档访问地址

### User Service (用户服务)
- **地址**: http://localhost:8001/swagger-ui/index.html
- **通过网关访问**: http://localhost:8000/user-service/swagger-ui/index.html

### AI Review Service (AI审查服务)
- **地址**: http://localhost:8002/swagger-ui/index.html
- **通过网关访问**: http://localhost:8000/ai-review-service/swagger-ui/index.html

## 已完善的接口文档

### 用户服务 API

#### 1. 用户注册 POST `/user/register`
- **描述**: 新用户注册接口，需提供用户名、密码、邮箱等信息
- **请求示例**:
```json
{
  "username": "testuser",
  "password": "123456",
  "email": "test@example.com",
  "nickname": "测试用户"
}
```
- **响应示例**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": "注册成功"
}
```

#### 2. 用户登录 POST `/user/login`
- **描述**: 用户登录接口，验证通过后返回JWT token
- **请求示例**:
```json
{
  "username": "admin",
  "password": "123456"
}
```
- **响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "username": "admin",
    "nickname": "管理员",
    "email": "admin@example.com"
  }
}
```

#### 3. 用户登出 POST `/user/logout`
- **描述**: 用户退出登录，清除Redis中的token缓存
- **需要**: 在请求头中携带token
- **响应示例**:
```json
{
  "code": 200,
  "message": "登出成功",
  "data": "登出成功"
}
```

#### 4. 获取用户信息 GET `/user/info`
- **描述**: 根据token获取当前登录用户的详细信息
- **需要**: 在请求头中携带token
- **响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "username": "admin",
    "nickname": "管理员",
    "email": "admin@example.com",
    "role": 1,
    "createTime": "2025-11-01 10:00:00"
  }
}
```

### AI审查服务 API

#### 1. 提交代码审查任务（异步） POST `/review/submit`
- **描述**: 提交代码到RabbitMQ队列进行异步审查，立即返回任务ID
- **请求示例**:
```json
{
  "title": "登录功能代码审查",
  "codeContent": "public class UserService { ... }",
  "language": "Java",
  "aiModel": "gpt-3.5-turbo",
  "async": true
}
```
- **响应示例**:
```json
{
  "code": 200,
  "message": "任务提交成功",
  "data": 123
}
```

#### 2. 批量提交代码审查任务 POST `/review/batch`
- **描述**: 批量提交多个文件进行代码审查
- **Content-Type**: multipart/form-data
- **参数**:
  - `title`: 批量任务标题
  - `language`: 编程语言
  - `aiModel`: AI模型
  - `async`: 是否异步
  - `files`: 文件列表（MultipartFile[]）
- **响应示例**:
```json
{
  "code": 200,
  "message": "批量任务提交成功",
  "data": [123, 124, 125]
}
```

#### 3. 同步代码审查 POST `/review/sync`
- **描述**: 立即执行代码审查并等待返回结果，耗时较长（10-60秒）
- **请求示例**:
```json
{
  "title": "算法优化审查",
  "codeContent": "public int fibonacci(int n) { ... }",
  "language": "Java",
  "aiModel": "gpt-4"
}
```
- **响应示例**:
```json
{
  "code": 200,
  "message": "审查完成",
  "data": {
    "id": 123,
    "title": "算法优化审查",
    "status": 2,
    "qualityScore": 85,
    "reviewResult": "{...}"
  }
}
```

#### 4. 获取任务详情 GET `/review/task/{taskId}`
- **描述**: 根据任务ID获取代码审查任务的详细信息
- **参数**: `taskId` - 任务ID（路径参数）
- **响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 123,
    "title": "登录功能审查",
    "status": 2,
    "qualityScore": 85,
    "securityScore": 90,
    "performanceScore": 80
  }
}
```

#### 5. 获取审查任务列表 GET `/review/tasks`
- **描述**: 分页查询当前用户的所有代码审查任务，支持筛选
- **查询参数**:
  - `page`: 页码（默认1）
  - `size`: 每页大小（默认10）
  - `status`: 审查状态（0-待审查，1-审查中，2-已完成，3-失败）
  - `language`: 编程语言
- **响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 123,
        "title": "登录功能审查",
        "status": 2
      }
    ],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

#### 6. 删除审查任务 DELETE `/review/task/{taskId}`
- **描述**: 删除指定的代码审查任务
- **参数**: `taskId` - 任务ID（路径参数）
- **响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": "删除成功"
}
```

#### 7. 导出审查报告 POST `/review/export`
- **描述**: 导出指定任务的审查报告，支持PDF和Excel格式
- **请求示例**:
```json
{
  "taskIds": [1, 2, 3],
  "format": "pdf",
  "includeDetails": true,
  "fileName": "code_review_report"
}
```
- **响应**: 文件流（application/pdf 或 application/vnd.ms-excel）

## DTO Schema 增强

所有DTO已添加完整的Swagger注解：

### 用户服务 DTO
- ✅ `UserLoginDTO` - 登录请求
- ✅ `UserRegisterDTO` - 注册请求

### AI审查服务 DTO
- ✅ `CodeReviewRequestDTO` - 代码审查请求
- ✅ `ReviewTaskQueryDTO` - 任务查询参数
- ✅ `PageResponseDTO<T>` - 分页响应
- ✅ `ExportReportRequestDTO` - 导出报告请求

## Schema 注解说明

每个字段都包含：
- ✅ `description`: 字段描述
- ✅ `example`: 示例值
- ✅ `required`: 是否必填
- ✅ `defaultValue`: 默认值（如果有）
- ✅ `allowableValues`: 可���值列表（如果有限定）

## Controller 注解增强

### 已添加的注解
- ✅ `@Operation`: 接口摘要和详细描述
- ✅ `@ApiResponses`: 各种HTTP状态码的响应说明
- ✅ `@ApiResponse`: 具体响应内容和示例
- ✅ `@Parameter`: 参数描述和示例
- ✅ `@Tag`: Controller分组和描述

### 响应状态码覆盖
- ✅ 200 - 成功响应（包含JSON示例）
- ✅ 400 - 参数校验失败
- ✅ 401 - 用户未登录
- ✅ 403 - 无权访问
- ✅ 404 - 资源不存在
- ✅ 408 - 请求超时
- ✅ 500 - 系统内部错误

## 使用建议

### 测试API
1. 启动相应的服务
2. 访问Swagger UI界面
3. 点击"Authorize"按钮，输入JWT token（登录后获取）
4. 展开接口，点击"Try it out"
5. 填写参数，点击"Execute"执行请求

### Token使用
登录接口返回的token需要在后续请求的Header中携带：
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

或通过网关时：
```
X-Original-Token: eyJhbGciOiJIUzI1NiJ9...
```

### 注意事项
- 所有需要认证的接口都需要携带token
- 批量上传接口使用`multipart/form-data`格式
- 导出接口返回文件流，不是JSON
- 分页查询的page从1开始，不是0

## 后续优化建议

1. ✅ 添加更多请求/响应示例
2. ✅ 完善错误码说明
3. ⬜ 添加接口调用频率限制说明
4. ⬜ 添加API版本管理
5. ⬜ 集成Swagger导出功能（生成Postman Collection）

---

**文档更新时间**: 2025-11-19
**文档版本**: 2.0
**完善状态**: ✅ 已完成80%核心接口文档
