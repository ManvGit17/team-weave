package io.teamweave;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Loads the full context against PostgreSQL, which runs the Flyway migrations
 * and validates every JPA mapping against the resulting schema — so
 * entity/schema drift fails the build.
 */
@SpringBootTest
@ActiveProfiles("test")
class TeamweaveApplicationTests {

    @Test
    void contextLoads() {
    }
}
