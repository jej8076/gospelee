package com.gospelee.api.service;

import com.gospelee.api.dto.jwt.JwkSet;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.RestClient;

public class AuthServiceImpl implements AuthService {

  @Override
  @Cacheable(cacheNames = "KakaoOICD", cacheManager = "oidcCacheManager")
  public JwkSet getPublicKeySet() {
    RestClient restClient = RestClient.builder()
        .baseUrl("https://kauth.kakao.com")
        .build();

    return restClient.get()
        .uri("/.well-known/jwks.json")
        .retrieve()
        .body(JwkSet.class);
  }
}
