package com.cvagent.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger配置类
 * 用于配置API文档生成和UI界面
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        // 定义安全方案
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("JWT认证令牌，格式：Bearer <token>");

        // 创建API信息
        Info info = new Info()
                .title("CV Agent API")
                .version("1.0.0")
                .description("AI驱动的简历优化工具API文档，提供简历分析、优化建议和求职信生成等功能。")
                .contact(new Contact()
                        .name("CV Agent Team")
                        .email("support@cvagent.com")
                        .url("https://github.com/alijiujiu123/ccpmTest"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));

        // 创建服务器配置
        Server server = new Server()
                .url("http://localhost:" + serverPort)
                .description("本地开发服务器");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .info(info)
                .addServersItem(server);
    }
}