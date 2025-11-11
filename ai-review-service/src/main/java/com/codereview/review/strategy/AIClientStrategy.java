package com.codereview.review.strategy;

/**
 * AI客户端策略接口
 * 使用策略模式支持多种AI模型
 * @author CodeReview
 */
public interface AIClientStrategy {

    /**
     * 调用AI进行代码审查
     * @param code 代码内容
     * @param language 编程语言
     * @return 审查结果
     */
    String reviewCode(String code, String language);

    /**
     * 获取策略支持的模型名称
     * @return 模型名称
     */
    String getModelName();
}
