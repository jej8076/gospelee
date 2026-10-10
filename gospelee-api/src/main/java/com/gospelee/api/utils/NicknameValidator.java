package com.gospelee.api.utils;

/**
 * 닉네임 규칙: 앞뒤 공백 제거 후 2~12자, 제어문자/연속 공백 불가
 */
public final class NicknameValidator {

  public static final int MIN_LENGTH = 2;
  public static final int MAX_LENGTH = 12;
  public static final String RULE_MESSAGE = "닉네임은 2~12자로 입력해주세요.";

  private NicknameValidator() {
  }

  public static String normalize(String nickname) {
    return nickname == null ? "" : nickname.trim().replaceAll("\\s+", " ");
  }

  public static boolean isValid(String normalized) {
    if (normalized == null) {
      return false;
    }
    int length = normalized.codePointCount(0, normalized.length());
    if (length < MIN_LENGTH || length > MAX_LENGTH) {
      return false;
    }
    return normalized.codePoints().noneMatch(Character::isISOControl);
  }
}
