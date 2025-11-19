package com.codereview.common.utils;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * 监控指标工具类
 * @author CodeReview
 */
@Component
public class MetricsUtils {

    private final MeterRegistry meterRegistry;

    @Autowired
    public MetricsUtils(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 计数器
     */
    public Counter counter(String name, String... tags) {
        return Counter.builder(name)
                .tags(tags)
                .register(meterRegistry);
    }

    /**
     * 计时器
     */
    public Timer timer(String name, String... tags) {
        return Timer.builder(name)
                .tags(tags)
                .register(meterRegistry);
    }

    /**
     * 记录方法执行时间（无返回值）
     */
    public void recordTimer(String name, Runnable action, String... tags) {
        Timer timer = timer(name, tags);
        timer.record(action);
    }

    /**
     * 记录方法执行时间（有返回值）
     */
    public <T> T recordTimer(String name, Callable<T> action, String... tags) throws Exception {
        Timer timer = timer(name, tags);
        return timer.recordCallable(action);
    }

    /**
     * 增加计数
     */
    public void incrementCounter(String name, String... tags) {
        counter(name, tags).increment();
    }

    /**
     * 增加计数（指定数量）
     */
    public void incrementCounter(String name, double amount, String... tags) {
        counter(name, tags).increment(amount);
    }

    /**
     * 记录API请求
     */
    public void recordApiRequest(String endpoint, String method, String status) {
        incrementCounter("api.requests", "endpoint", endpoint, "method", method, "status", status);
    }

    /**
     * 注册一个 Gauge 指标（用于监控动态值）
     * @param name 指标名称
     * @param valueHolder 值持有者（如 AtomicInteger、AtomicLong）
     * @param tags 标签
     */
    public void registerGauge(String name, AtomicInteger valueHolder, String... tags) {
        Gauge.builder(name, valueHolder, AtomicInteger::get)
                .tags(tags)
                .register(meterRegistry);
    }

    /**
     * 注册一个 Gauge 指标（用于监控动态值）
     * @param name 指标名称
     * @param valueHolder 值持有者
     * @param tags 标签
     */
    public void registerGauge(String name, AtomicLong valueHolder, String... tags) {
        Gauge.builder(name, valueHolder, AtomicLong::get)
                .tags(tags)
                .register(meterRegistry);
    }

    /**
     * 注册一个 Gauge 指标（使用 Supplier）
     * @param name 指标名称
     * @param valueSupplier 值提供者
     * @param tags 标签
     */
    public void registerGauge(String name, Supplier<Number> valueSupplier, String... tags) {
        Gauge.builder(name, valueSupplier, s -> s.get().doubleValue())
                .tags(tags)
                .register(meterRegistry);
    }

    /**
     * 记录代码审查任务指标
     */
    public void recordReviewTask(String status) {
        incrementCounter("review.task.total", "status", status);
    }

    /**
     * 记录AI API调用指标
     */
    public void recordAiApiCall(String model, boolean success) {
        incrementCounter("ai.api.calls",
                "model", model,
                "result", success ? "success" : "failure");
    }
}