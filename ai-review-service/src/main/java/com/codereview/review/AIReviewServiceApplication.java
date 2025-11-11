package com.codereview.review;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * AI代码审查服务启动类
 * @author CodeReview
 */
@SpringBootApplication(scanBasePackages = {"com.codereview.review", "com.codereview.common"})
@EnableDiscoveryClient
@MapperScan("com.codereview.review.mapper")
public class AIReviewServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AIReviewServiceApplication.class, args);
        System.out.println("====== AI Review Service Started Successfully ======");
    }
}
