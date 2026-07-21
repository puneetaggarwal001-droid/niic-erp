package com.niic.erp.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class JwtServiceTest {

    private static final String STRONG_SECRET = "a-strong-production-secret-value-that-is-long-enough-32b";

    // Fail-fast: signing prod tokens with the committed dev default (published in a
    // public repo) is a full auth bypass, so the context must refuse to start.
    @Test
    void prodProfileRejectsCommittedDevDefault() {
        MockEnvironment env = new MockEnvironment().withProperty("x", "x");
        env.setActiveProfiles("prod");
        assertThatThrownBy(() -> new JwtService(JwtService.DEV_DEFAULT_SECRET, 480, env))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ERP_JWT_SECRET");
    }

    @Test
    void prodProfileRejectsBlankSecret() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        assertThatThrownBy(() -> new JwtService("  ", 480, env))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void prodProfileAcceptsStrongSecret() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        JwtService service = new JwtService(STRONG_SECRET, 480, env);
        assertThat(service.generateToken("admin", "ADMIN")).isNotBlank();
    }

    // Dev keeps working on the default — the guard only bites under the prod profile.
    @Test
    void devProfileAllowsDevDefault() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");
        assertThatCode(() -> new JwtService(JwtService.DEV_DEFAULT_SECRET, 480, env))
                .doesNotThrowAnyException();
    }
}
