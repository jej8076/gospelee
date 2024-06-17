package com.gospelee.api.dto.ecclesia;

import lombok.Builder;
import lombok.Getter;

@Getter
public class EcclesiaDTO {

  private String ecclesiaUid;

  @Builder
  public EcclesiaDTO(String ecclesiaUid) {
    this.ecclesiaUid = ecclesiaUid;
  }
}


