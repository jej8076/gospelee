package com.gospelee.api.enums;

import java.util.HashMap;
import java.util.Map;

public enum AccountEcclesiaHistoryStatusType {
  JOIN_REQUEST,      // 참여요청
  INVITE_REQUEST,    // 초대요청
  JOIN_APPROVAL,     // 참여승인
  INVITE_APPROVAL,   // 초대승인
  JOIN_REJECT,     // 참여거절
  INVITE_REJECT,   // 초대거절
  LEAVE,
  ;

  private static final Map<String, AccountEcclesiaHistoryStatusType> NAME_MAP = new HashMap<>();

  static {
    for (AccountEcclesiaHistoryStatusType type : values()) {
      NAME_MAP.put(type.name(), type);
    }
  }

  public static AccountEcclesiaHistoryStatusType of(String name) {
    AccountEcclesiaHistoryStatusType result = NAME_MAP.get(name);
    if (result == null) {
      throw new IllegalArgumentException("Invalid AccountEcclesiaHistoryStatusType name: " + name);
    }
    return result;
  }
}