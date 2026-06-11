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
 * Security Configuration
 * For demo environment: authentication is handled manually
 * via our AccountService login logic and localStorage.
 * Spring Security is used here strictly for BCrypt
 * password encoding — a core security requirement.
 *
 * Note: In a production system, this would be upgraded
 * to JWT token-based stateless authentication.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // All API endpoints permitted for demo
                // RBAC is enforced at service layer via
                // role checks in controllers
                .anyRequest().permitAll()
            );
        return http.build();
    }

    // BCrypt — industry standard password hashing
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
