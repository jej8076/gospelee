package com.gospelee.api.dto.Ecclesia;

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


