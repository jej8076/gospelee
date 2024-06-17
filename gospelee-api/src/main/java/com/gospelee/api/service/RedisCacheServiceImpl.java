package com.gospelee.api.service;

import com.gospelee.api.dto.jwt.JwkSetDTO;
import org.springframework.cache.annotation.CachePut;
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

  @Override
  public String putAnyKeyValue(String key, String value) {
    // @CachePut 어노테이션에 의해 자동으로 캐시에 저장
    return value;
  }

  @Override
  public String getAnyKeyValue(String key) {
    // @Cacheable 어노테이션에 의해 캐시에서 데이터를 조회합니다.
    // 캐시에 없으면 null을 반환합니다.
    return null; // 실제 구현에서는 데이터 소스에서 가져오는 로직을 추가할 수 있습니다.
  }
}
