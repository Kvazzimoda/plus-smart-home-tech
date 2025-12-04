package ru.yandex.practicum.commerce.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableFeignClients(basePackages = "ru.yandex.practicum.commerce.interaction.client")
public class DeliveryApp {
    public static void main(String[] args) {
        SpringApplication.run(DeliveryApp.class, args);
    }
}
