package com.gospelee.api.service;

import com.gospelee.api.dto.jwt.JwkSetDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import redis.embedded.Redis;

@Service
public class RedisCacheServiceImpl implements RedisCacheService {

  public JwkSetDTO getPublicKeySet() {
    RestClient restClient = RestClient.builder()
        .baseUrl("https://kauth.kakao.com")
        .build();

    return restClient.get()
        .uri("/.well-known/jwks.json")
        .retrieve()
        .body(JwkSetDTO.class);
  }
}
