package com.gospelee.api.enums;

import java.util.HashMap;
import java.util.Map;

public enum OrganizationType {
  ECCLESIA, MINISTRY;

  private static final Map<String, OrganizationType> NAME_MAP = new HashMap<>();

  static {
    for (OrganizationType type : values()) {
      NAME_MAP.put(type.name(), type);
    }
  }

  public static OrganizationType fromName(String name) {
    OrganizationType result = NAME_MAP.get(name);
    if (result == null) {
      throw new IllegalArgumentException("Invalid OrganizationType name: " + name);
    }
    return result;
  }

  public String lowerCaseName() {
    return this.name().toLowerCase();
  }
}
