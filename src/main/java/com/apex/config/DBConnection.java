package com.apex.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton Pattern Implementation
 * Manages the global lifecycle of the MySQL JDBC connection.
 * Uses Bill Pugh's static inner Holder class for thread-safe
 * lazy initialization without synchronization overhead.
 *
 * Note: In Spring Boot context, JdbcTemplate uses the DataSource
 * configured in application.properties for repository operations.
 * This Singleton demonstrates the pattern explicitly as required
 * by the proposal, and can be used for raw JDBC operations.
 */
@Component
public class DBConnection {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    // Private constructor — prevents external instantiation
    private DBConnection() {}

    // Bill Pugh Singleton — thread-safe without synchronized keyword
    private static class Holder {
        private static final DBConnection INSTANCE = new DBConnection();
    }

    // Global access point
    public static DBConnection getInstance() {
        return Holder.INSTANCE;
    }

    // Provides a raw JDBC connection for direct use
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }
}
