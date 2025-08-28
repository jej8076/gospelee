package com.gospelee.api.dto.common;

import com.gospelee.api.enums.RedisCacheName;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisCacheDTO {

  private RedisCacheName redisCacheName;
  private String key;
  private String value;

  @Builder
  public RedisCacheDTO(RedisCacheName redisCacheName, String key, String value) {
    this.redisCacheName = redisCacheName;
    this.key = key;
    this.value = value;
  }
}
