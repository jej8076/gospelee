package com.gospelee.api.dto.ecclesia;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 초대 코드로 사전 조회하는 교회 소개 정보 (비로그인 공개)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EcclesiaInviteInfoDTO {

  private String name;
  private String seniorPastorName;
  private String churchAddress;
  private long memberCount;

  @Builder
  public EcclesiaInviteInfoDTO(String name, String seniorPastorName, String churchAddress,
      long memberCount) {
    this.name = name;
    this.seniorPastorName = seniorPastorName;
    this.churchAddress = churchAddress;
    this.memberCount = memberCount;
  }
}
