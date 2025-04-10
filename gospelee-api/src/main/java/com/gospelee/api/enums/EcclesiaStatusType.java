package com.gospelee.api.enums;

public enum EcclesiaStatusType {
  REQUEST("REQ"), REJECT("REJ"), APPROVAL("APL");

  final private String name;

  EcclesiaStatusType(String name) {
    this.name = name;
  }

  public static EcclesiaStatusType fromName(String name) {
    for (EcclesiaStatusType type : EcclesiaStatusType.values()) {
      if (type.getName().equals(name)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown status name: " + name);
  }

  public String getName() {
    return name;
  }
}
