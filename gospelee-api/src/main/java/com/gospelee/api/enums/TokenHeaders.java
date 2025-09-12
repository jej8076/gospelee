package com.gospelee.api.enums;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public enum TokenHeaders {
  AUTHORIZATION("Authorization"),
  SOCIAL_ACCESS_TOKEN("Social-Access-Token"),
  SOCIAL_REFRESH_TOKEN("Social-Refresh-Token"),
  ;

  @Getter
  final private String value;

  TokenHeaders(String value) {
    this.value = value;
  }

  private static final Map<String, TokenHeaders> NAME_MAP = new HashMap<>();

  static {
    for (TokenHeaders tokenHeaders : values()) {
      NAME_MAP.put(tokenHeaders.name(), tokenHeaders);
    }
  }

  public static TokenHeaders of(String name) {
    TokenHeaders result = NAME_MAP.get(name);
    if (result == null) {
      throw new IllegalArgumentException("Invalid TokenHeaders name: " + name);
    }
    return result;
  }

}
