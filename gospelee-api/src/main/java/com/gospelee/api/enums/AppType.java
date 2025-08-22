package com.gospelee.api.enums;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public enum AppType {
  OOG_WEB("OOG_WEB");

  @Getter
  final private String value;

  AppType(String value) {
    this.value = value;
  }

  private static final Map<String, AppType> NAME_MAP = new HashMap<>();

  static {
    for (AppType appType : values()) {
      NAME_MAP.put(appType.name(), appType);
    }
  }

  public static AppType of(String name) {
    AppType result = NAME_MAP.get(name);
    if (result == null) {
      throw new IllegalArgumentException("Invalid AppType name: " + name);
    }
    return result;
  }
}
