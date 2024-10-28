package com.gospelee.socket.service;

import com.gospelee.socket.dto.jwt.JwkSetDTO;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public interface RedisCacheService {

  @Cacheable(keyGenerator = "customKeyGenerator", cacheManager = "oidcCacheManager", cacheNames = "OIDCJwkSet")
  JwkSetDTO getPublicKeySet();

  @CachePut(key = "#key", cacheNames = "someCacheData")
  String putAnyKeyValue(String key, String value);

  @Cacheable(key = "#key", cacheNames = "someCacheData")
  public String getAnyKeyValue(String key);
}
