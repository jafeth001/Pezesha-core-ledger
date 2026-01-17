package com.fintech.pezesha_core_ledger.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {
    private final CacheManager cacheManager;
    private static final String CACHE_NAME = "idempotency";

    public boolean isDuplicate(String idempotencyKey) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        return cache != null && cache.get(idempotencyKey) != null;
    }

    public void storeIdempotencyKey(String idempotencyKey, Object result) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put(idempotencyKey, result);
            log.debug("Stored idempotency key in cache: {}", idempotencyKey);
        }
    }

    public <T> T getIdempotentResult(String idempotencyKey, Class<T> clazz) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) return null;
        Object value = cache.get(idempotencyKey, Object.class);
        if (clazz.isInstance(value)) {
            log.debug("Found idempotent result for key: {}", idempotencyKey);
            return clazz.cast(value);
        }
        return null;
    }
}
