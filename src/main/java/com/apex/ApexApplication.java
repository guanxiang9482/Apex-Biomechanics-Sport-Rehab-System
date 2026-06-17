package com.apex;

import com.apex.config.DBConnection;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApexApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApexApplication.class, args);
    }

    @Bean
    ApplicationRunner verifySingletonPattern() {
        return args -> DBConnection.getInstance();
    }
}
