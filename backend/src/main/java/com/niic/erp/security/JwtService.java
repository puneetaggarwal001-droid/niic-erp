package com.niic.erp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    // The env-var-overridable default committed in application.yml. Fine for local
    // dev, but signing prod tokens with a value published in a public repo is a full
    // auth bypass — anyone could forge tokens for any user/role. We fail closed on it.
    static final String DEV_DEFAULT_SECRET = "dev-only-secret-change-me-dev-only-secret-change-me";

    private final SecretKey signingKey;
    private final long expirationMinutes;

    public JwtService(
            @Value("${erp.jwt.secret}") String secret,
            @Value("${erp.jwt.expiration-minutes}") long expirationMinutes,
            Environment environment) {
        boolean prod = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (prod && (secret == null || secret.isBlank() || DEV_DEFAULT_SECRET.equals(secret))) {
            throw new IllegalStateException(
                    "erp.jwt.secret must be set to a strong, non-default value in prod. "
                            + "Provide the ERP_JWT_SECRET env var (>= 32 bytes); the committed dev default is rejected.");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(String username, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String expectedUsername) {
        try {
            Claims claims = parseClaims(token);
            return claims.getSubject().equals(expectedUsername) && claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
