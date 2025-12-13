package com.spartaecommerce.cart.adapter.out.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spartaecommerce.cart.domain.entity.Cart;
import com.spartaecommerce.cart.domain.port.out.CartStoragePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@ConditionalOnProperty(name = "cart.storage.type", havingValue = "redis")
public class RedisCartStorageAdapter implements CartStoragePort {

    private static final String CACHE_KEY_PREFIX = "cart:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisCartStorageAdapter(
        RedisTemplate<String, String> redisTemplate,
        ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        log.info("RedisCartStorageAdapter initialized");
    }

    @Override
    public void save(Cart cart) {
        if (cart == null || cart.getUserId() == null) {
            log.warn("Cannot save null cart or cart without userId");
            return;
        }

        try {
            String key = generateKey(cart.getUserId());
            String value = objectMapper.writeValueAsString(cart);
            redisTemplate.opsForValue().set(key, value, CACHE_TTL);
            log.debug("Cart saved to Redis: userId={}", cart.getUserId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize cart to JSON: userId={}", cart.getUserId(), e);
        }
    }

    @Override
    public Optional<Cart> get(Long userId) {
        if (userId == null) {
            log.warn("Cannot get cart with null userId");
            return Optional.empty();
        }

        try {
            String key = generateKey(userId);
            String value = redisTemplate.opsForValue().get(key);

            if (value == null) {
                log.debug("Cart not found in Redis: userId={}", userId);
                return Optional.empty();
            }

            Cart cart = objectMapper.readValue(value, Cart.class);
            log.debug("Cart found in Redis: userId={}", userId);
            return Optional.of(cart);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize cart from JSON: userId={}", userId, e);
            return Optional.empty();
        }
    }

    @Override
    public void delete(Long userId) {
        if (userId == null) {
            log.warn("Cannot delete cart with null userId");
            return;
        }

        String key = generateKey(userId);
        redisTemplate.delete(key);
        log.debug("Cart deleted from Redis: userId={}", userId);
    }

    private String generateKey(Long userId) {
        return CACHE_KEY_PREFIX + userId;
    }
}
