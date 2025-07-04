package org.ouanu.manager.service;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@EnableScheduling
public class TempDataService {
    private static final int MAX_CAPACITY = 1000;
    private static final int EVICTION_BATCH_SIZE = 50; // 每次清理数量
    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentHashMap<>(MAX_CAPACITY);

    public boolean store(String key, Object value) {
        if (cache.size() >= MAX_CAPACITY) {
            if (!performEviction()) {
                return false;
            }
        }
        cache.put(key, new CacheEntry(value));
        return true;
    }

    private boolean performEviction() {
        cleanup();
        if (cache.size() >= MAX_CAPACITY) {
            evictOldestEntries();
        }
        return cache.size() < MAX_CAPACITY;
    }

    // 基于LRU策略清理最老条目
    private void evictOldestEntries() {
        List<Map.Entry<String, CacheEntry>> entries = new ArrayList<>(cache.entrySet());
        entries.sort(Comparator.comparing(
                entry -> entry.getValue().lastAccessTime
        ));

        int removed = 0;
        for (Map.Entry<String, CacheEntry> entry : entries) {
            if (removed >= TempDataService.EVICTION_BATCH_SIZE) break;

            if (cache.remove(entry.getKey(), entry.getValue())) {
                removed++;
            }
        }
    }


    private static class CacheEntry {
        final Object value;
        volatile Instant lastAccessTime;

        CacheEntry(Object value) {
            this.value = value;
            this.lastAccessTime = Instant.now();
        }

        void touch() {
            lastAccessTime = Instant.now();
        }
    }


    public <T> T get(String key, Class<T> type) {
        CacheEntry entry = cache.get(key);
        if (entry == null) return null;

        entry.touch();

        if (isExpired(entry)) {
            cache.remove(key, entry);
            return null;
        }

        return type.cast(entry.value);
    }

    public void remove(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) return;
        entry.touch();
        cache.remove(key, entry);
    }

    @Scheduled(fixedRate = 15 * 60000)
    public void cleanup() {
        cache.entrySet().removeIf(entry ->
                isExpired(entry.getValue())
        );
    }

    private boolean isExpired(CacheEntry entry) {
        return entry.lastAccessTime.isBefore(Instant.now().minus(60, ChronoUnit.SECONDS));
    }
}

