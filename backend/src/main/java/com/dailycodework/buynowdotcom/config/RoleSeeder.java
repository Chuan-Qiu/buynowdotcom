package com.dailycodework.buynowdotcom.config;

import com.dailycodework.buynowdotcom.model.Role;
import com.dailycodework.buynowdotcom.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Ensures the two roles the authorization rules depend on exist before any
 * request is served. Without this, {@code hasRole("ADMIN")} could never match
 * and every write endpoint would be unreachable.
 *
 * <p>There is deliberately no API for granting ROLE_ADMIN — promotion is a
 * manual database operation, documented in the backend README.
 */
@Configuration
@RequiredArgsConstructor
public class RoleSeeder {

    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final RoleRepository roleRepository;

    @Bean
    CommandLineRunner seedRoles() {
        return args -> List.of(ROLE_USER, ROLE_ADMIN).forEach(name -> {
            if (roleRepository.findByName(name).isEmpty()) {
                Role role = new Role();
                role.setName(name);
                roleRepository.save(role);
            }
        });
    }
}
