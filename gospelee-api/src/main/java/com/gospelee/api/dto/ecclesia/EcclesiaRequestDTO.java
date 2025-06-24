package com.gospelee.api.dto.ecclesia;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EcclesiaRequestDTO {

  private Long ecclesiaUid;

  @Builder
  public EcclesiaRequestDTO(Long ecclesiaUid) {
    this.ecclesiaUid = ecclesiaUid;
  }
}


