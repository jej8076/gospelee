package com.gospelee.api.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NicknameValidatorTest {

  @Test
  void 공백은_정리하고_길이는_2에서_12자() {
    assertEquals("포도 사랑", NicknameValidator.normalize("  포도    사랑 "));
    assertTrue(NicknameValidator.isValid("포도"));
    assertTrue(NicknameValidator.isValid("열두글자열두글자열두글자"));
  }

  @Test
  void 너무_짧거나_길거나_비어_있으면_거부() {
    assertFalse(NicknameValidator.isValid(NicknameValidator.normalize(null)));
    assertFalse(NicknameValidator.isValid(NicknameValidator.normalize("   ")));
    assertFalse(NicknameValidator.isValid("가"));
    assertFalse(NicknameValidator.isValid("열세글자열세글자열세글자열"));
  }

  @Test
  void 제어문자는_거부() {
    assertFalse(NicknameValidator.isValid("포도\u0000"));
  }
}
