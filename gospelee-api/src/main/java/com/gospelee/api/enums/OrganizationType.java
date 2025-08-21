package com.gospelee.api.enums;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public enum OrganizationType {
  BRAND_STORY("브랜드스토리", false),
  ECCLESIA("교회", true),
//  MINISTRY("사역단체", false),
//  DEPARTMENT("부서", false),
//  SMALL_GROUP("소그룹", false);
  ;

  private static final Map<String, OrganizationType> NAME_MAP = new HashMap<>();

  static {
    for (OrganizationType type : values()) {
      NAME_MAP.put(type.name(), type);
    }
  }

  @Getter
  private final String displayName;
  @Getter
  private final boolean hasEntity; // Entity 존재 여부

  OrganizationType(String displayName, boolean hasEntity) {
    this.displayName = displayName;
    this.hasEntity = hasEntity;
  }

  public static OrganizationType fromName(String name) {
    OrganizationType result = NAME_MAP.get(name.toUpperCase());
    if (result == null) {
      throw new IllegalArgumentException("Invalid OrganizationType name: " + name);
    }
    return result;
  }

  public String lowerCaseName() {
    return this.name().toLowerCase();
  }

}
