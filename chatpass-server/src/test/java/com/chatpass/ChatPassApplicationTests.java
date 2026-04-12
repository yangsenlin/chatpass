package com.chatpass;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Requires PostgreSQL connection - skip in test environment")
@SpringBootTest
class ChatPassApplicationTests {

    @Test
    void contextLoads() {
    }
}