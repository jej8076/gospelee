package com.gospelee.api.enums;

import java.util.Arrays;
import lombok.Getter;

/**
 * 성경 통독 범위 유형 Enum
 */
@Getter
public enum BibleReadingRangeType {
  ALL("ALL", "성경 전체 66권"),
  OLD("OLD", "구약 39권"),
  NEW("NEW", "신약 27권"),
  CUSTOM("CUSTOM", "직접 선택");

  private final String code;
  private final String label;

  BibleReadingRangeType(String code, String label) {
    this.code = code;
    this.label = label;
  }

  public static BibleReadingRangeType fromCode(String code) {
    if (code == null || code.isBlank()) {
      return ALL;
    }
    return Arrays.stream(values())
        .filter(type -> type.code.equalsIgnoreCase(code))
        .findFirst()
        .orElse(ALL);
  }

  public static String getLabelByCode(String code, String customBooks) {
    BibleReadingRangeType type = fromCode(code);
    if (type == CUSTOM) {
      int count = 0;
      if (customBooks != null && !customBooks.isBlank()) {
        count = customBooks.split(",").length;
      }
      return "직접 선택 (" + count + "권)";
    }
    return type.getLabel();
  }
}
