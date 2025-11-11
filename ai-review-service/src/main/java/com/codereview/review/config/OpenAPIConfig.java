package com.codereview.review.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Swagger/OpenAPI 配置
 * @author CodeReview
 */
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI代码审查服务 API")
                        .description("AI代码审查平台 - 代码审查服务接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CodeReview Team")
                                .email("support@codereview.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8002").description("本地环境"),
                        new Server().url("http://localhost:8000").description("网关环境")
                ));
    }
}
