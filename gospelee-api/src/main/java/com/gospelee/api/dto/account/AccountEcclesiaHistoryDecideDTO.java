package com.gospelee.api.dto.account;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountEcclesiaHistoryDecideDTO {

  private Long id;
  private String status;

  @Builder
  public AccountEcclesiaHistoryDecideDTO(Long id, String status) {
    this.id = id;
    this.status = status;
  }
}