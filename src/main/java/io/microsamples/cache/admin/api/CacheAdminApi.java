package io.microsamples.cache.admin.api;

import io.microsamples.cache.admin.service.CacheAdminService;
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

    private CacheAdminService cacheAdminService;


    public CacheAdminApi(CacheAdminService cacheAdminService) {
        this.cacheAdminService = cacheAdminService;
    }

    @GetMapping
    public ResponseEntity<String> welcome() {
        return new ResponseEntity<>("Welcome to Cache Admin!", HttpStatus.OK);
    }


    @GetMapping("/cached")
    public ResponseEntity<Map> index(@RequestParam String cacheName) {

        if (StringUtils.isEmpty(cacheName))
            return new ResponseEntity("Cache name needs to be provided.", HttpStatus.BAD_REQUEST);

        Map<String, String> cachedData = cacheAdminService.dumpCacheFor(cacheName);

        return new ResponseEntity<>(cachedData, HttpStatus.OK);
    }


    @GetMapping("/purge")
    public ResponseEntity<Long> purge(@RequestParam String cacheName) {

        if (StringUtils.isEmpty(cacheName))
            return new ResponseEntity("Cache name needs to be provided.", HttpStatus.BAD_REQUEST);

        Set<String> range = cacheAdminService.evictAllObjectsFrom(cacheName);

        return new ResponseEntity(range.size(), HttpStatus.OK);
    }

    @GetMapping("/purgeall")
    public ResponseEntity<Long> purgeAll() {
        cacheAdminService.purgeAll();
        return new ResponseEntity(HttpStatus.OK);
    }


}

