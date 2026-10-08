package com.gospelee.api.dto.ecclesia;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EcclesiaVerifyRequestDTO {

  private Long ecclesiaUid;
  private Boolean verified;
}
