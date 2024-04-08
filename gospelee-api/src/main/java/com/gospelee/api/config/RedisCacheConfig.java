package com.gospelee.api.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisCacheConfig {

  @Value("${redis.host}")
  private String host;

  @Value("${redis.port}")
  private int port;

  @Bean
  public CacheManager oidcCacheManager(RedisConnectionFactory cf) {
    RedisCacheConfiguration redisCacheConfiguration =
        RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()))
            .entryTtl(Duration.ofDays(7L));

    return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(cf)
        .cacheDefaults(redisCacheConfiguration)
        .build();
  }

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(host, port);
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(
      RedisConnectionFactory redisConnectionFactory) {
    GenericJackson2JsonRedisSerializer genericJackson2JsonSerializer = new GenericJackson2JsonRedisSerializer();
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    // key, value
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(genericJackson2JsonSerializer);

    // hash key, value
    redisTemplate.setHashKeySerializer(new StringRedisSerializer());
    redisTemplate.setHashValueSerializer(genericJackson2JsonSerializer);

    return redisTemplate;
  }

  @Bean
  public KeyGenerator customKeyGenerator() {
    return new CustomKeyGenerator();
  }

}
