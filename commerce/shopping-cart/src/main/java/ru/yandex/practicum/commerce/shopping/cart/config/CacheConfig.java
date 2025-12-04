package ru.yandex.practicum.commerce.shopping.cart.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Общие настройки для всех кешей
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)  // Для shopping-cart короткий TTL
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .recordStats()
                .weakKeys());

        // Имена кешей (должны совпадать с теми, что в @Cacheable)
        cacheManager.setCacheNames(List.of(
                "shopping-carts",
                "cart-items",
                "user-active-carts",
                "product-availability",
                "cart-totals"
        ));

        return cacheManager;
    }
}
