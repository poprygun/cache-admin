package io.microsamples.cache.admin.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@ComponentScan({"io.microsamples.cache.admin"})
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "cache")
@Slf4j
public class CacheAdminAutoConfiguration extends CachingConfigurerSupport {

    /**
     * Configured as cache.specs map in application.yml to define cache expiry by name.
     */
    private Map<String, Long> specs;

    public Map<String, Long> getSpecs() {
        return specs;
    }

    public void setSpecs(Map<String, Long> specs) {
        this.specs = specs;
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                List<String> methodParams = new ArrayList<>();
                for (Object param : params) {
                    methodParams.add(param.toString());
                }

                CacheKey key = CacheKey.builder()
                        .className(target.getClass().toString())
                        .method(method.toString())
                        .params(methodParams)
                        .build();

                ObjectMapper objectMapper = new ObjectMapper();

                try {
                    return objectMapper.writeValueAsString(key);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Not able to process key for cache", e);
                }

            }
        };
    }

    @SuppressWarnings("rawtypes")
    @Bean
    @ConditionalOnMissingBean
    public CacheManager cacheManager(RedisTemplate redisTemplate) {
        RedisCacheManager rcm = new RedisCacheManager(redisTemplate);

        if (!CollectionUtils.isEmpty(specs))
                                rcm.setExpires(specs);

        return rcm;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, String> keyRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }


}
