package com.gospelee.api.dto.jwt;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
public class JwtPayload {

  private String issuer;
  private String subject;
  private Set<String> audience;
  private String email;
  private String name;
  private String nickname;
  private String pickture;

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
