package com.gospelee.api.enums;

import java.util.HashMap;
import java.util.Map;

public enum FileCategoryType {
  ANNOUNCEMENT, EXTRA;

  private static final Map<String, FileCategoryType> NAME_MAP = new HashMap<>();

  static {
    for (FileCategoryType type : values()) {
      NAME_MAP.put(type.name(), type);
    }
  }

  public static FileCategoryType fromName(String name) {
    FileCategoryType result = NAME_MAP.get(name);
    if (result == null) {
      throw new IllegalArgumentException("Invalid OrganizationType name: " + name);
    }
    return result;
  }

  public String lowerCaseName() {
    return this.name().toLowerCase();
  }
}
