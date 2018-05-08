package io.microsamples.cache.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class CacheAdminService {
    private CacheManager cacheManager;
    private RedisTemplate<String, Object> redisTemplate;
    private RedisTemplate keyRedisTemplate;

    private final static String CACHE_SUFFIX = "~keys";

    @Value("${spring.cache.cache-names:specify-in-parent-proj}")
    private String[] cacheNames;



    public CacheAdminService(CacheManager cacheManager
            , RedisTemplate<String, Object> redisTemplate
            , RedisTemplate keyRedisTemplate) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
        this.keyRedisTemplate = keyRedisTemplate;
    }

    public void purgeAll() {
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            cache.clear();
            log.info("...Cleared {}", cacheName);
        }
    }

    public Map<String, String> dumpCacheFor(@RequestParam String cacheName) {
        Set<String> range = namedCacheValues(cacheName);

        Map<String, String> cachedData = new HashMap<>();

        if (!CollectionUtils.isEmpty(range))
            range.stream().forEach(v -> cachedData.put(v, redisTemplate.opsForValue().get(v).toString()));
        return cachedData;
    }

    public Set<String> evictAllObjectsFrom(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);

        Set<String> range = namedCacheValues(cacheName);

        if (!CollectionUtils.isEmpty(range))
            range.stream().forEach(v -> cache.evict(v));

        return range;
    }

    public Set<String> namedCacheValues(@RequestParam String cacheName) {
        String keyName = cacheName.concat(CACHE_SUFFIX);
        long cachedSize = keyRedisTemplate.opsForZSet().size(keyName);
        return keyRedisTemplate.opsForZSet().range(keyName, 0, cachedSize);
    }
}
