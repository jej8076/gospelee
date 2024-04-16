package com.gospelee.api.entity;

public enum RoleType {
  LAYMAN("LAYMAN"), PASTOR("PASTOR"), SENIOR_PASTOR("SENIOR_PASTOR"), ADMIN("ADMIN");

  final private String name;

  RoleType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
