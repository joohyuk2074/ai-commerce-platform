package com.spartaecommerce.cart.adapter.out.storage;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.spartaecommerce.cart.domain.entity.Cart;
import com.spartaecommerce.cart.domain.port.out.CartStoragePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine 기반 로컬 메모리 캐시 구현
 * - 단일 서버 환경에서 사용
 * - 빠른 응답 속도
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "cart.storage.type", havingValue = "caffeine", matchIfMissing = true)
public class CaffeineCartStorageAdapter implements CartStoragePort {

    private static final String CACHE_KEY_PREFIX = "cart:";
    private final Cache<String, Cart> cache;

    public CaffeineCartStorageAdapter() {
        this.cache = Caffeine.newBuilder()
            .maximumSize(10_000)  // 최대 10,000개 캐시
            .expireAfterWrite(30, TimeUnit.MINUTES)  // 30분 후 만료
            .recordStats()  // 통계 기록
            .build();

        log.info("CaffeineCartStorageAdapter initialized");
    }

    @Override
    public void save(Cart cart) {
        if (cart == null || cart.getUserId() == null) {
            log.warn("Cannot save null cart or cart without userId");
            return;
        }

        String key = generateKey(cart.getUserId());
        cache.put(key, cart);
        log.debug("Cart saved to Caffeine cache: userId={}", cart.getUserId());
    }

    @Override
    public Optional<Cart> get(Long userId) {
        if (userId == null) {
            log.warn("Cannot get cart with null userId");
            return Optional.empty();
        }

        String key = generateKey(userId);
        Cart cart = cache.getIfPresent(key);

        if (cart != null) {
            log.debug("Cart found in Caffeine cache: userId={}", userId);
        } else {
            log.debug("Cart not found in Caffeine cache: userId={}", userId);
        }

        return Optional.ofNullable(cart);
    }

    @Override
    public void delete(Long userId) {
        if (userId == null) {
            log.warn("Cannot delete cart with null userId");
            return;
        }

        String key = generateKey(userId);
        cache.invalidate(key);
        log.debug("Cart deleted from Caffeine cache: userId={}", userId);
    }

    private String generateKey(Long userId) {
        return CACHE_KEY_PREFIX + userId;
    }
}
