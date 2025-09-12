package com.gospelee.api.service;

import com.gospelee.api.dto.common.RedisCacheDTO;
import com.gospelee.api.dto.jwt.JwkSetDTO;
import com.gospelee.api.enums.RedisCache;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RedisCacheServiceImpl implements RedisCacheService {

  private final CacheManager cacheManager;
  private final RedisTemplate<String, Object> redisTemplate;
  private final RestClient restClient;
  String JWK_WELL_KNOWN_URI = "/.well-known/jwks.json";

  public RedisCacheServiceImpl(CacheManager cacheManager,
      RedisTemplate<String, Object> redisTemplate, RestClient.Builder restClient) {
    this.cacheManager = cacheManager;
    this.redisTemplate = redisTemplate;
    this.restClient = restClient
        .baseUrl("https://kauth.kakao.com")
        .build();
  }

  @Override
  public JwkSetDTO getPublicKeySet() {
    return restClient.get()
        .uri(JWK_WELL_KNOWN_URI)
        .retrieve()
        .body(JwkSetDTO.class);
  }

  @Override
  public String put(RedisCacheDTO dto) {

    String namespacedKey = dto.getRedisCache().name() + "::" + dto.getKey();
    redisTemplate.opsForValue().set(namespacedKey, dto.getValue(), dto.getRedisCache().ttl());

    return dto.getValue();
  }

  @Override
  public String get(RedisCache redisCache, String key) {
    Cache cache = cacheManager.getCache(redisCache.name());
    if (cache != null) {
      return cache.get(key, String.class);
    }
    return null;
  }
}
