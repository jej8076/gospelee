package com.gospelee.api.enums;

import java.util.HashMap;
import java.util.Map;

public enum Yn {
  Y, N;

  private static final Map<String, Yn> NAME_MAP = new HashMap<>();

  static {
    for (Yn yn : values()) {
      NAME_MAP.put(yn.name(), yn);
    }
  }

  public static Yn of(String name) {
    Yn result = NAME_MAP.get(name);
    if (result == null) {
      throw new IllegalArgumentException("Invalid Yn name: " + name);
    }
    return result;
  }
}
