package com.gospelee.api.enums;

import java.time.Duration;

public enum RedisCacheNames {
  KAKAO_JWK_SET(Duration.ofDays(7L)),
  NONCE(Duration.ofMinutes(5L)),
  USER_ME(Duration.ofHours(6L)),
  ;

  final private Duration ttl;

  RedisCacheNames(Duration ttl) {
    this.ttl = ttl;
  }

  public Duration ttl() {
    return ttl;
  }

}
