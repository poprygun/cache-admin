package io.microsamples.cache.admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
//todo figure this out
//@ConditionalOnBean(name = {"redisTemplate", "keyRedisTemplate", "cacheManager"})
@ComponentScan({"io.microsamples.cache.admin.api"})
@Slf4j
public class CacheAdminAutoConfiguration {
}
