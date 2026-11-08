package org.akusher.staybook;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Disabled when PostgreSQL containers are not active. Enable during full integration testing.")
class StayBookApplicationTests {

    @Test
    void contextLoads() {
    }

}
