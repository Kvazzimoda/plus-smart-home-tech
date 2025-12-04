package ru.yandex.practicum.commerce.payment.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Регистрируем кеши с разными настройками
        // Для payments (средний TTL)
        cacheManager.registerCustomCache("payments", Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .expireAfterAccess(1, TimeUnit.HOURS)
                .recordStats()
                .build());

        // Для payment-methods (длинный TTL, так как редко меняются)
        cacheManager.registerCustomCache("payment-methods", Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .expireAfterAccess(2, TimeUnit.HOURS)
                .recordStats()
                .build());

        // Для transaction-status (короткий TTL, так как статусы часто меняются)
        cacheManager.registerCustomCache("transaction-status", Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .recordStats()
                .build());

        // Для payment-sessions (короткий TTL)
        cacheManager.registerCustomCache("payment-sessions", Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .recordStats()
                .build());

        // Для refunds
        cacheManager.registerCustomCache("refunds", Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .recordStats()
                .build());

        // Для payment-gateways (очень длинный TTL)
        cacheManager.registerCustomCache("payment-gateways", Caffeine.newBuilder()
                .maximumSize(50)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .recordStats()
                .build());

        // Для order-calculations (расчеты стоимости)
        cacheManager.registerCustomCache("order-calculations", Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .expireAfterAccess(20, TimeUnit.MINUTES)
                .recordStats()
                .build());

        // Для product-prices (цены товаров)
        cacheManager.registerCustomCache("product-prices", Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .recordStats()
                .build());

        return cacheManager;
    }
}
