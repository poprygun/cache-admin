package io.microsamples.cache.admin.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/cache-admin")
@Slf4j
public class CacheAdminApi {
    private final static String CACHE_SUFFIX = "~keys";

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    @Qualifier("keyRedisTemplate")
    private RedisTemplate keyRedisTemplate;

    @Autowired
    private CacheManager cacheManager;

    @Value("${spring.cache.cache-names}")
    private String[] cacheNames;

    public CacheAdminApi(RedisTemplate<String, Object> redisTemplate, RedisTemplate keyRedisTemplate) {
        this.keyRedisTemplate = keyRedisTemplate;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping
    public ResponseEntity<String> welcome() {
        return new ResponseEntity<>("Welcome to Cache Admin!", HttpStatus.OK);
    }


    @GetMapping("/cached")
    public ResponseEntity<Map> index(@RequestParam String cacheName) {

        if (StringUtils.isEmpty(cacheName))
            return new ResponseEntity("Cache name needs to be provided.", HttpStatus.BAD_REQUEST);

        Set<String> range = namedCacheValues(cacheName);

        Map<String, String> cachedData = new HashMap<>();

        if (!CollectionUtils.isEmpty(range))
            range.stream().forEach(v -> cachedData.put(v, redisTemplate.opsForValue().get(v).toString()));

        return new ResponseEntity<>(cachedData, HttpStatus.OK);
    }

    private Set<String> namedCacheValues(@RequestParam String cacheName) {
        String keyName = cacheName.concat(CACHE_SUFFIX);
        long cachedSize = keyRedisTemplate.opsForZSet().size(keyName);
        return keyRedisTemplate.opsForZSet().range(keyName, 0, cachedSize);
    }


    @GetMapping("/purge")
    public ResponseEntity<Long> purge(@RequestParam String cacheName) {

        if (StringUtils.isEmpty(cacheName))
            return new ResponseEntity("Cache name needs to be provided.", HttpStatus.BAD_REQUEST);

        Cache cache = cacheManager.getCache(cacheName);

        Set<String> range = namedCacheValues(cacheName);

        if (!CollectionUtils.isEmpty(range))
            range.stream().forEach(v -> cache.evict(v));

        return new ResponseEntity(range.size(), HttpStatus.OK);
    }

    @GetMapping("/purgeall")
    public ResponseEntity<Long> purgeAll() {
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            cache.clear();
            log.info("...Cleared {}", cacheName);
        }
        return new ResponseEntity(HttpStatus.OK);
    }


}

