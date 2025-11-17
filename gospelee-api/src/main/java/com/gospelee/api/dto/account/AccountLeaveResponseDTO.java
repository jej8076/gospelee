package com.gospelee.api.dto.account;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AccountLeaveResponseDTO {

  private Long uid;

  @Builder
  public AccountLeaveResponseDTO(Long uid) {
    this.uid = uid;
  }
}
