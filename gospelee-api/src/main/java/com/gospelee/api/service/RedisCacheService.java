package com.gospelee.api.service;

import com.gospelee.api.dto.common.RedisCacheDTO;
import com.gospelee.api.dto.jwt.JwkSetDTO;
import com.gospelee.api.enums.RedisCache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

public interface RedisCacheService {

  @Cacheable(keyGenerator = "customKeyGenerator", cacheManager = "oidcCacheManager", cacheNames = "OIDCJwkSet")
  JwkSetDTO getPublicKeySet();

  String put(RedisCacheDTO redisCacheDTO);

  String get(RedisCache redisCache, String key);
}
