package com.gospelee.api.enums;

import com.gospelee.api.entity.QEcclesia;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public enum OrganizationType {
  ECCLESIA(QEcclesia.ecclesia, QEcclesia.ecclesia.name, QEcclesia.ecclesia.uid);
//  MINISTRY(QMinistry.ministry.name, QMinistry.ministry.uid);

  private static final Map<String, OrganizationType> NAME_MAP = new HashMap<>();

  static {
    for (OrganizationType type : values()) {
      NAME_MAP.put(type.name(), type);
    }
  }

  @Getter
  private final EntityPathBase<?> entity; // Q타입의 루트
  @Getter
  private final StringPath nameField;
  @Getter
  private final NumberPath<Long> idField;

  OrganizationType(EntityPathBase<?> entity, StringPath nameField, NumberPath<Long> idField) {
    this.entity = entity;
    this.nameField = nameField;
    this.idField = idField;
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
