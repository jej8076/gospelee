package com.gospelee.api.enums;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public enum SocialLoginType {
  KAKAO("kakao"),
  APPLE("apple");

  @Getter
  final private String value;

  SocialLoginType(String value) {
    this.value = value;
  }

  private static final Map<String, SocialLoginType> NAME_MAP = new HashMap<>();

  static {
    for (SocialLoginType appType : values()) {
      NAME_MAP.put(appType.name(), appType);
    }
  }

  public static SocialLoginType of(String name) {
    SocialLoginType result = NAME_MAP.get(name);
    if (result == null) {
      throw new IllegalArgumentException("Invalid SocialLoginType name: " + name);
    }
    return result;
  }
}
