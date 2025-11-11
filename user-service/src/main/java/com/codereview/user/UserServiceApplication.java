package com.codereview.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户服务启动类
 * @author CodeReview
 */
@SpringBootApplication(scanBasePackages = {"com.codereview.user", "com.codereview.common"})
@EnableDiscoveryClient
@MapperScan("com.codereview.user.mapper")
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
        System.out.println("====== User Service Started Successfully ======");
    }
}
