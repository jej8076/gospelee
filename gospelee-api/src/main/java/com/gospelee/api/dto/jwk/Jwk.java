package com.gospelee.api.dto.jwk;

import lombok.Getter;

@Getter
public class Jwk {

  private String kid;
  private String kty;
  private String alg;
  private String use;
  private String n;
  private String e;
}