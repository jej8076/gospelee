package com.gospelee.api.dto.common;

import com.gospelee.api.enums.RedisCache;
import java.time.Duration;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisCacheDTO {

  private RedisCache redisCache;
  private String key;
  private String value;

  @Builder
  public RedisCacheDTO(RedisCache redisCache, String key, String value) {
    this.redisCache = redisCache;
    this.key = key;
    this.value = value;
  }
}
