package com.gospelee.api.dto.common;

import com.gospelee.api.enums.RedisCacheNames;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisCacheDTO {

  private RedisCacheNames redisCacheNames;
  private String key;
  private Object value;

  @Builder
  public RedisCacheDTO(RedisCacheNames redisCacheNames, String key, Object value) {
    this.redisCacheNames = redisCacheNames;
    this.key = key;
    this.value = value;
  }
}
