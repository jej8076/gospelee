package com.gospelee.api.dto.common;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseDTO {

  private int status;
  private String error;
  private String code;

  @Builder
  public ResponseDTO(int status, String error, String code) {
    this.status = status;
    this.error = error;
    this.code = code;
  }
}
