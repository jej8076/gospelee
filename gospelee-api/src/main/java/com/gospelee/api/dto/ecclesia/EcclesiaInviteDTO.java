package com.gospelee.api.dto.ecclesia;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 교회 초대 설정 (교회 관리자용)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EcclesiaInviteDTO {

  private String inviteCode;
  // true: 초대 링크로 가입 시 바로 가입, false: 관리자 승인 후 가입
  private boolean autoApprove;

  @Builder
  public EcclesiaInviteDTO(String inviteCode, boolean autoApprove) {
    this.inviteCode = inviteCode;
    this.autoApprove = autoApprove;
  }
}
