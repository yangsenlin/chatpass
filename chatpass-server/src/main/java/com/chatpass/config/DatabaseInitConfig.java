package com.chatpass.config;

import org.springframework.context.annotation.Configuration;

/**
 * 数据库初始化配置
 * 
 * Flyway 自动配置由 Spring Boot 管理
 * 参见 application.yml 中的 spring.flyway 配置
 */
@Configuration
public class DatabaseInitConfig {
    // Flyway 配置由 Spring Boot 自动完成
    // 参见 application.yml:
    // spring.flyway.enabled=true
    // spring.flyway.locations=classpath:db/migration
    // spring.flyway.baseline-on-migrate=true
}