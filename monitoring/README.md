# AI代码审查平台监控配置

本目录包含了AI代码审查平台的Prometheus和Grafana监控配置。

## 目录结构

```
monitoring/
├── prometheus.yml              # Prometheus配置文件
├── prometheus-rules.yml        # Prometheus告警规则
├── alertmanager.yml            # Alertmanager配置
├── grafana-dashboard.json      # Grafana仪表板配置
├── grafana-datasources/        # Grafana数据源配置
│   └── prometheus.yml
├── grafana-dashboards/         # Grafana仪表板自动加载配置
│   ├── dashboards.yml
│   └── ai-code-review-dashboard.json
└── README.md                   # 本文件
```

## 快速启动

1. 启动监控服务：
```bash
docker-compose -f docker-compose.monitoring.yml up -d
```

2. 访问监控界面：
   - Prometheus: http://localhost:9090
   - Grafana: http://localhost:3000 (admin/admin)
   - AlertManager: http://localhost:9093

## 监控指标

### 应用指标
- HTTP请求数量和速率
- 响应时间
- 错误率
- JVM内存使用情况
- CPU使用率
- 线程数

### 基础设施指标
- MySQL连接状态和性能
- Redis连接状态
- RabbitMQ队列状态
- 系统资源使用情况

## 告警规则

系统配置了以下告警规则：

- 服务不可用（严重）
- QPS过高（警告）
- 响应时间过长（警告）
- 内存使用率过高（警告）
- CPU使用率过高（警告）
- 错误率过高（严重）
- 数据库连接失败（严重）
- Redis连接失败（警告）
- RabbitMQ连接失败（警告）

## Grafana仪表板

预配置的仪表板包含以下面板：

1. **服务状态** - 显示各服务的运行状态
2. **总QPS** - 显示系统总请求速率
3. **请求速率** - 各服务的请求速率趋势
4. **平均响应时间** - 各服务的响应时间趋势
5. **JVM内存使用率** - Java服务的内存使用情况
6. **CPU使用率** - 系统CPU使用情况
7. **JVM线程数** - Java服务的线程数量

## 配置说明

### Prometheus配置
- 抓取间隔：服务指标10秒，基础设施指标15秒
- 数据保留时间：200小时
- 支持热重载配置

### Grafana配置
- 自动加载仪表板和数据源配置
- 默认管理员密码：admin
- 刷新间隔：5秒

### 告警配置
- AlertManager负责告警路由和通知
- 支持邮件、Webhook等多种通知方式
- 告警规则按严重程度分级

## 自定义配置

### 添加新的监控指标
1. 在应用中添加Micrometer指标
2. 更新Prometheus配置文件
3. 在Grafana中创建新的仪表板面板

### 修改告警规则
编辑 `prometheus-rules.yml` 文件，然后重新加载Prometheus配置：
```bash
curl -X POST http://localhost:9090/-/reload
```

### 自定义Grafana仪表板
1. 登录Grafana界面
2. 创建或编辑仪表板
3. 导出配置到 `grafana-dashboards` 目录

## 故障排除

### 服务无法抓取指标
1. 检查服务是否正常运行
2. 验证 `/actuator/prometheus` 端点是否可访问
3. 检查网络连接和防火墙设置

### Grafana无法连接Prometheus
1. 确认Prometheus服务正常运行
2. 检查数据源配置是否正确
3. 验证容器网络连接

### 告警不生效
1. 检查AlertManager配置
2. 验证告警规则语法
3. 确认通知渠道配置正确