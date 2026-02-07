package com.gospelee.api.enums;

public enum SocialLoginPlatform {
  EMPTY, KAKAO, APPLE;

  public static SocialLoginPlatform of(String name) {
    if (name == null || name.isBlank()) {
      return EMPTY;
    }
    try {
      return SocialLoginPlatform.valueOf(name.toUpperCase());
    } catch (IllegalArgumentException e) {
      return EMPTY;
    }
  }
}
