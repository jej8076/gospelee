package com.gospelee.api.dto.common;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NonceRequestDTO {

  private String anonymousId;
  private String nonce;

}
