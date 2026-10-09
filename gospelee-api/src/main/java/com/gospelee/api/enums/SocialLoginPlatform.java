package com.gospelee.api.enums;

public enum SocialLoginPlatform {
  EMPTY, KAKAO, APPLE,
  // 앱스토어 심사용 계정 (서버가 발급한 토큰으로 인증)
  REVIEW;

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
