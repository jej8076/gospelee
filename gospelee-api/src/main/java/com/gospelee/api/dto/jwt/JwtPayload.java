package com.gospelee.api.dto.jwt;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
public class JwtPayload {

  private final String issuer;
  private final String subject;
  private final String email;
  private final String name;
  private final String nickname;
  private final String pickture;
  private final Set<String> audience;

  @Builder
  public JwtPayload(String issuer, String subject, Set<String> audience, String email, String name,
      String nickname, String pickture) {
    this.issuer = issuer;
    this.subject = subject;
    this.audience = audience;
    this.email = email;
    this.name = name;
    this.nickname = nickname;
    this.pickture = pickture;
  }
}
