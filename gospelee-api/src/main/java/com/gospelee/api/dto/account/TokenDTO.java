package com.gospelee.api.dto.account;

import static com.google.auth.http.AuthHttpConstants.BEARER;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenDTO {

  private String idToken;
  private String accessToken;
  private String refreshToken;

  @Builder
  public TokenDTO(String idToken, String accessToken, String refreshToken) {
    this.idToken = idToken;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }

  public void removeBearerIdToken() {
    this.idToken = idToken.replace(BEARER + " ", "");
  }
}
