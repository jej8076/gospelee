package com.gospelee.api.enums;

import java.util.HashMap;
import java.util.Map;

public enum ArticleCategoryType {
  PRAY_24365_DAILY;

  private static final Map<String, ArticleCategoryType> NAME_MAP = new HashMap<>();

  static {
    for (ArticleCategoryType type : values()) {
      NAME_MAP.put(type.name(), type);
    }
  }

  public static ArticleCategoryType fromName(String name) {
    ArticleCategoryType result = NAME_MAP.get(name);
    if (result == null) {
      throw new IllegalArgumentException("Invalid OrganizationType name: " + name);
    }
    return result;
  }

  public String lowerCaseName() {
    return this.name().toLowerCase();
  }


}
