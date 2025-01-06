package com.gospelee.api.enums;

public enum EcclesiaStatusType {
  REQUEST("REQ"), REJECT("REJ"), APPROVAL("APL");

  final private String name;

  EcclesiaStatusType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
