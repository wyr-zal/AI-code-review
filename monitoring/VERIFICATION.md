# 监控系统验证指南

## 服务状态

### 核心监控服务 ✅
- **Prometheus**: http://localhost:9090 - 运行正常
- **Grafana**: http://localhost:3000 - 运行正常 (admin/admin)
- **AlertManager**: http://localhost:9093 - 运行正常
- **Node Exporter**: http://localhost:9100 - 运行正常

### Exporter服务状态
- **Redis Exporter**: http://localhost:9121 - 运行正常
- **RabbitMQ Exporter**: http://localhost:9419 - 运行正常
- **MySQL Exporter**: http://localhost:9104 - ⚠️ 需要启动虚拟机MySQL

## 验证步骤

### 1. 验证Prometheus

访问 http://localhost:9090

**检查目标状态：**
1. 点击 Status -> Targets
2. 查看以下目标应为UP状态：
   - prometheus (自监控)
   - node-exporter (系统指标)
   - redis-exporter (Redis指标)
   - rabbitmq-exporter (RabbitMQ指标)

**测试查询：**
在Expression输入框输入以下查询：
```promql
# 查看所有服务的UP状态
up

# 查看系统CPU使用率
100 - (avg by(instance)(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)

# 查看Redis连接数
redis_connected_clients
```

### 2. 验证Grafana

访问 http://localhost:3000

**登录信息：**
- 用户名: admin
- 密码: admin

**验证步骤：**
1. 登录后查看左侧菜单 -> Dashboards
2. 应该看到 "AI Code Review Platform" 仪表板
3. 点击进入仪表板，查看各项指标

**预配置的面板：**
- 服务状态监控
- 总QPS统计
- 请求速率趋势
- 平均响应时间
- JVM内存使用率
- CPU使用率
- JVM线程数

### 3. 验证AlertManager

访问 http://localhost:9093

**检查内容：**
- Alerts页面查看当前告警
- Status页面查看配置状态

### 4. 验证应用指标

**前提条件：**
确保微服务已启动（Gateway、User Service、AI Review Service）

**验证端点：**
```bash
# Gateway服务 (8000)
curl http://localhost:8000/actuator/prometheus

# User Service (8001)
curl http://localhost:8001/actuator/prometheus

# AI Review Service (8002)
curl http://localhost:8002/actuator/prometheus
```

应该返回Prometheus格式的指标数据。

## 常见问题

### MySQL Exporter连接失败

**错误信息：**
```
Error opening connection to database: dial tcp 192.168.100.128:3306: connect: connection refused
```

**解决方案：**
1. 启动虚拟机上的MySQL服务
2. 检查MySQL是否监听3306端口
3. 检查防火墙是否允许3306端口访问
4. 验证MySQL密码配置（.env.monitoring文件）

```bash
# 在虚拟机上检查MySQL状态
sudo systemctl status mysql

# 启动MySQL
sudo systemctl start mysql

# 检查端口监听
sudo netstat -tlnp | grep 3306
```

### RabbitMQ Exporter连接失败

**解决方案：**
1. 启动虚拟机上的RabbitMQ服务
2. 启用RabbitMQ管理插件
```bash
sudo rabbitmq-plugins enable rabbitmq_management
sudo systemctl restart rabbitmq-server
```

### 微服务指标不显示

**解决方案：**
1. 确保微服务已启动
2. 检查微服务的/actuator/prometheus端点
3. 查看Prometheus配置是否正确
4. 检查微服务的management配置

## 告警测试

### 测试服务宕机告警

1. 停止某个微服务
```bash
# 例如停止user-service
```

2. 等待1-2分钟，在Prometheus查看告警：
   - Status -> Rules
   - Alerts -> 查看active alerts

3. 在AlertManager查看告警通知

### 测试高QPS告警

可以通过压测工具（如Apache Bench）触发：
```bash
ab -n 1000 -c 50 http://localhost:8000/api/user/info
```

## 监控指标说明

### 应用指标
- `http_server_requests_seconds_count` - HTTP请求总数
- `http_server_requests_seconds_sum` - HTTP请求总耗时
- `jvm_memory_used_bytes` - JVM内存使用
- `jvm_threads_live_threads` - JVM活跃线程数
- `process_cpu_usage` - 进程CPU使用率

### 系统指标
- `node_cpu_seconds_total` - CPU时间
- `node_memory_MemAvailable_bytes` - 可用内存
- `node_disk_io_time_seconds_total` - 磁盘IO时间

### 数据库指标
- `mysql_up` - MySQL可用性
- `mysql_global_status_threads_connected` - MySQL连接数
- `redis_connected_clients` - Redis客户端连接数

## 下一步

1. ✅ 启动虚拟机上的MySQL、Redis、RabbitMQ
2. ✅ 启动所有微服务
3. ✅ 刷新Grafana仪表板查看完整指标
4. ✅ 配置AlertManager邮件通知
5. ✅ 根据实际情况调整告警阈值

## 停止监控服务

```bash
# 停止所有监控服务
docker compose -f docker-compose.monitoring.yml down

# 停止并删除数据卷
docker compose -f docker-compose.monitoring.yml down -v
```

## 查看日志

```bash
# 查看所有服务日志
docker compose -f docker-compose.monitoring.yml logs

# 查看特定服务日志
docker compose -f docker-compose.monitoring.yml logs prometheus
docker compose -f docker-compose.monitoring.yml logs grafana

# 实时查看日志
docker compose -f docker-compose.monitoring.yml logs -f
```

---

**监控系统验证完成时间**: 2025-11-19
**验证状态**: ✅ 核心功能正常，Exporter需要基础设施支持
