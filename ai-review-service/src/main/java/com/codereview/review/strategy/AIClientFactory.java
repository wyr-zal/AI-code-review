package com.codereview.review.strategy;

import com.codereview.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI客户端工厂
 * 使用工厂模式管理不同的AI客户端策略
 * @author CodeReview
 */
@Component
public class AIClientFactory {

    @Resource
    private List<AIClientStrategy> strategies;

    private final Map<String, AIClientStrategy> strategyMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        for (AIClientStrategy strategy : strategies) {
            strategyMap.put(strategy.getModelName(), strategy);
        }
    }

    /**
     * 根据模型名称获取对应的策略
     */
    public AIClientStrategy getStrategy(String model) {
        // 支持的模型映射
        String strategyKey;
        if (model.contains("gpt")) {
            strategyKey = "gpt";
        } else if (model.contains("claude")) {
            strategyKey = "claude";
        } else {
            strategyKey = "gpt"; // 默认使用GPT
        }

        AIClientStrategy strategy = strategyMap.get(strategyKey);
        if (strategy == null) {
            throw new BusinessException("不支持的AI模型: " + model);
        }
        return strategy;
    }
}
