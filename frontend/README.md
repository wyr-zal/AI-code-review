# AI代码审查平台 - 前端使用说明

## 目录

- [项目介绍](#项目介绍)
- [快速开始](#快速开始)
- [功能说明](#功能说明)
- [开发指南](#开发指南)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [常见问题](#常见问题)

---

## 项目介绍

AI代码审查平台前端是一个基于 **Vue 3 + Vite + Element Plus** 构建的现代化单页应用（SPA），为用户提供直观、高效的代码审查体验。

### 主要功能

- 用户注册与登录
- 代码提交与审查
- 审查历史管理
- 审查结果详情查看
- 个人信息管理

### 技术亮点

- ⚡️ **Vite** - 极速的开发体验
- 🎨 **Element Plus** - 优秀的UI组件库
- 🔒 **JWT认证** - 安全的用户认证
- 📱 **响应式设计** - 完美适配各种设备
- 🎯 **TypeScript** - 类型安全（可选）

---

## 快速开始

### 1. 安装依赖

```bash
cd frontend
npm install
```

### 2. 配置环境变量

复制 `.env` 文件并根据需要修改：

```env
# 后端API地址
VITE_API_BASE_URL=http://localhost:8000

# 应用标题
VITE_APP_TITLE=AI代码审查平台
```

### 3. 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:3000

### 4. 构建生产版本

```bash
npm run build
```

---

## 功能说明

### 1. 用户注册与登录

#### 注册账号

1. 点击登录页面的"立即注册"
2. 填写以下信息：
   - 用户名（3-20个字符）
   - 密码（6-20个字符）
   - 确认密码
   - 邮箱
   - 昵称（2-20个字符）
3. 点击"注册"按钮
4. 注册成功后自动跳转到登录页

#### 登录系统

1. 输入用户名和密码
2. 点击"登录"按钮
3. 登录成功后进入控制台

**测试账号：**
- 管理员：`admin` / `123456`
- 普通用户：`testuser` / `123456`

### 2. 代码审查

#### 提交代码审查

1. 进入"代码审查"页面
2. 填写审查表单：
   - **标题**：审查任务的名称（必填）
   - **编程语言**：选择代码使用的语言
   - **AI模型**：选择审查使用的AI模型
     - GPT-3.5 Turbo（快速、经济）
     - GPT-4（深度分析、精准）
     - Claude-3（平衡性能）
   - **审查模式**：
     - **异步审查**（推荐）：提交后在后台处理，完成后可在历史记录查看
     - **同步审查**：立即返回结果，等待时间较长
   - **代码内容**：粘贴要审查的代码
3. 点击"提交审查"

#### 查看同步审查结果

如果选择同步审查，提交后会弹出结果对话框，包含：
- 质量评分
- 安全评分
- 性能评分
- 审查总结
- 发现的问题列表
- 改进建议

### 3. 审查历史

#### 查看审查记录

1. 进入"审查历史"页面
2. 可以看到所有提交的审查任务
3. 支持筛选：
   - 按状态筛选（待审查、审查中、已完成、失败）
   - 按编程语言筛选
4. 支持操作：
   - 查看详情（已完成的任务）
   - 删除记录

#### 任务状态说明

| 状态 | 说明 | 操作 |
|------|------|------|
| 待审查 | 任务已创建，等待处理 | 等待 |
| 审查中 | AI正在分析代码 | 等待 |
| 已完成 | 审查完成，可查看结果 | 查看详情 |
| 失败 | 审查过程出错 | 查看错误信息 |

### 4. 审查详情

点击"查看详情"后，可以看到：

#### 基本信息
- 任务ID
- 标题
- 编程语言
- AI模型
- 状态
- 创建时间

#### 代码内容
显示提交审查的原始代码

#### 审查结果（仅已完成任务）

**评分系统：**
- 质量评分：代码规范、可维护性
- 安全评分：安全漏洞检测
- 性能评分：性能优化建议

**问题列表：**
每个问题包含：
- 严重程度（高危/中等/低危）
- 问题类型（安全漏洞/代码质量/性能问题等）
- 问题描述
- 优化建议

**改进建议：**
AI提供的整体优化建议列表

### 5. 个人中心

#### 查看个人信息
- 用户ID
- 用户名
- 昵称
- 邮箱
- 角色（管理员/普通用户）
- 状态

#### 编辑资料
1. 点击"编辑资料"
2. 修改昵称或邮箱
3. 点击"确定"保存

#### 修改密码
1. 点击"修改密码"
2. 输入原密码
3. 输入新密码
4. 确认新密码
5. 点击"确定"

---

## 开发指南

### 项目结构

```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API接口
│   │   ├── user.js       # 用户相关API
│   │   └── review.js     # 审查相关API
│   ├── assets/           # 资源文件
│   │   └── style.css     # 全局样式
│   ├── components/       # 公共组件
│   ├── router/           # 路由配置
│   │   └── index.js      # 路由定义
│   ├── stores/           # 状态管理
│   │   └── user.js       # 用户状态
│   ├── utils/            # 工具函数
│   │   └── request.js    # axios封装
│   ├── views/            # 页面组件
│   │   ├── Login.vue     # 登录页
│   │   ├── Register.vue  # 注册页
│   │   ├── Dashboard.vue # 主布局
│   │   ├── Review.vue    # 代码审查
│   │   ├── History.vue   # 审查历史
│   │   ├── Detail.vue    # 审查详情
│   │   └── Profile.vue   # 个人中心
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── .env                  # 环境变量
├── .gitignore           # Git忽略配置
├── Dockerfile           # Docker配置
├── index.html           # HTML模板
├── nginx.conf           # Nginx配置
├── package.json         # 项目配置
└── vite.config.js       # Vite配置
```

### 添加新页面

1. 在 `src/views/` 创建新的Vue组件
2. 在 `src/router/index.js` 添加路由配置
3. 在对应的菜单位置添加链接

示例：

```javascript
// src/router/index.js
{
  path: 'newpage',
  name: 'NewPage',
  component: () => import('@/views/NewPage.vue'),
  meta: { title: '新页面', requiresAuth: true }
}
```

### 添加新API

在 `src/api/` 目录创建或编辑API文件：

```javascript
import request from '@/utils/request'

export function newApi(data) {
  return request({
    url: '/api/xxx',
    method: 'post',
    data
  })
}
```

### 状态管理

使用Pinia进行状态管理，在 `src/stores/` 创建store：

```javascript
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useMyStore = defineStore('my', () => {
  const data = ref(null)

  function setData(newData) {
    data.value = newData
  }

  return { data, setData }
})
```

### 样式定制

#### 修改主题色

编辑 `src/assets/style.css`：

```css
:root {
  --primary-color: #409eff;  /* 主题色 */
  --success-color: #67c23a;
  --warning-color: #e6a23c;
  --danger-color: #f56c6c;
}
```

#### Element Plus主题定制

可以使用Element Plus的主题编辑器：
https://element-plus.org/zh-CN/guide/theming.html

---

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.3.4 | 渐进式JavaScript框架 |
| Vite | 4.4.9 | 下一代前端构建工具 |
| Vue Router | 4.2.4 | Vue官方路由管理器 |
| Pinia | 2.1.6 | Vue官方状态管理库 |
| Element Plus | 2.3.14 | Vue 3组件库 |
| Axios | 1.5.0 | HTTP客户端 |

### 开发依赖

- @vitejs/plugin-vue - Vue 3 Vite插件
- sass - CSS预处理器

---

## API接口文档

### 基础URL

开发环境：`http://localhost:8000`
生产环境：配置在 `.env.production`

### 请求格式

所有请求使用JSON格式：

```javascript
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 用户相关API

#### 注册
- **URL:** `POST /api/user/register`
- **参数:**
  ```json
  {
    "username": "string",
    "password": "string",
    "email": "string",
    "nickname": "string"
  }
  ```

#### 登录
- **URL:** `POST /api/user/login`
- **参数:**
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```
- **返回:**
  ```json
  {
    "token": "string",
    "userId": "number",
    "username": "string",
    "nickname": "string"
  }
  ```

#### 获取用户信息
- **URL:** `GET /api/user/info`
- **Headers:** `Authorization: {token}`

### 审查相关API

#### 提交审查（异步）
- **URL:** `POST /api/review/submit`
- **Headers:** `Authorization: {token}`
- **参数:**
  ```json
  {
    "title": "string",
    "codeContent": "string",
    "language": "string",
    "aiModel": "string",
    "async": true
  }
  ```

#### 同步审查
- **URL:** `POST /api/review/sync`
- **参数:** 同上，`async: false`

#### 获取任务列表
- **URL:** `GET /api/review/tasks?page=1&size=10`
- **Headers:** `Authorization: {token}`

#### 获取任务详情
- **URL:** `GET /api/review/task/{taskId}`
- **Headers:** `Authorization: {token}`

---

## 常见问题

### 1. npm install失败

**解决方案：**
```bash
# 清除缓存
npm cache clean --force

# 删除node_modules
rm -rf node_modules package-lock.json

# 重新安装
npm install
```

### 2. 开发环境API请求失败

**检查：**
1. 后端服务是否启动（端口8000）
2. `.env` 文件配置是否正确
3. 浏览器控制台是否有CORS错误

### 3. 页面刷新后404

**原因：** Vue Router使用history模式

**解决：** 确保开发服务器配置了回退到index.html
（Vite已默认配置）

### 4. Element Plus组件样式丢失

**解决：**
```javascript
// main.js中确保导入了样式
import 'element-plus/dist/index.css'
```

### 5. Token过期怎么办

系统会自动检测Token过期，并跳转到登录页。
需要重新登录获取新Token。

### 6. 如何调试

**Vue DevTools：**
1. 安装Chrome/Firefox扩展
2. 打开浏览器开发者工具
3. 切换到Vue标签页

**网络请求调试：**
1. 打开浏览器开发者工具
2. 切换到Network标签页
3. 查看API请求和响应

---

## 浏览器兼容性

| 浏览器 | 版本要求 |
|--------|----------|
| Chrome | 最新版本 |
| Firefox | 最新版本 |
| Safari | 最新版本 |
| Edge | 最新版本 |

不支持IE浏览器。

---

## 性能优化建议

### 1. 代码分割

已使用路由懒加载：
```javascript
component: () => import('@/views/Page.vue')
```

### 2. 图片优化

- 使用WebP格式
- 压缩图片大小
- 使用懒加载

### 3. 打包优化

构建时会自动：
- Tree Shaking
- 代码压缩
- 资源优化

---

## 贡献指南

### 提交代码

1. Fork项目
2. 创建功能分支
3. 提交代码
4. 发起Pull Request

### 代码规范

- 使用ESLint检查代码
- 遵循Vue官方风格指南
- 组件命名使用PascalCase
- 文件名使用kebab-case

---

## 更新日志

### v1.0.0 (2025-11-08)

- ✨ 初始版本发布
- 🎨 完整的用户界面
- 🔒 JWT认证系统
- 📝 代码审查功能
- 📊 审查历史管理
- 👤 个人中心

---

## 联系与支持

- 📖 文档：查看完整README
- 🐛 Bug反馈：提交Issue
- 💡 功能建议：提交Feature Request
- 📧 邮箱：support@example.com

---

**祝您使用愉快！**
