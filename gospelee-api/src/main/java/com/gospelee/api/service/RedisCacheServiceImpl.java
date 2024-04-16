package com.gospelee.api.service;

import com.gospelee.api.dto.jwt.JwkSetDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RedisCacheServiceImpl implements RedisCacheService {

  String KAKAO_AUTH_URL = "https://kauth.kakao.com";
  String JWK_WELL_KNOWN_URI = "/.well-known/jwks.json";

  public JwkSetDTO getPublicKeySet() {
    RestClient restClient = RestClient.builder()
        .baseUrl(KAKAO_AUTH_URL)
        .build();

    return restClient.get()
        .uri(JWK_WELL_KNOWN_URI)
        .retrieve()
        .body(JwkSetDTO.class);
  }
}
