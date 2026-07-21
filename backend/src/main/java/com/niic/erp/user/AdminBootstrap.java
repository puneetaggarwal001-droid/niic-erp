package com.niic.erp.user;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the initial admin account at startup instead of seeding a known
 * credential in the schema (which would ship a public default password in a
 * public repo). Idempotent: it only creates the account when it is absent.
 *
 * <ul>
 *   <li>prod: the password must come from ERP_ADMIN_PASSWORD — startup fails fast
 *       if it is unset, so no deployment ever runs on a guessable default.</li>
 *   <li>dev: if no password is provided, a random one is generated and logged
 *       once, so local runs still have a working login without a shipped default.</li>
 * </ul>
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    private final String username;
    private final String configuredPassword;

    public AdminBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder, Environment environment,
                          @Value("${erp.admin.username:admin}") String username,
                          @Value("${erp.admin.password:}") String configuredPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
        this.username = username;
        this.configuredPassword = configuredPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        boolean prod = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        String password = configuredPassword;
        boolean generated = false;
        if (password == null || password.isBlank()) {
            if (prod) {
                throw new IllegalStateException(
                        "ERP_ADMIN_PASSWORD must be set to create the initial admin '" + username + "' in prod.");
            }
            password = generatePassword();
            generated = true;
        }

        userRepository.save(new User(username, passwordEncoder.encode(password), Role.ADMIN));

        if (generated) {
            log.warn("No erp.admin.password set — created initial admin '{}' with a generated dev password: {} "
                    + "(set ERP_ADMIN_PASSWORD to control it; change it before any shared use).", username, password);
        } else {
            log.info("Created initial admin '{}' from the configured password.", username);
        }
    }

    private static String generatePassword() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
