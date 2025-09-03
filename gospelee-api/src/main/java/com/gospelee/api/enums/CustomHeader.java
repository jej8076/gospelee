package com.gospelee.api.enums;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public enum CustomHeader {
  SOCIAL_ACCESS_TOKEN("Social-Access-Token"),
  X_APP_IDENTIFIER("X-App-Identifier"),
  X_ANONYMOUS_USER_ID("X-Anonymous-User-ID"),
  ;

  private static final Map<String, CustomHeader> NAME_MAP = new HashMap<>();

  static {
    for (CustomHeader header : values()) {
      NAME_MAP.put(header.name(), header);
    }
  }

  @Getter
  final private String headerName;

  CustomHeader(String headerName) {
    this.headerName = headerName;
  }

  public static CustomHeader of(String name) {
    CustomHeader result = NAME_MAP.get(name);
    if (result == null) {
      throw new IllegalArgumentException("Invalid Header name: " + name);
    }
    return result;
  }
}
