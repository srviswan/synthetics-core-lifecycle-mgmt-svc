package com.synthetics.core.infra.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Cache service for caching frequently accessed data
 * Provides in-memory caching with TTL support
 */
@Service
@Slf4j
public class CacheService {
    
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    
    /**
     * Get value from cache or compute if not present
     * @param key The cache key
     * @param supplier The supplier to compute value if not cached
     * @param ttl The time-to-live duration
     * @return CompletableFuture containing the cached or computed value
     */
    public <T> CompletableFuture<T> getOrCompute(String key, Supplier<T> supplier, Duration ttl) {
        CacheEntry entry = cache.get(key);
        
        if (entry != null && !entry.isExpired()) {
            log.debug("Cache hit for key: {}", key);
            return CompletableFuture.completedFuture((T) entry.getValue());
        }
        
        log.debug("Cache miss for key: {}, computing value", key);
        return CompletableFuture.supplyAsync(() -> {
            T value = supplier.get();
            cache.put(key, new CacheEntry(value, LocalDateTime.now().plus(ttl)));
            return value;
        });
    }
    
    /**
     * Get value from cache or compute async if not present
     * @param key The cache key
     * @param supplier The async supplier to compute value if not cached
     * @param ttl The time-to-live duration
     * @return CompletableFuture containing the cached or computed value
     */
    public <T> CompletableFuture<T> getOrComputeAsync(String key, Supplier<CompletableFuture<T>> supplier, Duration ttl) {
        CacheEntry entry = cache.get(key);
        
        if (entry != null && !entry.isExpired()) {
            log.debug("Cache hit for key: {}", key);
            return CompletableFuture.completedFuture((T) entry.getValue());
        }
        
        log.debug("Cache miss for key: {}, computing async value", key);
        return supplier.get().thenApply(value -> {
            cache.put(key, new CacheEntry(value, LocalDateTime.now().plus(ttl)));
            return value;
        });
    }
    
    /**
     * Put value in cache
     * @param key The cache key
     * @param value The value to cache
     * @param ttl The time-to-live duration
     */
    public void put(String key, Object value, Duration ttl) {
        cache.put(key, new CacheEntry(value, LocalDateTime.now().plus(ttl)));
        log.debug("Cached value for key: {} with TTL: {}", key, ttl);
    }
    
    /**
     * Get value from cache
     * @param key The cache key
     * @return The cached value or null if not present/expired
     */
    public Object get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            log.debug("Cache hit for key: {}", key);
            return entry.getValue();
        }
        
        if (entry != null && entry.isExpired()) {
            log.debug("Cache expired for key: {}, removing", key);
            cache.remove(key);
        }
        
        return null;
    }
    
    /**
     * Remove value from cache
     * @param key The cache key
     */
    public void remove(String key) {
        cache.remove(key);
        log.debug("Removed cache entry for key: {}", key);
    }
    
    /**
     * Clear all cache entries
     */
    public void clear() {
        cache.clear();
        log.info("Cleared all cache entries");
    }
    
    /**
     * Get cache size
     * @return The number of cache entries
     */
    public int size() {
        return cache.size();
    }
    
    /**
     * Clean expired entries
     * @return The number of expired entries removed
     */
    public int cleanExpired() {
        int removed = 0;
        for (String key : cache.keySet()) {
            CacheEntry entry = cache.get(key);
            if (entry != null && entry.isExpired()) {
                cache.remove(key);
                removed++;
            }
        }
        
        if (removed > 0) {
            log.debug("Cleaned {} expired cache entries", removed);
        }
        
        return removed;
    }
    
    /**
     * Cache entry with expiration
     */
    private static class CacheEntry {
        private final Object value;
        private final LocalDateTime expirationTime;
        
        public CacheEntry(Object value, LocalDateTime expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }
        
        public Object getValue() {
            return value;
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expirationTime);
        }
    }
}



