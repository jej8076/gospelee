package com.gospelee.api.enums;

public enum MinistryMemberStatus {
  ACTIVE,
  /** 가입 승인 대기 */
  PENDING,
  /** 내보낸 이력. 다시 가입하려면 자동 승인이어도 승인 대기를 거친다 */
  REMOVED
}
