package com.apex.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton Pattern Implementation.
 *
 * Spring Boot repositories use JdbcTemplate and the configured DataSource
 * for normal application work. This class keeps an explicit Singleton
 * example for the proposal and for rare raw JDBC usage.
 */
public class DBConnection {

    private DBConnection() {}

    private static class Holder {
        private static final DBConnection INSTANCE = new DBConnection();
    }

    public static DBConnection getInstance() {
        return Holder.INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        String jdbcUrl = System.getProperty(
                "spring.datasource.url",
                "jdbc:mysql://localhost:3306/apex_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kuala_Lumpur");
        String username = System.getProperty(
                "spring.datasource.username", "root");
        String password = System.getProperty(
                "spring.datasource.password", "031216Low");
        return DriverManager.getConnection(jdbcUrl, username, password);
    }
}
