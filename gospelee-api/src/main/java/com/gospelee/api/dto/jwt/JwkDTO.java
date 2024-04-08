package com.gospelee.api.dto.jwt;

import lombok.Getter;

@Getter
public class JwkDTO {

  private String kid;
  private String kty;
  private String alg;
  private String use;
  private String n;
  private String e;
}