# Redis 工具类使用指南

> 基于 `StringRedisTemplate` 封装，支持字符串缓存、对象缓存和分布式锁

**文件位置**: `common/src/main/java/com/codereview/common/utils/RedisUtils.java`

---

## 功能概览

| 功能分类 | 方法数量 | 主要用途 |
|---------|---------|---------|
| 字符串操作 | 3 | 缓存字符串数据 |
| 对象操作 | 4 | 缓存 Java 对象（自动 JSON 序列化） |
| 通用操作 | 6 | 删除、过期时间、自增自减等 |
| 分布式锁 | 2 | 分布式环境下的并发控制 |

---

## 一、字符串操作

### 1.1 设置字符串缓存

```java
// 无过期时间
redisUtils.set("cache:key", "value");

// 带过期时间
redisUtils.set("cache:key", "value", 1, TimeUnit.HOURS);
redisUtils.set("session:token", tokenValue, 24, TimeUnit.HOURS);
```

### 1.2 获取字符串缓存

```java
String value = redisUtils.get("cache:key");
if (value != null) {
    // 处理缓存数据
}
```

---

## 二、对象操作（⭐ 推荐）

### 2.1 缓存单个对象

**适用场景**: 用户信息、订单详情、商品信息等

```java
// 示例1: 缓存用户信息
User user = userService.getUserById(userId);
redisUtils.setObject("user:" + userId, user, 2, TimeUnit.HOURS);

// 获取用户信息
User cachedUser = redisUtils.getObject("user:" + userId, User.class);
if (cachedUser != null) {
    return cachedUser; // 缓存命中
}

// 示例2: 缓存审查任务
ReviewTask task = reviewService.getTaskDetail(taskId);
redisUtils.setObject("task:" + taskId, task, 30, TimeUnit.MINUTES);
ReviewTask cachedTask = redisUtils.getObject("task:" + taskId, ReviewTask.class);
```

### 2.2 缓存 List 集合

**适用场景**: 用户列表、商品列表、文章列表等

```java
// 缓存用户列表
List<User> users = userService.getUsersByDepartment(deptId);
redisUtils.setObject("users:dept:" + deptId, users, 1, TimeUnit.HOURS);

// 获取列表（使用 TypeReference）
List<User> cachedUsers = redisUtils.getObject(
    "users:dept:" + deptId,
    new TypeReference<List<User>>(){}
);

// 示例2: 缓存审查历史
List<ReviewTask> tasks = reviewService.getUserTasks(userId);
redisUtils.setObject("tasks:user:" + userId, tasks, 30, TimeUnit.MINUTES);
List<ReviewTask> cachedTasks = redisUtils.getObject(
    "tasks:user:" + userId,
    new TypeReference<List<ReviewTask>>(){}
);
```

### 2.3 缓存 Map

**适用场景**: 配置信息、统计数据、权限映射等

```java
// 缓存用户配置
Map<String, String> userConfig = new HashMap<>();
userConfig.put("theme", "dark");
userConfig.put("language", "zh-CN");
userConfig.put("timezone", "Asia/Shanghai");
redisUtils.setObject("config:user:" + userId, userConfig, 24, TimeUnit.HOURS);

// 获取配置
Map<String, String> config = redisUtils.getObject(
    "config:user:" + userId,
    new TypeReference<Map<String, String>>(){}
);

// 示例2: 缓存统计数据
Map<String, Integer> statistics = new HashMap<>();
statistics.put("totalReviews", 100);
statistics.put("pendingReviews", 20);
statistics.put("completedReviews", 80);
redisUtils.setObject("stats:reviews", statistics, 5, TimeUnit.MINUTES);
```

### 2.4 缓存嵌套对象

**适用场景**: 复杂的业务对象

```java
// 缓存订单详情（包含商品列表）
Order order = orderService.getOrderWithItems(orderId);
// order.getItems() 返回 List<OrderItem>
redisUtils.setObject("order:" + orderId, order, 1, TimeUnit.HOURS);

// 获取订单
Order cachedOrder = redisUtils.getObject("order:" + orderId, Order.class);
List<OrderItem> items = cachedOrder.getItems(); // 自动反序列化嵌套对象
```

---

## 三、通用操作

### 3.1 删除缓存

```java
// 删除单个缓存
Boolean deleted = redisUtils.delete("cache:key");

// 更新数据时删除缓存
public void updateUser(User user) {
    userMapper.updateById(user);
    redisUtils.delete("user:" + user.getId()); // 删除旧缓存
}
```

### 3.2 检查缓存是否存在

```java
Boolean exists = redisUtils.hasKey("cache:key");
if (exists) {
    // 缓存存在
}
```

### 3.3 设置过期时间

```java
// 为已存在的缓存设置过期时间
redisUtils.expire("cache:key", 1, TimeUnit.HOURS);
```

### 3.4 获取过期时间

```java
// 返回剩余秒数，-1表示永久，-2表示不存在
Long ttl = redisUtils.getExpire("cache:key");
if (ttl > 0) {
    log.info("缓存还有 {} 秒过期", ttl);
}
```

### 3.5 计数器操作

```java
// 自增（用于统计、限流等）
Long views = redisUtils.increment("article:views:" + articleId, 1);
Long loginAttempts = redisUtils.increment("login:attempts:" + username, 1);

// 自减
Long stock = redisUtils.decrement("product:stock:" + productId, 1);
```

---

## 四、分布式锁

### 4.1 基本用法

```java
String lockKey = "lock:order:" + orderId;
String lockValue = UUID.randomUUID().toString(); // 唯一标识

// 尝试获取锁，超时时间5分钟
if (redisUtils.tryLock(lockKey, lockValue, 5, TimeUnit.MINUTES)) {
    try {
        // 执行业务逻辑（保证同一时间只有一个线程执行）
        processOrder(orderId);
    } finally {
        // 释放锁（安全释放，检查 value 是否匹配）
        redisUtils.unlock(lockKey, lockValue);
    }
} else {
    // 获取锁失败，已有其他线程在处理
    log.warn("订单正在处理中: {}", orderId);
}
```

### 4.2 防止重复提交

```java
@PostMapping("/submit")
public Result submitReview(@RequestBody CodeReviewRequestDTO dto) {
    String lockKey = "review:submit:" + userId;
    String lockValue = UUID.randomUUID().toString();

    if (!redisUtils.tryLock(lockKey, lockValue, 10, TimeUnit.SECONDS)) {
        return Result.error("请勿重复提交");
    }

    try {
        // 提交审查任务
        Long taskId = reviewService.submitReviewTask(dto, userId);
        return Result.success(taskId);
    } finally {
        redisUtils.unlock(lockKey, lockValue);
    }
}
```

### 4.3 限流场景

```java
public boolean checkRateLimit(String userId) {
    String key = "rate:limit:api:" + userId;
    Long count = redisUtils.increment(key, 1);

    if (count == 1) {
        // 第一次访问，设置过期时间
        redisUtils.expire(key, 1, TimeUnit.MINUTES);
    }

    // 限制每分钟最多100次请求
    return count <= 100;
}
```

---

## 五、实战案例

### 案例1: 缓存审查结果

```java
@Service
public class ReviewServiceImpl implements ReviewService {

    @Resource
    private RedisUtils redisUtils;

    @Override
    public ReviewTask getTaskDetail(Long taskId) {
        // 1. 先查缓存
        String cacheKey = "review:task:" + taskId;
        ReviewTask cached = redisUtils.getObject(cacheKey, ReviewTask.class);
        if (cached != null) {
            log.info("缓存命中: taskId={}", taskId);
            return cached;
        }

        // 2. 缓存未命中，查数据库
        ReviewTask task = reviewTaskMapper.selectById(taskId);

        // 3. 写入缓存（已完成的任务缓存时间更长）
        if (task != null) {
            long ttl = task.getStatus() == 2 ? 24 : 1; // 已完成24小时，其他1小时
            redisUtils.setObject(cacheKey, task, ttl, TimeUnit.HOURS);
        }

        return task;
    }
}
```

### 案例2: 用户会话管理

```java
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private RedisUtils redisUtils;

    @Override
    public Map<String, Object> login(String username, String password) {
        // 验证用户
        User user = authenticateUser(username, password);

        // 生成 token
        String token = JwtUtils.createToken(user.getId().toString());

        // 缓存用户信息（使用 token 作为 key）
        String cacheKey = "session:" + token;
        redisUtils.setObject(cacheKey, user, 24, TimeUnit.HOURS);

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    @Override
    public User getCurrentUser(String token) {
        String cacheKey = "session:" + token;
        return redisUtils.getObject(cacheKey, User.class);
    }
}
```

### 案例3: 热点数据缓存

```java
@Service
public class StatisticsService {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private ReviewTaskMapper reviewTaskMapper;

    /**
     * 获取用户统计信息（5分钟缓存）
     */
    public Map<String, Integer> getUserStatistics(Long userId) {
        String cacheKey = "stats:user:" + userId;

        // 尝试从缓存获取
        Map<String, Integer> stats = redisUtils.getObject(
            cacheKey,
            new TypeReference<Map<String, Integer>>(){}
        );

        if (stats != null) {
            return stats;
        }

        // 查询数据库
        stats = new HashMap<>();
        stats.put("total", reviewTaskMapper.countByUserId(userId));
        stats.put("pending", reviewTaskMapper.countByUserIdAndStatus(userId, 0));
        stats.put("completed", reviewTaskMapper.countByUserIdAndStatus(userId, 2));

        // 写入缓存
        redisUtils.setObject(cacheKey, stats, 5, TimeUnit.MINUTES);

        return stats;
    }
}
```

---

## 六、最佳实践

### 6.1 缓存 Key 命名规范

```java
// ✅ 好的命名
"user:123"                    // 用户信息
"user:profile:123"            // 用户资料
"review:task:456"             // 审查任务
"cache:articles:list"         // 文章列表
"lock:order:789"              // 订单锁
"rate:limit:api:user:123"     // API 限流

// ❌ 不好的命名
"u123"                        // 不清晰
"data"                        // 太笼统
"temp_cache_123"              // 不规范
```

### 6.2 过期时间设置建议

```java
// 热点数据（短期）
redisUtils.setObject(key, data, 5, TimeUnit.MINUTES);

// 常规缓存（中期）
redisUtils.setObject(key, data, 1, TimeUnit.HOURS);

// 稳定数据（长期）
redisUtils.setObject(key, data, 24, TimeUnit.HOURS);

// 会话数据
redisUtils.setObject(key, data, 30, TimeUnit.DAYS);
```

### 6.3 异常处理

```java
// RedisUtils 内部已做异常处理，外部调用时注意空值判断
User user = redisUtils.getObject("user:123", User.class);
if (user == null) {
    // 缓存不存在或解析失败，查数据库
    user = userMapper.selectById(123L);
}
```

### 6.4 避免缓存穿透

```java
public User getUser(Long userId) {
    String cacheKey = "user:" + userId;

    // 查缓存
    User user = redisUtils.getObject(cacheKey, User.class);
    if (user != null) {
        return user;
    }

    // 查数据库
    user = userMapper.selectById(userId);

    // 即使数据不存在，也缓存空对象，防止穿透
    if (user == null) {
        user = new User(); // 空对象
        redisUtils.setObject(cacheKey, user, 5, TimeUnit.MINUTES);
    } else {
        redisUtils.setObject(cacheKey, user, 1, TimeUnit.HOURS);
    }

    return user;
}
```

---

## 七、技术细节

### 7.1 序列化机制

- **依赖**: FastJSON 2 (`com.alibaba.fastjson2`)
- **序列化**: `JSON.toJSONString(object)`
- **反序列化**: `JSON.parseObject(json, Class)` / `JSON.parseObject(json, TypeReference)`
- **存储格式**: Redis 中存储为 JSON 字符串，可直接查看

### 7.2 类型支持

| Java 类型 | 支持情况 | 使用方法 |
|----------|---------|---------|
| 基本对象 | ✅ | `getObject(key, User.class)` |
| List | ✅ | `getObject(key, new TypeReference<List<User>>(){})` |
| Map | ✅ | `getObject(key, new TypeReference<Map<K,V>>(){})` |
| Set | ✅ | `getObject(key, new TypeReference<Set<User>>(){})` |
| 嵌套对象 | ✅ | FastJSON 自动处理 |

### 7.3 优势对比

| 特性 | RedisTemplate | StringRedisTemplate + JSON |
|-----|--------------|---------------------------|
| 配置复杂度 | 需要配置序列化器 | 无需配置 ✅ |
| 可读性 | 二进制数据 | JSON 字符串 ✅ |
| 调试难度 | 难以直接查看 | 易于调试 ✅ |
| 性能 | 稍快 | 优秀 ✅ |
| 跨语言 | 困难 | 容易（JSON 通用）✅ |

---

## 八、常见问题

### Q1: 为什么选择 StringRedisTemplate？

**A**:
1. 无需配置复杂的序列化器
2. Redis 中存储的是可读的 JSON，便于调试
3. FastJSON 性能优秀，序列化速度快
4. 跨语言兼容性好（Python、Go 也能读取）

### Q2: 如何处理循环引用？

**A**: FastJSON 默认支持循环引用检测，但建议设计 DTO 避免循环引用。

### Q3: 缓存和数据库数据不一致怎么办？

**A**: 采用缓存更新策略：
- **先更新数据库，再删除缓存**（推荐）
- 或使用 Canal 等工具监听 binlog 自动更新缓存

```java
@Transactional
public void updateUser(User user) {
    // 1. 更新数据库
    userMapper.updateById(user);

    // 2. 删除缓存
    redisUtils.delete("user:" + user.getId());
}
```

### Q4: 如何避免缓存雪崩？

**A**: 设置随机过期时间：

```java
// 基础时间1小时 + 随机0-10分钟
long baseTime = 1 * 60 * 60; // 1小时
long randomTime = new Random().nextInt(10 * 60); // 0-10分钟
redisUtils.setObject(key, data, baseTime + randomTime, TimeUnit.SECONDS);
```

---

## 九、版本记录

| 版本 | 日期 | 变更内容 |
|-----|------|---------|
| v2.0 | 2025-11-11 | 重构为 StringRedisTemplate，新增对象缓存方法 |
| v1.0 | 2025-11-09 | 初始版本，基于 RedisTemplate |

---

**最后更新**: 2025-11-11
**作者**: AI Code Review Team
