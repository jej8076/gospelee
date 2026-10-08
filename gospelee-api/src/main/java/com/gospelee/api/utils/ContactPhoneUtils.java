package com.gospelee.api.utils;

import java.util.regex.Pattern;

/**
 * 운영자가 전화로 확인할 연락처 검증/정규화 (국내 전화번호: 일반/휴대폰/인터넷전화, 1588 등 대표번호)
 */
public final class ContactPhoneUtils {

  // 0으로 시작하는 9~11자리(지역번호, 휴대폰, 070) 또는 1로 시작하는 8자리(1588 등 대표번호)
  private static final Pattern VALID_DIGITS = Pattern.compile("^(0\\d{8,10}|1\\d{7})$");

  private ContactPhoneUtils() {
  }

  /**
   * 숫자만 남긴 전화번호를 반환하고, 올바른 번호가 아니면 null
   */
  public static String normalizeOrNull(String phone) {
    if (phone == null) {
      return null;
    }
    String digits = phone.replaceAll("[^0-9]", "");
    return VALID_DIGITS.matcher(digits).matches() ? digits : null;
  }
}
