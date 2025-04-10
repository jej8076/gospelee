package com.gospelee.api.dto.ecclesia;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EcclesiaUpdateDTO {

  private String status;

  @Builder
  public EcclesiaUpdateDTO(String status) {
    this.status = status;
  }
}


