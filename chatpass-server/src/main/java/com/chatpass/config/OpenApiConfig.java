package com.chatpass.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI chatpassOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ChatPass API")
                        .description("ChatPass - Zulip Java 复刻项目 API 文单\n\n" +
                                "基于 Zulip (https://github.com/zulip/zulip) 复刻\n" +
                                "Original Copyright (c) 2012-2024 Kandra Labs, Inc.\n" +
                                "Licensed under Apache 2.0")
                        .version("v0.0.1")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0"))
                        .contact(new Contact()
                                .name("ChatPass Team")));
    }
}