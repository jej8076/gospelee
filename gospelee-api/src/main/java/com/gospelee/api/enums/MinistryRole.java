package com.gospelee.api.enums;

/** 모임 안에서의 역할. 계정 전역의 {@link RoleType} 과는 별개다 */
public enum MinistryRole {
  OWNER,
  MANAGER,
  MEMBER;

  public boolean canManage() {
    return this == OWNER || this == MANAGER;
  }
}
