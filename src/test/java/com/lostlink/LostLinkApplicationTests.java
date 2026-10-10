package com.lostlink;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("h2")
class LostLinkApplicationTests {

    @Test
    void contextLoads() {
        // Verifies Spring application context boots successfully
    }
}
