package com.gospelee.api.dto.ecclesia;

import lombok.Builder;
import lombok.Getter;

@Getter
public class EcclesiaRequestDTO {

  private String ecclesiaUid;

  @Builder
  public EcclesiaRequestDTO(String ecclesiaUid) {
    this.ecclesiaUid = ecclesiaUid;
  }
}


