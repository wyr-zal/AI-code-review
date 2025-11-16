package com.codereview.common.config;

import com.codereview.common.interceptor.UserContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类，注册用户上下文拦截器
 * @author CodeReview
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册用户上下文拦截器，拦截所有API请求
        // 修改路径模式以匹配实际API路径，之前只匹配/api/**，现在匹配更广泛的路径
        registry.addInterceptor(new UserContextInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/api/user/login", "/api/user/register", "/user/login", "/user/register");
    }
}