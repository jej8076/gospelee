package com.gospelee.api.enums;

import java.util.HashMap;
import java.util.Map;

public enum CategoryType {
  ANNOUNCEMENT, EXTRA;

  private static final Map<String, CategoryType> NAME_MAP = new HashMap<>();

  static {
    for (CategoryType type : values()) {
      NAME_MAP.put(type.name(), type);
    }
  }

  public static CategoryType fromName(String name) {
    CategoryType result = NAME_MAP.get(name);
    if (result == null) {
      throw new IllegalArgumentException("Invalid OrganizationType name: " + name);
    }
    return result;
  }

  public String lowerCaseName() {
    return this.name().toLowerCase();
  }
}
