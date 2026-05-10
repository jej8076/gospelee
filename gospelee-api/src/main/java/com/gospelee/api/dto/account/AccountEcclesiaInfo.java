package com.gospelee.api.dto.account;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountEcclesiaInfo {

  private Long ecclesiaUid;
  private String ecclesiaStatus;

  @Builder
  public AccountEcclesiaInfo(Long ecclesiaUid, String ecclesiaStatus) {
    this.ecclesiaUid = ecclesiaUid;
    this.ecclesiaStatus = ecclesiaStatus;
  }
}
