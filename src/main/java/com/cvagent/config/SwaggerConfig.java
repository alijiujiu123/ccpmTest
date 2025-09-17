package com.cvagent.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
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

        // 定义API分组标签
        Tag[] tags = new Tag[] {
                new Tag().name("认证管理").description("用户登录、注册和认证相关接口"),
                new Tag().name("简历管理").description("简历的创建、查询、更新和删除相关接口"),
                new Tag().name("增强简历管理").description("增强简历的生成、导出、预览等功能接口"),
                new Tag().name("项目管理").description("项目的创建、查询、更新和删除相关接口"),
                new Tag().name("求职信管理").description("求职信的生成、管理、优化和导出相关接口"),
                new Tag().name("文件管理").description("文件上传、下载、预览和处理相关接口"),
                new Tag().name("优化规则管理").description("简历优化规则的CRUD操作和管理功能接口"),
                new Tag().name("AI服务").description("AI相关功能的服务接口")
        };

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .info(info)
                .addServersItem(server)
                .addTagsItem(tags[0])
                .addTagsItem(tags[1])
                .addTagsItem(tags[2])
                .addTagsItem(tags[3])
                .addTagsItem(tags[4])
                .addTagsItem(tags[5])
                .addTagsItem(tags[6])
                .addTagsItem(tags[7]);
    }
}