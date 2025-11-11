# AI代码审查平台 - 前端部署指南

## 目录

- [环境要求](#环境要求)
- [开发环境部署](#开发环境部署)
- [生产环境部署](#生产环境部署)
  - [方式一：传统部署](#方式一传统部署)
  - [方式二：Docker部署](#方式二docker部署)
- [配置说明](#配置说明)
- [常见问题](#常见问题)

---

## 环境要求

### 开发环境
- Node.js 16.x 或更高版本
- npm 7.x 或更高版本（或 yarn 1.22+）

### 生产环境
- Nginx 1.18+ 或更高版本
- Docker（可选，用于容器化部署）

---

## 开发环境部署

### 1. 安装依赖

```bash
cd frontend
npm install
```

### 2. 配置环境变量

编辑 `.env` 文件，配置后端API地址：

```env
VITE_API_BASE_URL=http://localhost:8000
VITE_APP_TITLE=AI代码审查平台
```

### 3. 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:3000 查看应用。

### 4. 开发环境特性

- 热重载（Hot Module Replacement）
- API代理配置（自动转发 `/api` 请求到后端）
- Source Map支持
- 详细的错误提示

---

## 生产环境部署

### 方式一：传统部署（Nginx）

#### 1. 构建项目

```bash
cd frontend
npm run build
```

构建完成后，会在 `dist/` 目录生成静态文件。

#### 2. 安装Nginx

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install nginx
```

**CentOS/RHEL:**
```bash
sudo yum install nginx
```

**Windows:**
从 [Nginx官网](http://nginx.org/en/download.html) 下载Windows版本。

#### 3. 配置Nginx

将项目中的 `nginx.conf` 复制到Nginx配置目录：

**Linux:**
```bash
sudo cp nginx.conf /etc/nginx/conf.d/ai-code-review.conf
```

**Windows:**
将 `nginx.conf` 内容复制到 `nginx安装目录/conf/nginx.conf` 中。

#### 4. 复制静态文件

**Linux:**
```bash
sudo mkdir -p /usr/share/nginx/html/ai-code-review
sudo cp -r dist/* /usr/share/nginx/html/ai-code-review/
```

**Windows:**
将 `dist` 目录下的所有文件复制到 `nginx安装目录/html/` 目录。

#### 5. 修改nginx配置中的路径

编辑nginx配置文件，根据实际情况修改：

```nginx
server {
    listen 80;
    server_name your-domain.com;  # 改为你的域名

    root /usr/share/nginx/html/ai-code-review;  # 改为实际路径

    location /api/ {
        proxy_pass http://localhost:8000/;  # 改为后端实际地址
    }
}
```

#### 6. 启动Nginx

**Linux:**
```bash
# 测试配置文件
sudo nginx -t

# 启动Nginx
sudo systemctl start nginx

# 设置开机自启
sudo systemctl enable nginx

# 重新加载配置
sudo systemctl reload nginx
```

**Windows:**
```bash
# 进入nginx目录
cd C:\nginx

# 启动nginx
start nginx

# 重新加载配置
nginx -s reload
```

#### 7. 访问应用

打开浏览器访问：http://your-domain.com 或 http://localhost

---

### 方式二：Docker部署

#### 1. 构建项目

```bash
cd frontend
npm run build
```

#### 2. 构建Docker镜像

```bash
docker build -t ai-code-review-frontend:latest .
```

#### 3. 运行Docker容器

```bash
docker run -d \
  --name ai-code-review-frontend \
  -p 80:80 \
  ai-code-review-frontend:latest
```

#### 4. 使用Docker Compose（推荐）

创建 `docker-compose.yml` 文件：

```yaml
version: '3.8'

services:
  frontend:
    build: .
    container_name: ai-code-review-frontend
    ports:
      - "80:80"
    restart: unless-stopped
    networks:
      - ai-review-network

networks:
  ai-review-network:
    driver: bridge
```

启动服务：

```bash
docker-compose up -d
```

#### 5. 查看日志

```bash
# 查看容器日志
docker logs ai-code-review-frontend

# 实时查看日志
docker logs -f ai-code-review-frontend
```

---

## 配置说明

### 环境变量配置

项目支持以下环境变量：

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| VITE_API_BASE_URL | 后端API基础URL | http://localhost:8000 |
| VITE_APP_TITLE | 应用标题 | AI代码审查平台 |

### Nginx配置说明

#### 1. 基本配置

```nginx
server {
    listen 80;                      # 监听端口
    server_name localhost;          # 服务器域名
    root /usr/share/nginx/html;     # 静态文件根目录
    index index.html;               # 默认首页
}
```

#### 2. 路由配置（Vue Router History模式）

```nginx
location / {
    try_files $uri $uri/ /index.html;  # 所有路由都返回index.html
}
```

#### 3. API代理配置

```nginx
location /api/ {
    proxy_pass http://localhost:8000/;  # 后端网关地址
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
```

#### 4. 静态资源缓存

```nginx
location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
    expires 30d;                        # 缓存30天
    add_header Cache-Control "public";
}
```

#### 5. Gzip压缩

```nginx
gzip on;
gzip_vary on;
gzip_min_length 1k;
gzip_comp_level 6;
gzip_types text/plain text/css application/json application/javascript;
```

### HTTPS配置（可选）

如果需要HTTPS，可以配置SSL证书：

```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    # 其他配置...
}

# HTTP重定向到HTTPS
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

---

## 常见问题

### 1. 页面刷新后404错误

**原因：** Vue Router使用history模式，nginx需要配置所有路由都返回index.html

**解决：** 确保nginx配置了：
```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

### 2. API请求跨域错误

**原因：** 前端直接访问后端API时的跨域问题

**解决方案：**
- 使用nginx代理（推荐）
- 后端开启CORS支持

### 3. 静态资源加载失败

**原因：** 路径配置错误

**解决：**
- 检查nginx的root路径是否正确
- 确认dist文件是否完整复制

### 4. 构建失败

**常见错误：**
```bash
# Node版本过低
错误: Node.js version too old

# 解决：升级Node.js
nvm install 16
nvm use 16
```

```bash
# 依赖安装失败
错误: npm install失败

# 解决：清除缓存重新安装
rm -rf node_modules package-lock.json
npm cache clean --force
npm install
```

### 5. Docker容器无法访问

**检查步骤：**

```bash
# 1. 查看容器是否运行
docker ps

# 2. 查看容器日志
docker logs ai-code-review-frontend

# 3. 进入容器检查
docker exec -it ai-code-review-frontend sh

# 4. 检查nginx配置
docker exec ai-code-review-frontend nginx -t
```

### 6. 端口被占用

**Windows:**
```bash
# 查看端口占用
netstat -ano | findstr :80

# 结束进程
taskkill /PID 进程号 /F
```

**Linux:**
```bash
# 查看端口占用
sudo lsof -i :80

# 结束进程
sudo kill -9 PID
```

---

## 性能优化建议

### 1. 启用Gzip压缩
已在nginx配置中启用，可将文件大小减少70%以上。

### 2. 配置缓存策略
- HTML文件：不缓存或短期缓存
- JS/CSS/图片：长期缓存（30天）

### 3. 使用CDN
将静态资源部署到CDN，加快访问速度。

### 4. 开启HTTP/2
如果使用HTTPS，建议开启HTTP/2：
```nginx
listen 443 ssl http2;
```

### 5. 图片优化
- 使用WebP格式
- 压缩图片
- 使用懒加载

---

## 监控和日志

### Nginx访问日志

```bash
# 查看访问日志
tail -f /var/log/nginx/access.log

# 查看错误日志
tail -f /var/log/nginx/error.log
```

### 日志格式配置

```nginx
log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                '$status $body_bytes_sent "$http_referer" '
                '"$http_user_agent" "$http_x_forwarded_for"';

access_log /var/log/nginx/access.log main;
```

---

## 安全配置

### 1. 隐藏Nginx版本号

```nginx
http {
    server_tokens off;
}
```

### 2. 限制请求大小

```nginx
client_max_body_size 10M;
```

### 3. 防止点击劫持

```nginx
add_header X-Frame-Options "SAMEORIGIN";
```

### 4. XSS防护

```nginx
add_header X-XSS-Protection "1; mode=block";
add_header X-Content-Type-Options "nosniff";
```

---

## 更新部署

### 方式一：直接替换

```bash
# 1. 构建新版本
npm run build

# 2. 备份旧版本
sudo mv /usr/share/nginx/html /usr/share/nginx/html.bak

# 3. 部署新版本
sudo cp -r dist/* /usr/share/nginx/html/

# 4. 重启nginx
sudo systemctl reload nginx
```

### 方式二：蓝绿部署

```bash
# 部署新版本到新目录
sudo cp -r dist/* /usr/share/nginx/html-new/

# 修改nginx配置指向新目录
# 重载nginx
sudo systemctl reload nginx

# 确认无问题后删除旧版本
sudo rm -rf /usr/share/nginx/html-old/
```

---

## 联系支持

如有问题，请联系：
- 项目文档：查看 `README.md`
- Issue跟踪：GitHub Issues
- 邮箱：support@example.com

---

**部署成功后，请访问应用并使用测试账号登录：**
- 管理员：admin / 123456
- 普通用户：testuser / 123456
