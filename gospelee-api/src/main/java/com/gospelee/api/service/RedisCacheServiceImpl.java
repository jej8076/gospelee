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
  String KAKAO_AUTH_URL = "https://kauth.kakao.com";
  String JWK_WELL_KNOWN_URI = "/.well-known/jwks.json";

  public RedisCacheServiceImpl(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  @Override
  public JwkSetDTO getPublicKeySet() {
    RestClient restClient = RestClient.builder()
        .baseUrl(KAKAO_AUTH_URL)
        .build();

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
