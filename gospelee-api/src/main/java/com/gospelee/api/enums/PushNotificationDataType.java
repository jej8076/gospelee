package com.gospelee.api.enums;

public enum PushNotificationDataType {
  ROUTE,
  ;


  public String lower() {
    return this.name().toLowerCase();
  }
}
