package com.cvagent.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.cvagent.repository")
public class MongoConfig {

    // MongoDB配置类
    // 启用审计功能（自动设置createdAt和updatedAt）
    // 启用Repository扫描

    // 可以在这里添加自定义的MongoDB配置
    // 例如连接池设置、索引配置等
}