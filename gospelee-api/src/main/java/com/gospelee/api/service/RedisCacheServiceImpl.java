package com.gospelee.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gospelee.api.dto.common.RedisCacheDTO;
import com.gospelee.api.dto.jwt.JwkSetDTO;
import com.gospelee.api.enums.RedisCacheNames;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RedisCacheServiceImpl implements RedisCacheService {

  private final CacheManager cacheManager;
  private final RedisTemplate<String, String> redisTemplate;
  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  String JWK_CACHE_KEY = "kakao";
  String JWK_WELL_KNOWN_URI = "/.well-known/jwks.json";
  String REDIS_SEPARATOR = "::";

  public RedisCacheServiceImpl(CacheManager cacheManager,
      RedisTemplate<String, String> redisTemplate, RestClient.Builder restClient,
      ObjectMapper objectMapper) {
    this.cacheManager = cacheManager;
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
    this.restClient = restClient
        .baseUrl("https://kauth.kakao.com")
        .build();
  }

  @Override
  public JwkSetDTO getPublicKeySet() {
    String cachedJson = this.get(RedisCacheNames.KAKAO_JWK_SET, JWK_CACHE_KEY);

    if (cachedJson != null) {
      try {
        return objectMapper.readValue(cachedJson, JwkSetDTO.class);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("JWKSet 역직렬화 실패", e);
      }
    }

    // 캐시에 없으면 외부에서 가져옴
    JwkSetDTO fetched = restClient.get()
        .uri(JWK_WELL_KNOWN_URI)
        .retrieve()
        .body(JwkSetDTO.class);

    RedisCacheDTO redisCacheDTO = RedisCacheDTO.builder()
        .redisCacheNames(RedisCacheNames.KAKAO_JWK_SET)
        .key(JWK_CACHE_KEY)
        .value(fetched)
        .build();
    this.put(redisCacheDTO);

    return fetched;
  }

  @Override
  public String put(RedisCacheDTO dto) {

    String namespacedKey = dto.getRedisCacheNames().name() + REDIS_SEPARATOR + dto.getKey();
    Object value = dto.getValue();
    String store;

    if (value instanceof String) {
      store = (String) value;
    } else {
      try {
        store = objectMapper.writeValueAsString(value);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Redis 캐시 직렬화 실패", e);
      }
    }

    redisTemplate.opsForValue().set(namespacedKey, store, dto.getRedisCacheNames().ttl());
    return store;
  }

  @Override
  public String get(RedisCacheNames redisCacheNames, String key) {
    Cache cache = cacheManager.getCache(redisCacheNames.name());
    if (cache != null) {
      return cache.get(key, String.class);
    }
    return null;
  }
}
