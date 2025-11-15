package com.codereview.user.config;

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
        registry.addInterceptor(new UserContextInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/user/login", "/api/user/register"); // 排除登录和注册接口
    }
}