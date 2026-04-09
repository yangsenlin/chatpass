package com.chatpass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ChatPass - Zulip Java 复刻项目
 * 
 * 基于 Zulip (https://github.com/zulip/zulip) 复刻
 * Original Copyright (c) 2012-2024 Kandra Labs, Inc.
 * Licensed under Apache 2.0
 * 
 * @author ChatPass Team
 * @since 2026-04-09
 */
@SpringBootApplication
public class ChatPassApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatPassApplication.class, args);
    }
}