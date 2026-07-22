package com.niic.erp.db;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Boots the full application against a real PostgreSQL server (an embedded
 * Postgres binary, no Docker needed) using the prod profile. If the context
 * loads, then against real Postgres: every Flyway migration applied, Hibernate
 * schema validation (ddl-auto: validate) matched every entity to its table, and
 * all beans wired. This is the guard that catches H2-only SQL and @Lob/CLOB
 * mapping mismatches before they reach production — a plain Flyway-only run does
 * not exercise Hibernate validation and would miss them.
 */
@SpringBootTest
@ActiveProfiles("prod")
class PostgresMigrationTest {

    private static EmbeddedPostgres postgres;

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
        // The prod profile requires these with no fallback.
        registry.add("erp.jwt.secret", () -> "test-only-secret-test-only-secret-test-only-secret");
        registry.add("erp.admin.password", () -> "test-admin-pw");
    }

    @AfterAll
    static void stopPostgres() throws Exception {
        if (postgres != null) {
            postgres.close();
        }
    }

    @Test
    void appBootsAndValidatesSchemaOnPostgres() {
        // The @SpringBootTest context load is the assertion: Flyway migrate +
        // Hibernate validate + bean wiring all succeeded against real Postgres.
    }
}
