package com.gospelee.api.enums;

import java.util.HashMap;
import java.util.Map;

public enum PushNotificationSendStatusType {
  READY, SENT, RECEIVED, CONFIRMED, FAILED, CANCELLED;

  private static final Map<String, PushNotificationSendStatusType> NAME_MAP = new HashMap<>();

  static {
    for (PushNotificationSendStatusType type : values()) {
      NAME_MAP.put(type.name(), type);
    }
  }

  public static PushNotificationSendStatusType fromName(String name) {
    PushNotificationSendStatusType result = NAME_MAP.get(name);
    if (result == null) {
      throw new IllegalArgumentException("Invalid OrganizationType name: " + name);
    }
    return result;
  }

  public String lowerCaseName() {
    return this.name().toLowerCase();
  }
}
