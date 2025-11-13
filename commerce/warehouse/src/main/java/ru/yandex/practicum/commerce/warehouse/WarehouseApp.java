package ru.yandex.practicum.commerce.warehouse;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableFeignClients
public class WarehouseApp {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseApp.class, args);
    }

    // Новый тест-раннер: Запустится автоматически после bootstrap
    @Bean
    public CommandLineRunner testDbConnection(DataSource dataSource) {
        return args -> {
            System.out.println("=== DB TEST START ===");
            try (Connection conn = dataSource.getConnection()) {
                System.out.println("✅ Connection OK! Catalog: " + conn.getCatalog());  // Должен: commerce_warehouse
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT version();")) {
                    if (rs.next()) {
                        System.out.println("✅ PostgreSQL version: " + rs.getString(1));
                    }
                }
                System.out.println("=== DB TEST END ===");
            } catch (Exception e) {
                System.err.println("❌ DB Connection FAILED: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}
