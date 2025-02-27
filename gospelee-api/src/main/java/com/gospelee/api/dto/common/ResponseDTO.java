package com.gospelee.api.dto.common;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseDTO {

  private String code;
  private String message;

  @Builder
  public ResponseDTO(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public static ResponseDTO of(String code, String description) {
    return new ResponseDTO(code, description);
  }
}
