package com.gospelee.api.dto.jwt;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JwkDTO {

  private String kid;
  private String kty;
  private String alg;
  private String use;
  private String n;
  private String e;
}
