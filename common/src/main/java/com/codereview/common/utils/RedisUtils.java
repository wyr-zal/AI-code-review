package com.codereview.common.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类 - 基于 StringRedisTemplate
 * @author CodeReview
 */
@Slf4j
@Component
public class RedisUtils {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // ==================== 字符串操作 ====================

    /**
     * 设置缓存
     */
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存（带过期时间）
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取缓存
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    // ==================== 对象操作 ====================

    /**
     * 设置对象缓存（自动 JSON 序列化）
     * @param key 缓存键
     * @param value 对象值
     */
    public <T> void setObject(String key, T value) {
        try {
            String jsonValue = JSON.toJSONString(value);
            stringRedisTemplate.opsForValue().set(key, jsonValue);
        } catch (Exception e) {
            log.error("设置对象缓存失败: key={}", key, e);
            throw new RuntimeException("设置对象缓存失败", e);
        }
    }

    /**
     * 设置对象缓存（带过期时间，自动 JSON 序列化）
     * @param key 缓存键
     * @param value 对象值
     * @param timeout 超时时间
     * @param unit 时间单位
     */
    public <T> void setObject(String key, T value, long timeout, TimeUnit unit) {
        try {
            String jsonValue = JSON.toJSONString(value);
            stringRedisTemplate.opsForValue().set(key, jsonValue, timeout, unit);
        } catch (Exception e) {
            log.error("设置对象缓存失败: key={}, timeout={}", key, timeout, e);
            throw new RuntimeException("设置对象缓存失败", e);
        }
    }

    /**
     * 获取对象缓存（自动 JSON 反序列化）
     * @param key 缓存键
     * @param clazz 对象类型
     * @return 对象实例，如果不存在返回 null
     */
    public <T> T getObject(String key, Class<T> clazz) {
        try {
            String jsonValue = stringRedisTemplate.opsForValue().get(key);
            if (jsonValue == null) {
                return null;
            }
            return JSON.parseObject(jsonValue, clazz);
        } catch (Exception e) {
            log.error("获取对象缓存失败: key={}, class={}", key, clazz.getName(), e);
            return null;
        }
    }

    /**
     * 获取对象缓存（支持泛型，自动 JSON 反序列化）
     * 适用于 List、Map 等复杂类型
     *
     * 使用示例：
     * List<User> users = redisUtils.getObject(key, new TypeReference<List<User>>(){});
     * Map<String, Integer> map = redisUtils.getObject(key, new TypeReference<Map<String, Integer>>(){});
     *
     * @param key 缓存键
     * @param typeReference 类型引用
     * @return 对象实例，如果不存在返回 null
     */
    public <T> T getObject(String key, TypeReference<T> typeReference) {
        try {
            String jsonValue = stringRedisTemplate.opsForValue().get(key);
            if (jsonValue == null) {
                return null;
            }
            return JSON.parseObject(jsonValue, typeReference);
        } catch (Exception e) {
            log.error("获取对象缓存失败: key={}, type={}", key, typeReference.getType(), e);
            return null;
        }
    }

    // ==================== 通用操作 ====================

    /**
     * 删除缓存
     */
    public Boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }

    /**
     * 判断缓存是否存在
     */
    public Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return stringRedisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取过期时间（秒）
     */
    public Long getExpire(String key) {
        return stringRedisTemplate.getExpire(key);
    }

    /**
     * 自增
     */
    public Long increment(String key, long delta) {
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 自减
     */
    public Long decrement(String key, long delta) {
        return stringRedisTemplate.opsForValue().decrement(key, delta);
    }

    // ==================== 分布式锁 ====================

    /**
     * 尝试获取分布式锁
     */
    public Boolean tryLock(String key, String value, long timeout, TimeUnit unit) {
        return stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }

    /**
     * 释放分布式锁（安全释放，检查value是否匹配）
     */
    public void unlock(String key, String value) {
        String currentValue = stringRedisTemplate.opsForValue().get(key);
        if (value != null && value.equals(currentValue)) {
            stringRedisTemplate.delete(key);
        }
    }
}
