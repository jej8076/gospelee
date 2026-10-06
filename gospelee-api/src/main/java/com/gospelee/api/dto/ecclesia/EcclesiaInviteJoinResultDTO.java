package com.gospelee.api.dto.ecclesia;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 초대 링크 가입 결과
 * status: JOINED(바로 가입 완료), PENDING(관리자 승인 대기)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EcclesiaInviteJoinResultDTO {

  private Long ecclesiaUid;
  private String ecclesiaName;
  private String status;

  @Builder
  public EcclesiaInviteJoinResultDTO(Long ecclesiaUid, String ecclesiaName, String status) {
    this.ecclesiaUid = ecclesiaUid;
    this.ecclesiaName = ecclesiaName;
    this.status = status;
  }
}
