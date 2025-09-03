package com.gospelee.api.service;

import com.gospelee.api.dto.common.RedisCacheDTO;
import com.gospelee.api.dto.jwt.JwkSetDTO;
import com.gospelee.api.enums.RedisCacheName;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RedisCacheServiceImpl implements RedisCacheService {

  private final CacheManager cacheManager;
  private final RestClient restClient;
  String JWK_WELL_KNOWN_URI = "/.well-known/jwks.json";

  public RedisCacheServiceImpl(CacheManager cacheManager, RestClient.Builder restClient) {
    this.cacheManager = cacheManager;
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
  public String put(RedisCacheDTO redisCacheDTO) {
    Cache cache = cacheManager.getCache(redisCacheDTO.getRedisCacheName().name());
    if (cache != null) {
      cache.put(redisCacheDTO.getKey(), redisCacheDTO.getValue());
    }
    return redisCacheDTO.getValue();
  }

  @Override
  public String get(RedisCacheName redisCacheName, String key) {
    Cache cache = cacheManager.getCache(redisCacheName.name());
    if (cache != null) {
      return cache.get(key, String.class);
    }
    return null;
  }
}
