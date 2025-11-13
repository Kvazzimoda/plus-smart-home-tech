package ru.yandex.practicum.commerce.shopping.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.yandex.practicum.commerce.interaction.client.warehouse.WarehouseClient;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableFeignClients(clients = WarehouseClient.class)
public class CartApp {
    public static void main(String[] args) {
        SpringApplication.run(CartApp.class, args);
    }
}
