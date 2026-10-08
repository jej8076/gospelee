package com.gospelee.api.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ContactPhoneUtilsTest {

  @Test
  void validKoreanNumbers_areNormalizedToDigits() {
    assertEquals("0212345678", ContactPhoneUtils.normalizeOrNull("02-1234-5678"));
    assertEquals("021234567", ContactPhoneUtils.normalizeOrNull("02-123-4567"));
    assertEquals("0311234567", ContactPhoneUtils.normalizeOrNull("031 123 4567"));
    assertEquals("01012345678", ContactPhoneUtils.normalizeOrNull("010-1234-5678"));
    assertEquals("07012345678", ContactPhoneUtils.normalizeOrNull("070-1234-5678"));
    assertEquals("15881234", ContactPhoneUtils.normalizeOrNull("1588-1234"));
  }

  @Test
  void invalidNumbers_returnNull() {
    assertNull(ContactPhoneUtils.normalizeOrNull(null));
    assertNull(ContactPhoneUtils.normalizeOrNull(""));
    assertNull(ContactPhoneUtils.normalizeOrNull("   "));
    assertNull(ContactPhoneUtils.normalizeOrNull("abc"));
    assertNull(ContactPhoneUtils.normalizeOrNull("1234"));
    assertNull(ContactPhoneUtils.normalizeOrNull("010-12"));
    assertNull(ContactPhoneUtils.normalizeOrNull("12345678901234"));
    assertNull(ContactPhoneUtils.normalizeOrNull("9912345678"));
  }
}
