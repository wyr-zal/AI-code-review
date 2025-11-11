package com.codereview.common.constant;

/**
 * Redis键常量
 * @author CodeReview
 */
public class RedisConstants {

    /**
     * 用户token键前缀
     */
    public static final String USER_TOKEN_KEY = "user:token:";

    /**
     * 代码审查任务键前缀
     */
    public static final String REVIEW_TASK_KEY = "review:task:";

    /**
     * AI审查结果缓存键前缀
     */
    public static final String AI_REVIEW_CACHE_KEY = "ai:review:cache:";

    /**
     * 限流键前缀
     */
    public static final String RATE_LIMIT_KEY = "rate:limit:";

    /**
     * 分布式锁键前缀
     */
    public static final String DISTRIBUTED_LOCK_KEY = "lock:";

    /**
     * token过期时间（小时）
     */
    public static final Long TOKEN_EXPIRE_TIME = 24L;

    /**
     * 审查结果缓存时间（小时）
     */
    public static final Long REVIEW_CACHE_EXPIRE_TIME = 72L;
}
