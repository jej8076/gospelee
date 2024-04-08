package com.gospelee.api.dto.jwt;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
public class JwtPayload {

  private final String issuer;
  private final Set<String> audience;
  private final String sub;
  private final String email;
  private final String nickname;
  private final String picture;


  @Builder
  public JwtPayload(String issuer, String sub, Set<String> audience, String email,
      String nickname, String picture) {
    this.issuer = issuer;
    this.audience = audience;
    this.sub = sub;
    this.email = email;
    this.nickname = nickname;
    this.picture = picture;
  }
}
