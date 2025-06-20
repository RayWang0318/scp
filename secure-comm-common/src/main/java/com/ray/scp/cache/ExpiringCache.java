package com.ray.scp.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 轻量级可过期缓存，无线程泄露风险（懒清理方式）
 */
public class ExpiringCache<K, V> {

    private final ConcurrentHashMap<K, CacheValue<V>> map = new ConcurrentHashMap<>();
    private final long defaultExpireMillis;

    public ExpiringCache(long defaultExpireMillis) {
        this.defaultExpireMillis = defaultExpireMillis;
    }

    public void put(K key, V value) {
        put(key, value, defaultExpireMillis);
    }

    public void put(K key, V value, long expireMillis) {
        long expireAt = System.currentTimeMillis() + expireMillis;
        map.put(key, new CacheValue<>(value, expireAt));
    }

    public boolean containsKey(K key) {
        CacheValue<V> cached = map.get(key);
        if (cached == null || cached.isExpired()) {
            map.remove(key);
            return false;
        }
        return true;
    }

    public V get(K key) {
        CacheValue<V> cached = map.get(key);
        if (cached == null || cached.isExpired()) {
            map.remove(key);
            return null;
        }
        return cached.value;
    }

    public void remove(K key) {
        map.remove(key);
    }

    /**
     * 可选调用：外部可定时调用进行批量清理
     */
    public void cleanUp() {
        long now = System.currentTimeMillis();
        for (Map.Entry<K, CacheValue<V>> entry : map.entrySet()) {
            if (entry.getValue().expireAt < now) {
                map.remove(entry.getKey());
            }
        }
    }

    private static class CacheValue<V> {
        final V value;
        final long expireAt;

        CacheValue(V value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireAt;
        }
    }
}