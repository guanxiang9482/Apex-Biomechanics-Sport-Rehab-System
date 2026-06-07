package com.apex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * RBAC Configuration
 * Enforces Role-Based Access Control at the Handler Tier.
 * Each endpoint is restricted to specific roles as defined
 * in the proposal's use case specifications.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        http
            // Disable CSRF for REST API (frontend handles this)
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — no auth required
                .requestMatchers("/api/auth/**").permitAll()
                // Athlete-only endpoints
                .requestMatchers("/api/athlete/**")
                    .hasAnyRole("ATHLETE", "ADMIN")
                // Therapist-only endpoints
                .requestMatchers("/api/therapist/**")
                    .hasAnyRole("THERAPIST", "ADMIN")
                // Admin-only endpoints
                .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")
                // Shared endpoints
                .requestMatchers("/api/sessions/today").authenticated()
                .requestMatchers("/api/notifications/**").authenticated()
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            // Use HTTP Basic for simplicity in demo environment
            .httpBasic(basic -> {});

        return http.build();
    }

    // BCrypt password encoder — industry standard
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
