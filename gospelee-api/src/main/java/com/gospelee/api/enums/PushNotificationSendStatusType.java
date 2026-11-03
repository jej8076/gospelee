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

  /**
   * 전달 단계의 진행 정도. 값이 클수록 더 나아간 상태다 (FAILED 는 가장 낮다).
   * 앱이 보고하는 RECEIVED/CONFIRMED 가 늦게 도착해도 상태가 뒤로 가지 않게 비교하는 데 쓴다
   */
  public int progress() {
    return switch (this) {
      case READY, SENT -> 1;
      case RECEIVED -> 2;
      case CONFIRMED -> 3;
      default -> 0;
    };
  }

  public String lowerCaseName() {
    return this.name().toLowerCase();
  }
}
