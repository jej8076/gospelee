package com.gospelee.api.enums;

import java.util.Arrays;
import lombok.Getter;

/**
 * 성경 통독 순서 유형 Enum
 */
@Getter
public enum BibleReadingOrderType {
  CANONICAL("CANONICAL", "성경목차순"),
  CHRONOLOGICAL("CHRONOLOGICAL", "연대기순"),
  NEW_FIRST("NEW_FIRST", "신약 우선"),
  CUSTOM("CUSTOM", "직접 지정 순서");

  private final String code;
  private final String label;

  BibleReadingOrderType(String code, String label) {
    this.code = code;
    this.label = label;
  }

  public static BibleReadingOrderType fromCode(String code) {
    if (code == null || code.isBlank()) {
      return CANONICAL;
    }
    return Arrays.stream(values())
        .filter(type -> type.code.equalsIgnoreCase(code))
        .findFirst()
        .orElse(CANONICAL);
  }

  public static String getLabelByCode(String code) {
    return fromCode(code).getLabel();
  }
}
