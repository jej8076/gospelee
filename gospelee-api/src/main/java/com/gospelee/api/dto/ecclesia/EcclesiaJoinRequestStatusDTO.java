package com.gospelee.api.dto.ecclesia;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인한 사용자의 교회 가입 요청 상태
 * status: PENDING(승인 대기), REJECTED(반려), NONE(요청 없음)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EcclesiaJoinRequestStatusDTO {

  private Long ecclesiaUid;
  private String ecclesiaName;
  private String status;

  @Builder
  public EcclesiaJoinRequestStatusDTO(Long ecclesiaUid, String ecclesiaName, String status) {
    this.ecclesiaUid = ecclesiaUid;
    this.ecclesiaName = ecclesiaName;
    this.status = status;
  }

  public static EcclesiaJoinRequestStatusDTO none() {
    return EcclesiaJoinRequestStatusDTO.builder().status("NONE").build();
  }
}
