package com.gospelee.api.enums;

import java.time.Duration;

public enum RedisCache {
  NONCE(Duration.ofMinutes(5L));

  final private Duration ttl;

  RedisCache(Duration ttl) {
    this.ttl = ttl;
  }

  public Duration ttl() {
    return ttl;
  }

}
