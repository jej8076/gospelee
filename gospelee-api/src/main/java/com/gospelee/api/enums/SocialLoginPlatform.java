package com.gospelee.api.enums;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public enum SocialLoginPlatform {
  EMPTY("empty"),
  KAKAO("kakao"),
  APPLE("apple");

  @Getter
  final private String value;

  SocialLoginPlatform(String value) {
    this.value = value;
  }

  private static final Map<String, SocialLoginPlatform> NAME_MAP = new HashMap<>();

  static {
    for (SocialLoginPlatform appType : values()) {
      NAME_MAP.put(appType.getValue(), appType);
    }
  }

  public static SocialLoginPlatform of(String name) {
    SocialLoginPlatform result = NAME_MAP.get(name);
    if (result == null) {
      return EMPTY;
    }
    return result;
  }
}
