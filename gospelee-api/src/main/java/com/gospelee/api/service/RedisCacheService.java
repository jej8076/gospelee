package com.gospelee.api.service;

import com.gospelee.api.dto.jwt.JwkSetDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Cacheable(keyGenerator = "customKeyGenerator", cacheManager = "oidcCacheManager", cacheNames = "OIDCJwkSet")
public interface RedisCacheService {

  JwkSetDTO getPublicKeySet();
}
